package com.xysss.keeplearning.app.ble

import android.annotation.SuppressLint
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
import com.xysss.keeplearning.app.util.FileUtils
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
    private val transcodingBytesList = ArrayList<Byte>()
    private lateinit var uiCallback: UiCallback
    private lateinit var afterBytes: ByteArray
    private var recordArrayList = ArrayList<Record>()
    private var alarmArrayList = ArrayList<Alarm>()
    private val newLengthBytes = ByteArray(2)
    private var newLength = 0
    private var newIndex = -1
    private var mVocIndex = 0
    private var beforeIsFF = false

    fun setUiCallback(uiCallback: UiCallback) {
        this.uiCallback = uiCallback
    }

    /**
     * 连接状态回调
     */
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Thread.sleep(500)
            gatt.discoverServices()
            "开始连接服务".logE(LogFlag)
        }
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                //获取MtuSize
                //gatt.requestMtu(512)
                "蓝牙已经连接".logE(LogFlag)
//                Thread.sleep(500)
//                gatt.discoverServices()
            }
            else -> "onConnectionStateChange: $status"
        }
//        else{
//            "onConnectionStateChange: $status".logE(LogFlag)
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
        "发出: ${if (status == BluetoothGatt.GATT_SUCCESS) "成功：" else "失败："} ${characteristic.value.toHexString()}".logE(LogFlag)
    }

    /**
     * 描述符写入回调
     */
    @SuppressLint("MissingPermission")
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (mmkv.getString(ValueKey.DESCRIPTOR_UUID,"0") == descriptor.uuid.toString().lowercase(Locale.getDefault())) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) readPhy()
                    readDescriptor(descriptor)
                    readRemoteRssi()
                }

                "蓝牙:通知开启成功，准备完成:".logE(LogFlag)

//                uiCallback.bleConnected("已连接设备")

                scope.launch(Dispatchers.IO) {
                    startSendMessage()
                }

                scope.launch(Dispatchers.IO) {
                    startDealMessage()
                }

                scope.launch(Dispatchers.IO) {
                    delay(500)
                    BleHelper.addSendLinkedDeque(reqDeviceMsg)  //请求设备信息
                }
            } else {
                uiCallback.bleConnected("未连接设备")
                "通知开启失败".logE(LogFlag)
            }
        }
    }

    /**
     * 读取远程设备的信号强度回调
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {}
    //= "onReadRemoteRssi: rssi: $rssi".logE(LogFlag)

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
    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        //initServiceAndChara(gatt)
        if (!BleHelper.enableIndicateNotification(gatt)) {
            gatt.disconnect()
            "开启通知属性异常".logE(LogFlag)
        } else {
            "发现了服务 code: $status".logE("LogFlag")
        }
    }

    /**
     * 特性改变回调
     * 先触发
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        scope.launch(Dispatchers.IO){
            if (isConnectMqtt){
                if (!isPollingModel){
                    uiCallback.mqttSendMsg(characteristic.value)
                }
            }
        }
        "收到数据：${characteristic.value.size}长度: ${characteristic.value.toHexString()}".logE(LogFlag)
        scope.launch(Dispatchers.IO) {
            for (byte in characteristic.value)
                BleHelper.addRecLinkedDeque(byte)
        }
    }

    private suspend fun startSendMessage() {
        while (true) {
            sendLinkedDeque.poll()?.let {
                delay(200)
                BleHelper.sendBlueToothMsg(it)
            }
        }
    }

    private suspend fun startDealMessage() {
        while (true) {
            recLinkedDeque.poll()?.let {
                if (it == ByteUtils.FRAME_START) {
                    transcodingBytesList.clear()
                    transcodingBytesList.add(it)
                } else if (beforeIsFF) {
                    when (it) {
                        ByteUtils.FRAME_FF -> {
                            transcodingBytesList.add(ByteUtils.FRAME_FF)
                        }
                        ByteUtils.FRAME_00 -> {
                            transcodingBytesList.add(ByteUtils.FRAME_START)
                        }
                        else -> {
                            transcodingBytesList.add(ByteUtils.FRAME_FF)
                            transcodingBytesList.add(it)
                        }
                    }
                    beforeIsFF = false
                } else if (!beforeIsFF) {
                    if (it == ByteUtils.FRAME_FF) {
                        beforeIsFF = true
                    } else {
                        beforeIsFF = false
                        transcodingBytesList.add(it)
                    }
                }

                //取协议数据长度
                if (transcodingBytesList.size == 3) {
                    newLengthBytes[0] = transcodingBytesList[1]
                    newLengthBytes[1] = transcodingBytesList[2]
                    newLength = newLengthBytes.readInt16BE()
                    "协议长度: $newLength".logE("LogFlag")
                }

                if (transcodingBytesList.size == newLength && transcodingBytesList.size > 9) {
                    transcodingBytesList.let { arrayList ->
                        afterBytes = ByteArray(arrayList.size)
                        for (k in afterBytes.indices) {
                            afterBytes[k] = arrayList[k]
                        }
                    }

                    isRecOK = if (afterBytes[0] == ByteUtils.FRAME_START && afterBytes[afterBytes.size - 1] == ByteUtils.FRAME_END) {
                        //CRC校验
                        if (Crc8.isFrameValid(afterBytes, afterBytes.size)) {
                            analyseMessage(afterBytes)  //分发数据
                            //"协议正确: ${afterBytes.toHexString()}".logE("LogFlag")
                            true
                        } else {
                            "CRC校验错误，协议长度: $newLength : ${afterBytes.toHexString()}".logE("LogFlag")
                            BleHelper.retryHistoryMessage()
                            false
                        }
                    } else {
                        "协议开头结尾不对:  ${afterBytes.toHexString()}".logE("LogFlag")
                        BleHelper.retryHistoryMessage()
                        false
                    }
                    transcodingBytesList.clear()
                } else if (newLength < 9 && transcodingBytesList.size > 9) { //协议长度不够
                    "解析协议不完整，协议长度: $newLength  解析长度：${transcodingBytesList.size}}".logE("LogFlag")
                    isRecOK = false
                    //BleHelper.retryHistoryMessage(recordCommand,alarmCommand)
                    transcodingBytesList.clear()
                }
            }
        }
    }

    private suspend fun analyseMessage(mBytes: ByteArray?) {
        mBytes?.let {
            scope.launch(Dispatchers.Default) {
                when (it[4]) {
                    //设备信息
                    ByteUtils.Msg80 -> dealMsg80(it)
                    //实时数据
                    ByteUtils.Msg90 -> dealMsg90(it)
                    //历史记录
                    ByteUtils.Msg81 -> dealMsg81(it)
                    //物质信息
                    ByteUtils.MsgA1 -> dealMsgA1(it)
                    //物质库信息
                    ByteUtils.MsgA0 -> dealMsgA0(it)
                    else -> it[4].toInt().logE("LogFlag")
                }
            }

        }
    }

    private fun dealMsg80(mBytes: ByteArray) {
        mBytes.let {
            if (it.size == 71) {
                //设备序列号
                var i = 49
                while (i < it.size)
                    if (it[i] == ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(49, i - 49)

                mmkv.putString(ValueKey.deviceHardwareVersion,it[7].toInt().toString()+"."+it[8].toInt().toString())
                mmkv.putString(ValueKey.deviceSoftwareVersion,it[9].toInt().toString()+"."+it[10].toInt().toString())
                mmkv.putInt(ValueKey.deviceBattery,it[11].toInt())
                mmkv.putInt(ValueKey.deviceFreeMemory,it[12].toInt())
                mmkv.putInt(ValueKey.deviceRecordSum,it.readByteArrayBE(13, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceAlarmSum,it.readByteArrayBE(17, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCurrentRunningTime,it.readByteArrayBE(21, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCurrentAlarmNumber,it.readByteArrayBE(25, 4).readInt32LE())
                mmkv.putInt(ValueKey.deviceCumulativeRunningTime,it.readByteArrayBE(29, 4).readInt32LE())
                mmkv.putString(ValueKey.deviceDensityMax,String.format("%.2f", it.readByteArrayBE(33, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceDensityMin,String.format("%.2f", it.readByteArrayBE(37, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceTwaNumber,String.format("%.2f", it.readByteArrayBE(41, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceSteLNumber,String.format("%.2f", it.readByteArrayBE(45, 4).readFloatLE()))
                mmkv.putString(ValueKey.deviceId,String(tempBytes))
                mmkv.putString(ValueKey.recTopicValue, recTopicDefault+String(tempBytes)+"/")
                mmkv.putString(ValueKey.sendTopicValue, sendTopicDefault+String(tempBytes)+"/")

                "设备信息解析成功: ${String(tempBytes)}".logE("LogFlag")

                uiCallback.bleConnected("已连接设备")
            }
        }
    }

    private suspend fun dealMsg90(mBytes: ByteArray) {
        mBytes.let {
            if (it.size == 69) {
                //浓度值
                val conNum = String.format("%.2f", it.readByteArrayBE(7, 4).readFloatLE())
                //报警状态
                val conState = it.readByteArrayBE(11, 4).readInt32LE()
                //物质库索引
                val maIndex = it.readByteArrayBE(15, 4).readInt32LE()
                //浓度单位
                val conUnit: String = when (it[19].toInt()) {
                    0 -> "ppm"
                    1 -> "ppm"
                    2 -> "mg/m3"
                    else -> ""
                }
                //CF值
                val cf = it.readByteArrayBE(23, 4).readFloatLE()
                //物质名称
                var i = 27
                while (i < it.size)
                    if (it[i] == ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(27, i - 27)
                //val name = tempBytes.toAsciiString()
                val name = String(tempBytes)
                //tempBytes.toHexString().logE("LogFlag")
                materialInfo.apply {
                    concentrationNum = if (it[19].toInt()==1){
                        String.format("%.2f", conNum.toFloat()/1000)
                    }else{
                        conNum
                    }
                    concentrationState=conState.toString()
                    materialLibraryIndex=maIndex
                    concentrationUnit=conUnit
                    cfNum=cf.toString()
                    materialName=name
                }
                "实时检测数据： ${materialInfo.concentrationNum}".logE(LogFlag)
                uiCallback.realData(materialInfo)

                delay(1000)
                if (isRealTimeModel) {
                    BleHelper.addSendLinkedDeque(reqRealTimeDataMsg)
                }
            }
        }
    }

    private suspend fun dealMsg81(mBytes: ByteArray) {
        mBytes.let {
            if (it.size > 18) {
                //开始记录索引
                val dataIndex = it.readByteArrayBE(8, 4).readInt32LE()
                //记录条数
                val dataNum = it.readByteArrayBE(12, 4).readInt32LE()
                //数据记录
                if (it[7] == ByteUtils.FRAME_00) {
                    //uiCallback.state(ByteUtils.RecordRecFlag)
                    recordArrayList = ArrayList(dataNum)
                    for (i in 0..dataNum) {
                        val firstIndex = 16 + i * 48
                        if (firstIndex + 44 < it.size && dataNum > 0) {
                            val mTimestamp = it.readByteArrayBE(firstIndex, 4).readUInt32LE()
                            val mDateStr = ByteUtils.getDateTime((mTimestamp - 28800) * 1000)
                            val mReserve = it.readByteArrayBE(firstIndex + 4, 4).readInt32LE()
                            val mPpm = it.readByteArrayBE(firstIndex + 8, 4).readFloatLE()
                            val mPpmStr = ByteUtils.getNoMoreThanTwoDigits(mPpm)
                            val mCF = it.readByteArrayBE(firstIndex + 12, 4).readFloatLE()
                            mVocIndex = it.readByteArrayBE(firstIndex + 16, 4).readInt32LE()
                            val mAlarm = it.readByteArrayBE(firstIndex + 20, 4).readInt32LE()
                            val mHi = it.readByteArrayBE(firstIndex + 24, 4).readFloatLE()
                            val mLo = it.readByteArrayBE(firstIndex + 28, 4).readFloatLE()
                            val mTwa = it.readByteArrayBE(firstIndex + 32, 4).readFloatLE()
                            val mStel = it.readByteArrayBE(firstIndex + 36, 4).readFloatLE()
                            val mUserId = it.readByteArrayBE(firstIndex + 40, 4).readInt32LE()
                            val mPlaceId = it.readByteArrayBE(firstIndex + 44, 4).readInt32LE()

                            val dataRecord = Record(
                                mDateStr,
                                mReserve.toString(),
                                mPpmStr,
                                mCF.toString(),
                                mVocIndex,
                                mAlarm.toString(),
                                mHi.toString(),
                                mLo.toString(),
                                mTwa.toString(),
                                mStel.toString(),
                                mUserId.toString(),
                                mPlaceId.toString()
                            )
                            //存储数据
                            Repository.insertRecord(dataRecord)
                            //保存文件
                            FileUtils.saveRecord(dataRecord)
//                            if (Repository.forgetRecordIsExist(dataRecord.timestamp) == 0) {
//                            }
                            //请求物质名称
                            if (newIndex != mVocIndex) {
                                if (Repository.forgetMatterIsExist(mVocIndex) == 0) {
                                    val sendBytes = matterIndexMsg.writeInt32LE(mVocIndex.toLong())
                                    val materialNameCommand = matterHeadMsg + sendBytes.toHexString(false).trim()
                                    BleHelper.addSendLinkedDeque(materialNameCommand)
                                    newIndex = mVocIndex
                                    //阻塞1000ms,等待结果协议接收完成,如果底层发送延时时间变动，此处时间也需要更改
                                    delay(1000)
                                }
                            }
                        }
                    }
                    //请求下一包
                    recordData()
                }

                //报警解析
                else if ((it[7] == ByteUtils.FRAME_01)) {
                    //uiCallback.state(ByteUtils.AlarmRecOverFlag)
                    //报警记录协议条数
                    alarmArrayList = ArrayList(dataNum)
                    for (i in 0..dataNum) {
                        val firstIndex = 16 + i * 16
                        if (firstIndex + 12 < it.size && dataNum > 0) {
                            val mTimestamp = it.readByteArrayBE(firstIndex, 4).readUInt32LE()
                            val dateTimeStr = ByteUtils.getDateTime((mTimestamp-28800)*1000)  //以格林威治为标准需要北京时间减8小时

                            val mAlarm = it.readByteArrayBE(firstIndex + 4, 4).readInt32LE()
                            val mType = it.readByteArrayBE(firstIndex + 8, 4).readInt32LE()
                            val mValue = it.readByteArrayBE(firstIndex + 12, 4).readInt32LE()
                            //val alarmRecord=Alarm("123","alarm","mType","mValue")
                            val alarmRecord = Alarm(
                                dateTimeStr,
                                mAlarm.toString(),
                                mType.toString(),
                                mValue.toString()
                            )
                            alarmArrayList.add(alarmRecord)
                        }
                    }
                    alarmData(alarmArrayList)
                }
            }
        }
    }

    private fun dealMsgA1(mBytes: ByteArray) {
        mBytes.let {
            if (it.size == 77) {
                //物质索引号
                val matterIndex = it.readByteArrayBE(7, 4).readInt32LE()
                //cf值
                val mcfNum = String.format("%.2f", it.readByteArrayBE(11, 4).readFloatLE())
                var i = 35
                while (i < it.size)
                    if (it[i] == ByteUtils.FRAME_00) break else i++
                val tempBytes: ByteArray = it.readByteArrayBE(35, i - 35)
                val matterName = String(tempBytes)
                val matter = Matter(matterIndex, matterName, mcfNum)
                if (Repository.forgetMatterIsExist(matter.voc_index_matter) == 0) {
                    Repository.insertMatter(matter)
                }
            } else {
                "查询物质信息协议长度不为77，实际长度：${it.size}".logE("LogFlag")
            }
        }
    }

    private fun dealMsgA0(mBytes: ByteArray) {
        mBytes.let {
            if (it.size == 17) {
                //物质库个数
                val matterSum = it.readByteArrayBE(7, 4).readInt32LE()
                "物质库个数：$matterSum".logE("LogFlag")
                mmkv.putInt(ValueKey.matterSum, matterSum)

                //当前选中索引
                val choiceIndex = it.readByteArrayBE(11, 4).readInt32LE()
            } else {
                "查询物质信息协议长度不为17，实际长度：${it.size}".logE("LogFlag")
            }
        }
    }

    private fun recordData() {
        //Repository.insertRecordList(recordArrayList)
        //recordArrayList.logE("LogFlag")

        val recordProgress = recordIndex * 100 / recordSum
        "recordIndex: $recordIndex recordSum: $recordSum progress: $recordProgress".logE("LogFlag")
        uiCallback.synProgress(recordProgress.toInt(), "${recordIndex - 1}/$recordSum")

        if (recordIndex < recordSum - recordReadNum) {
            val sendBytes = startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
            BleHelper.recordCommand = recordHeadMsg + sendBytes.toHexString(false).trim()
            BleHelper.addSendLinkedDeque(BleHelper.recordCommand)
            recordIndex += recordReadNum
        } else {
            if (recordIndex < recordSum) {
                recordReadNum = recordSum - recordIndex
                val sendBytes = startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
                BleHelper.recordCommand = recordHeadMsg + sendBytes.toHexString(false).trim()
                BleHelper.addSendLinkedDeque(BleHelper.recordCommand)
            }
            recordIndex = recordSum.toLong()
        }
    }


    private fun alarmData(alarmArrayList: ArrayList<Alarm>) {
        if (alarmArrayList.size != 0) {
            for (alarm in alarmArrayList) {
                Repository.insertAlarm(alarm)
                FileUtils.saveAlarm(alarm)
                //不存在
//                if (Repository.forgetAlarmIsExist(alarm.timestamp) == 0) {
//                }
            }
            //Repository.insertAlarmList(alarmArrayList)
            //alarmArrayList.logE("LogFlag")

            val alarmProgress = alarmIndex * 100 / alarmSum
            "alarmIndex: $alarmIndex alarmSum: $alarmSum progress: $alarmProgress".logE("LogFlag")
            uiCallback.synProgress(alarmProgress.toInt(), "${alarmIndex - 1}/$alarmSum")

            if (alarmIndex < alarmSum - alarmReadNum) {
                val sendBytes = startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                BleHelper.alarmCommand = alarmHeadMsg + sendBytes.toHexString(false).trim()
                BleHelper.addSendLinkedDeque(BleHelper.alarmCommand)
                alarmIndex += alarmReadNum
            } else {
                if (alarmIndex < alarmSum) {
                    alarmReadNum = alarmSum - alarmIndex
                    val sendBytes = startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                    BleHelper.alarmCommand = alarmHeadMsg + sendBytes.toHexString(false).trim()
                    BleHelper.addSendLinkedDeque(BleHelper.alarmCommand)
                    "alarmIndex: $alarmIndex:$alarmReadNum:$alarmSum".logE("LogFlag")
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
                    "11read_chara=$read_UUID_chara----read_service=$read_UUID_service".logE("LogFlag")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                    write_UUID_chara = characteristic.uuid
                    write_UUID_service = bluetoothGattService.uuid
                    "22write_chara=$write_UUID_chara----write_service=$write_UUID_service".logE("LogFlag")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                    write_UUID_chara = characteristic.uuid
                    write_UUID_service = bluetoothGattService.uuid
                    "33write_chara=$write_UUID_chara----write_service=$write_UUID_service".logE("LogFlag")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                    notify_UUID_chara = characteristic.uuid
                    notify_UUID_service = bluetoothGattService.uuid
                    "44notify_chara=$notify_UUID_chara----notify_service=$notify_UUID_service".logE("LogFlag")
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                    indicate_UUID_chara = characteristic.uuid
                    indicate_UUID_service = bluetoothGattService.uuid
                    "55indicate_chara=$indicate_UUID_chara----indicate_service=$indicate_UUID_service".logE("LogFlag")
                }
            }
        }
    }

    /**
     * UI回调
     */
    interface UiCallback {
        fun realData(materialInfo: MaterialInfo)
        fun bleConnected(state: String)
        fun synProgress(progress: Int, numShow: String)
        fun mqttSendMsg(bytes: ByteArray)
    }

}