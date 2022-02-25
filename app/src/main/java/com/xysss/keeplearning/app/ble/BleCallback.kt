package com.xysss.keeplearning.app.ble

import android.bluetooth.*
import android.os.Build
import com.swallowsonny.convertextlibrary.*
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Matter
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.Crc8
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


/**
 * Ble回调
 * @description BleCallback
 */
class BleCallback : BluetoothGattCallback() {
    private val TAG = BleCallback::class.java.simpleName
    private val completeBytesList = ArrayList<Byte>()
    private val transcodingBytesList = ArrayList<Byte>()
    private lateinit var uiCallback: UiCallback
    private lateinit var afterBytes: ByteArray
    private var recordArrayList=ArrayList<Record>()
    private var alarmArrayList=ArrayList<Alarm>()

    fun setUiCallback(uiCallback: UiCallback) {
        this.uiCallback = uiCallback
    }

    /**
     * 连接状态回调
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Thread.sleep(500)
            gatt.discoverServices()
        }
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                //获取MtuSize
                //gatt.requestMtu(512)
                "蓝牙已经连接".logE("xysLog")
//                Thread.sleep(500)
//                gatt.discoverServices()
            }
            else -> "onConnectionStateChange: $status"
        }
//        else{
//            "onConnectionStateChange: $status".logE("xysLog")
//        }
//        uiCallback.state(
//            when (newState) {
//                BluetoothProfile.STATE_CONNECTED -> {
//                    //获取MtuSize
//                    //Thread.sleep(600)
//                    gatt.requestMtu(512)
//                    "连接成功"
//                }
//                BluetoothProfile.STATE_DISCONNECTED -> "断开连接"
//                else -> "onConnectionStateChange: $status"
//            }
//        )
    }

    /**
     * 特性写入回调
     * 后触发
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        "发出: ${if (status == BluetoothGatt.GATT_SUCCESS) "成功：" else "失败："} ${characteristic.value.toHexString()}".logE("xysLog")
    }

    /**
     * 描述符写入回调
     */
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (mmkv.getString(ValueKey.DESCRIPTOR_UUID,"0") == descriptor.uuid.toString().lowercase(Locale.getDefault())) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) readPhy()
                    readDescriptor(descriptor)
                    readRemoteRssi()
                }

                defaultIndex=mmkv.getInt(ValueKey.matterIndex,0)
                defaultName= mmkv.getString(ValueKey.matterName,"异丁烯").toString()
                "蓝牙:通知开启成功，准备完成:".logE("xysLog")


                scope.launch(Dispatchers.IO) {
                    startSendMessage()
                }

                scope.launch(Dispatchers.IO) {
                    startDealMessage()
                }

                scope.launch(Dispatchers.IO) {
                    delay(1000)
                    BleHelper.addSendLinkedDeque(reqDeviceMsg)  //请求设备信息
                    //uiCallback.state(bluetoothConnected)
                }
            } else "通知开启失败".logE("xysLog")
        }
    }

    /**
     * 读取远程设备的信号强度回调
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {}
    //= "onReadRemoteRssi: rssi: $rssi".logE("xysLog")

    /**
     * 获取MtuSize回调
     */
//    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
//        uiCallback.state("获取到MtuSize：$mtu")
//        //发现服务
//        gatt.discoverServices()
//    }

    /**
     * 发现服务回调
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        //initServiceAndChara(gatt)
        if (!BleHelper.enableIndicateNotification(gatt)) {
            gatt.disconnect()
            "开启通知属性异常".logE("xysLog")
        } else {
            "发现了服务 code: $status".logE("xysLog")
        }
    }

    /**
     * 特性改变回调
     * 先触发
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//        val id = Thread.currentThread().id
//        "蓝牙回调方法中的线程号：$id".logE("xysLog")
//        "蓝牙回调运行在${if (isMainThread()) "主线程" else "子线程"}中".logE("xysLog")

        scope.launch(Dispatchers.IO){
            if (isConnectMqtt)
                uiCallback.mqttSendMsg(characteristic.value)
        }
        "收到数据：${characteristic.value.size}长度: ${characteristic.value.toHexString()}".logE("xysLog")
        BleHelper.addRecLinkedDeque(characteristic.value)
    }

    private suspend fun startSendMessage(){
        while (true){
            while (sendLinkedDeque.peek()!=null){
                delay(500)
                BleHelper.sendBlueToothMsg(sendLinkedDeque.poll()!!)
            }
        }
    }

    private suspend fun startDealMessage() {
        while (true) {
            while (recLinkedDeque.peek() != null){
                recLinkedDeque.poll()!!.let {
                    //先拼完整包
                    var i = 0
                    while (i < it.size) {
                        //校验开头
                        if (it[i] == ByteUtils.FRAME_START){
                            completeBytesList.clear()
                            completeBytesList.add(it[i])
                        }else{
                            completeBytesList.add(it[i])
                        }
                        i++
                    }
                    if (completeBytesList[completeBytesList.size-1] == ByteUtils.FRAME_END){
                        transcoding()
                    }
                }
            }
        }
    }


    private suspend fun transcoding(){
        completeBytesList.let {
            var i = 0
            while (i < it.size) {
                //校验开头
                if (it[i] == ByteUtils.FRAME_START) {
                    transcodingBytesList.clear()
                    transcodingBytesList.add(it[i])
                }
                else if (i!=it.size-1){
                    //转码
                    if (it[i] == ByteUtils.FRAME_FF) {
                        when{
                            it[i + 1] == ByteUtils.FRAME_FF -> {
                                transcodingBytesList.add(ByteUtils.FRAME_FF)
                                i++
                            }
                            it[i + 1] == ByteUtils.FRAME_00 -> {
                                transcodingBytesList.add(ByteUtils.FRAME_START)
                                i++
                            }
                            else -> {
                                transcodingBytesList.add(it[i])
                            }
                        }
                    } else
                        transcodingBytesList.add(it[i])
                }
                else
                    transcodingBytesList.add(it[i])
                i++
            }
        }

        //数据完成
        val bytes = ByteArray(2)
        bytes[0] = transcodingBytesList[1]
        bytes[1] = transcodingBytesList[2]
        val length = bytes.readInt16BE()
        //校验数据长度
        if (length == transcodingBytesList.size) {
            transcodingBytesList.let {
                afterBytes = ByteArray(it.size)
                for (k in afterBytes.indices) {
                    afterBytes[k] = it[k]
                }
            }
            //CRC校验
            if (Crc8.isFrameValid(afterBytes,afterBytes.size)){
                //分发数据
                analyseMessage(afterBytes)
            }else{
                "CRC校验错误".logE("xysLog")
            }
            transcodingBytesList.clear()
        }else{
            "转码后协议length: $length".logE("xysLog")
            "转码后实际length: ${transcodingBytesList.size}".logE("xysLog")
            //transcodingBytesList.toHexString().logE("xysLog")
            transcodingBytesList.clear()
        }
    }

    private suspend fun analyseMessage(mBytes: ByteArray?) {
        mBytes?.let {
            when (it[4]) {
                //设备信息
                ByteUtils.Msg80 -> {
                    scope.launch(Dispatchers.IO) {
                        dealMsg80(it)
                    }
                }
                //实时数据
                ByteUtils.Msg90 ->{
                    scope.launch(Dispatchers.IO) {
                        dealMsg90(it)
                    }
                }
                //历史记录
                ByteUtils.Msg81 ->{
                    scope.launch(Dispatchers.IO){
                        dealMsg81(it)
                    }
                }
                //物质信息
                ByteUtils.MsgA1 -> {
                    scope.launch(Dispatchers.IO) {
                        dealMsgA1(it)
                    }
                }
                else -> it[4].toInt().logE("xysLog")
            }
        }
    }

    private suspend fun dealMsg80(mBytes: ByteArray){
        mBytes.let {
            if (it.size == 71) {
                //设备序列号
                var i = 49
                while (i < it.size)
                    if (it[i] ==ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(49, i - 49)

                mmkv.putString(ValueKey.deviceHardwareVersion,it[7].toInt().toString()+":"+it[8].toInt().toString())
                mmkv.putString(ValueKey.deviceSoftwareVersion,it[9].toInt().toString()+":"+it[10].toInt().toString())
                mmkv.putInt(ValueKey.deviceBattery,it[11].toInt())
                mmkv.putInt(ValueKey.deviceFreeMemory,it[12].toInt())
                mmkv.putInt(ValueKey.deviceRecordSum,it.readByteArrayBE(13, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceAlarmSum,it.readByteArrayBE(17, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCurrentRunningTime,it.readByteArrayBE(21, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCurrentAlarmNumber,it.readByteArrayBE(25, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCumulativeRunningTime,it.readByteArrayBE(29, 4).readInt32LE())
                mmkv.putString(ValueKey.deviceDensityMax,String.format("%.3f", it.readByteArrayBE(33, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceDensityMin,String.format("%.3f", it.readByteArrayBE(37, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceTwaNumber,String.format("%.3f", it.readByteArrayBE(41, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceSteLNumber,String.format("%.3f", it.readByteArrayBE(45, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceId,String(tempBytes))
                mmkv.putString(ValueKey.recTopicValue, recTopicDefault+String(tempBytes)+"/")
                mmkv.putString(ValueKey.sendTopicValue, sendTopicDefault+String(tempBytes)+"/")

                String(tempBytes).logE("xysLog")
            }
        }
    }

    private suspend fun dealMsg90(mBytes: ByteArray){
        mBytes.let {
            if (it.size == 49) {
                //浓度值
                val concentrationNum = String.format("%.3f", it.readByteArrayBE(7, 4).readFloatLE())
                //报警状态
                val concentrationState = it.readByteArrayBE(11, 4).readInt32LE()
                //物质库索引
                val materialLibraryIndex = it.readByteArrayBE(15, 4).readInt32LE()
                //浓度单位
                val concentrationUnit: String = when (it[19].toInt()) {
                    0 -> "ppm"
                    1 -> "ppb"
                    2 -> "mg/m3"
                    else -> ""
                }
                //CF值
                val cfNum=it.readByteArrayBE(23, 4).readFloatLE()
                //物质名称
                var i = 27
                while (i < it.size)
                    if (it[i] == ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(27, i-27)
                //val name = tempBytes.toAsciiString()
                val name= String(tempBytes)
                val materialInfo = MaterialInfo(
                    concentrationNum, concentrationState.toString(),
                    materialLibraryIndex, concentrationUnit,cfNum.toString(),name
                )
                uiCallback.realData(materialInfo)

                if (!isStopReqRealMsg){
                    delay(1500)
                    BleHelper.addSendLinkedDeque(reqRealTimeDataMsg)
                }
            }
        }
    }

    private suspend fun dealMsg81(mBytes: ByteArray){
        mBytes.let {
            if (it.size > 18) {
                //开始记录索引
                val dataIndex = it.readByteArrayBE(8, 4).readInt32LE()
                //记录条数
                val dataNum = it.readByteArrayBE(12, 4).readInt32LE()
                //数据记录
                if (it[7] == ByteUtils.FRAME_00) {
                    //uiCallback.state(ByteUtils.RecordRecFlag)
                    recordArrayList=ArrayList(dataNum)
                    for (i in 0..dataNum){
                        val firstIndex=16+i*48
                        if (firstIndex+44<it.size&&dataNum>0){
                            val mTimestamp=it.readByteArrayBE(firstIndex,4).readUInt32LE()
                            val mDateStr=ByteUtils.getDateTime(mTimestamp.toString())

                            val mReserve=it.readByteArrayBE(firstIndex+4,4).readInt32LE()

                            val mPpm=it.readByteArrayBE(firstIndex+8,4).readFloatLE()
                            val mPpmStr=ByteUtils.getNoMoreThanTwoDigits(mPpm)

                            val mCF=it.readByteArrayBE(firstIndex+12,4).readFloatLE()
                            val mVocIndex=it.readByteArrayBE(firstIndex+16,4).readInt32LE()
                            //val mVocIndex=2

                            val mAlarm=it.readByteArrayBE(firstIndex+20,4).readInt32LE()
                            val mHi=it.readByteArrayBE(firstIndex+24,4).readFloatLE()
                            val mLo=it.readByteArrayBE(firstIndex+28,4).readFloatLE()
                            val mTwa=it.readByteArrayBE(firstIndex+32,4).readFloatLE()
                            val mStel=it.readByteArrayBE(firstIndex+36,4).readFloatLE()
                            val mUserId=it.readByteArrayBE(firstIndex+40,4).readInt32LE()
                            val mPlaceId=it.readByteArrayBE(firstIndex+44,4).readInt32LE()
                            var name: String
                            if(mVocIndex==defaultIndex) name=defaultName else {
                                name="未知物质"
                                uiCallback.reqMatter(mVocIndex)
                            }
                            val dateRecord=Record(mDateStr,mReserve.toString(),mPpmStr,mCF.toString(),mVocIndex,
                                mAlarm.toString(), mHi.toString(), mLo.toString(),mTwa.toString(),mStel.toString(),mUserId.toString()
                                ,mPlaceId.toString(),name)
                            recordArrayList.add(dateRecord)
                        }
                    }
                    recordData(recordArrayList)
                }

                //报警解析
                else if ((it[7] == ByteUtils.FRAME_01)) {
                    //uiCallback.state(ByteUtils.AlarmRecOverFlag)
                    //报警记录协议条数
                    alarmArrayList=ArrayList(dataNum)
                    for (i in 0..dataNum){
                        val firstIndex=16+i*16
                        if (firstIndex+12<it.size&&dataNum>0){
                            val mTimestamp=it.readByteArrayBE(firstIndex,4).readUInt32LE()
                            val dateTimeStr=ByteUtils.getDateTime(mTimestamp.toString())

                            val mAlarm=it.readByteArrayBE(firstIndex+4,4).readInt32LE()
                            val mType=it.readByteArrayBE(firstIndex+8,4).readInt32LE()
                            val mValue=it.readByteArrayBE(firstIndex+12,4).readInt32LE()
                            //val alarmRecord=Alarm("123","alarm","mType","mValue")
                            val alarmRecord=Alarm(dateTimeStr,mAlarm.toString(),mType.toString(),mValue.toString())
                            alarmArrayList.add(alarmRecord)
                        }
                    }
                    alarmData(alarmArrayList)
                }
            }
        }
    }

    private suspend fun dealMsgA1(mBytes: ByteArray){
        mBytes.let {
            if (it.size == 57) {
                //物质索引号
                defaultIndex=it.readByteArrayBE(7,4).readInt32LE()
                //cf值
                val mcfNum=it.readByteArrayBE(11,4).readInt32LE()
                var i = 35
                while (i < it.size)
                    if (it[i] == ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(35, i - 35)
                defaultName = String(tempBytes)
                val matter=Matter(defaultIndex,defaultName,mcfNum.toString())
                mmkv.putInt(ValueKey.matterIndex,defaultIndex)
                mmkv.putString(ValueKey.matterName,defaultName)
                uiCallback.saveMatter(matter)
            }else{
                "查询物质信息协议长度不为57，实际长度：${it.size}".logE("xysLog")
            }
        }
    }

    private suspend fun recordData(recordArrayList: ArrayList<Record>) {
        if (recordArrayList.size!=0){
            for (record in recordArrayList){
                if (Repository.forgetRecordIsExist(record.timestamp)==0){
                    Repository.insertRecord(record)
                }
            }
            //Repository.insertRecordList(recordArrayList)
            //recordArrayList.logE("xysLog")
            recordSum= mmkv.getInt(ValueKey.deviceRecordSum,0)
            if (recordIndex<recordSum-recordReadNum){
                "recordIndex: $recordIndex".logE("xysLog")
                val sendBytes=startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
                val command=recordHeadMsg+sendBytes.toHexString(false).trim()
                BleHelper.addSendLinkedDeque(command)
                recordIndex += recordReadNum
            }
            else{
                if (recordIndex<recordSum){
                    recordReadNum=recordSum-recordIndex
                    val sendBytes=startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
                    val command=recordHeadMsg+sendBytes.toHexString(false).trim()
                    BleHelper.addSendLinkedDeque(command)
                }
                recordIndex = recordSum.toLong()
            }
        }
    }


    private suspend fun alarmData(alarmArrayList: ArrayList<Alarm>) {
        if (alarmArrayList.size!=0){
            for (alarm in alarmArrayList){
                //不存在
                if (Repository.forgetAlarmIsExist(alarm.timestamp)==0){
                    Repository.insertAlarm(alarm)
                }
            }
            //Repository.insertAlarmList(alarmArrayList)
            //alarmArrayList.logE("xysLog")
            alarmSum= mmkv.getInt(ValueKey.deviceAlarmSum,0)
            if (alarmIndex<alarmSum-alarmReadNum){
                "alarmIndex: $alarmIndex:$alarmReadNum:$alarmSum".logE("xysLog")
                val sendBytes=startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                val command=alarmHeadMsg+sendBytes.toHexString(false).trim()
                BleHelper.addSendLinkedDeque(command)
                alarmIndex += alarmReadNum
            }
            else{
                if (alarmIndex<alarmSum){
                    alarmReadNum=alarmSum-alarmIndex
                    val sendBytes=startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                    val command=alarmHeadMsg+sendBytes.toHexString(false).trim()
                    BleHelper.addSendLinkedDeque(command)
                    "alarmIndex: $alarmIndex:$alarmReadNum:$alarmSum".logE("xysLog")
                }
                alarmIndex = alarmSum.toLong()
            }
        }
    }

    private fun initServiceAndChara(mBluetoothGatt: BluetoothGatt) {
        //服务和特征值
        var write_UUID_service: UUID?
        var write_UUID_chara: UUID?
        var read_UUID_service: UUID?
        var read_UUID_chara: UUID?
        var notify_UUID_service: UUID?
        var notify_UUID_chara: UUID?
        var indicate_UUID_service: UUID?
        var indicate_UUID_chara: UUID?
        val bluetoothGattServices: List<BluetoothGattService> = mBluetoothGatt.getServices()
        for (bluetoothGattService in bluetoothGattServices) {
            val characteristics = bluetoothGattService.characteristics
            for (characteristic in characteristics) {
                val charaProp = characteristic.properties
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                    read_UUID_chara = characteristic.uuid
                    read_UUID_service = bluetoothGattService.uuid
                    "11read_chara=$read_UUID_chara----read_service=$read_UUID_service".logE("xysLog")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                    write_UUID_chara = characteristic.uuid
                    write_UUID_service = bluetoothGattService.uuid
                    "22write_chara=$write_UUID_chara----write_service=$write_UUID_service".logE("xysLog")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                    write_UUID_chara = characteristic.uuid
                    write_UUID_service = bluetoothGattService.uuid
                    "33write_chara=$write_UUID_chara----write_service=$write_UUID_service".logE("xysLog")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                    notify_UUID_chara = characteristic.uuid
                    notify_UUID_service = bluetoothGattService.uuid
                    "44notify_chara=$notify_UUID_chara----notify_service=$notify_UUID_service".logE(
                        "xysLog"
                    )
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                    indicate_UUID_chara = characteristic.uuid
                    indicate_UUID_service = bluetoothGattService.uuid
                    "55indicate_chara=$indicate_UUID_chara----indicate_service=$indicate_UUID_service".logE(
                        "xysLog"
                    )
                }
            }
        }
    }

    /**
     * UI回调
     */
    interface UiCallback {
        fun reqMatter(index:Int)
        fun saveMatter(matter: Matter)
        fun state(state:String?)
        fun realData(materialInfo:MaterialInfo)
        fun mqttSendMsg(bytes:ByteArray)
        //fun recordData(recordArrayList: ArrayList<Record>)
        //fun alarmData(alarmArrayList: ArrayList<Alarm>)
    }

}