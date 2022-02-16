package com.xysss.keeplearning.app.ble

import android.bluetooth.*
import android.os.Build
import com.swallowsonny.convertextlibrary.*
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.app.util.BleConstant
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.ByteUtils.FRAME00
import com.xysss.keeplearning.app.util.ByteUtils.FRAME01
import com.xysss.keeplearning.app.util.ByteUtils.FRAME23
import com.xysss.keeplearning.app.util.ByteUtils.FRAME55
import com.xysss.keeplearning.app.util.ByteUtils.Msg80
import com.xysss.keeplearning.app.util.ByteUtils.Msg81
import com.xysss.keeplearning.app.util.ByteUtils.Msg90
import com.xysss.keeplearning.app.util.ByteUtils.MsgA1
import com.xysss.keeplearning.app.util.getString
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.response.DeviceInfo
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.mvvmhelper.ext.logE
import java.util.*
import kotlin.collections.ArrayList


/**
 * Ble回调
 * @description BleCallback
 */
class BleCallback : BluetoothGattCallback() {
    private val TAG = BleCallback::class.java.simpleName
    private val tempBytesList = ArrayList<Byte>()
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
            "onConnectionStateChange: $status".logE("xysLog")
            Thread.sleep(600)
            gatt.discoverServices()
        }
        if (status == BluetoothGatt.STATE_DISCONNECTED){
            "STATE_DISCONNECTED: $status".logE("xysLog")
        }
        else{
            "onConnectionStateChange: $status".logE("xysLog")
        }
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

        if (status == BluetoothGatt.GATT_SUCCESS) {
            "onServicesDiscovered--ACTION_GATT_SERVICES_DISCOVERED".logE("xysLog")
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
     * 特性改变回调
     * 先触发
     */
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
//        val id = Thread.currentThread().id
//        "蓝牙回调方法中的线程号：$id".logE("xysLog")
//        "蓝牙回调运行在${if (isMainThread()) "主线程" else "子线程"}中".logE("xysLog")
        //uiCallback.mqttSendMsg(characteristic.value)

        "收到数据：${characteristic.value.toHexString().length}长度: ${characteristic.value.toHexString()}".logE("xysLog")

        var i = 0
        while (i < characteristic.value.size) {
            //校验开头
            if (characteristic.value[i] == FRAME55) {
                tempBytesList.clear()
                tempBytesList.add(characteristic.value[i])
            } else {
                //转码
                if (characteristic.value[i] == ByteUtils.FRAMEFF) {
                    if (characteristic.value[i + 1] == ByteUtils.FRAMEFF) {
                        tempBytesList.add(ByteUtils.FRAMEFF)
                        i++
                    } else if (characteristic.value[i + 1] == FRAME00) {
                        tempBytesList.add(FRAME55)
                        i++
                    } else {
                        tempBytesList.add(characteristic.value[i])
                    }
                } else {
                    tempBytesList.add(characteristic.value[i])
                }
            }
            i++
        }

        //校验结尾
        if (tempBytesList[tempBytesList.size - 1] == FRAME23) {
            val bytes = ByteArray(2)
            bytes[0] = tempBytesList[1]
            bytes[1] = tempBytesList[2]
            val length = bytes.readInt16BE()
            //"length: $length".logE("xysLog")
            if (tempBytesList[0] == FRAME55 && length == tempBytesList.size) {
                //校验数据长度
                tempBytesList.let {
                    afterBytes = ByteArray(it.size)
                    for (k in afterBytes.indices) {
                        afterBytes[k] = it[k]
                    }
                }
                dealMessage(afterBytes)
                tempBytesList.clear()
            }
        }
    }

    private fun dealMessage(mBytes: ByteArray?) {
        mBytes?.let {
            uiCallback.state("收到解析后的收据长度：${mBytes.size}")
            when (it[4]) {
                //设备信息
                Msg80 -> {
                    if (it.size == 71) {
                        val hardWareMainVersion: Int = it[7].toInt()
                        val hardWareSecondVersion: Int = it[8].toInt()
                        val softWareMainVersion: Int = it[9].toInt()
                        val softWareSecondVersion: Int = it[10].toInt()

                        val recordNum = it.readByteArrayBE(13, 4).readInt32LE()
                        val alarmNum = it.readByteArrayBE(17, 4).readInt32LE()
                        mmkv.putInt(ValueKey.recordSumNum,recordNum)
                        mmkv.putInt(ValueKey.alarmSumNum,alarmNum)
                        //设备序列号
                        var i = 49
                        while (i < it.size)
                            if (it[i] == FRAME00) break else i++
                        val tempBytes: ByteArray = it.readByteArrayBE(49, i - 49)
                        //val deviceId = tempBytes.toAsciiString()
                        val deviceId= String(tempBytes)
                        val deviceInfo = DeviceInfo(
                            "$hardWareMainVersion:$hardWareSecondVersion", "$softWareMainVersion:$softWareSecondVersion",
                            recordNum,alarmNum ,deviceId
                        )
                        uiCallback.state(deviceInfo.toString())
                    }
                }
                //实时数据
                Msg90 -> {
                    if (it.size == 49) {
                        //浓度值
                        val concentrationNumTemp = it.readByteArrayBE(7, 4).readFloatLE()
                        val concentrationNum = String.format("%.3f", concentrationNumTemp)
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
                            if (it[i] == FRAME00) break else i++
                        val tempBytes: ByteArray = it.readByteArrayBE(27, i-27)
                        //val name = tempBytes.toAsciiString()
                        val name= String(tempBytes)
                        val materialInfo = MaterialInfo(
                            concentrationNum, concentrationState.toString(),
                            materialLibraryIndex.toString(), concentrationUnit,cfNum.toString(),name
                        )
                        uiCallback.realData(materialInfo.toString())
                        uiCallback.state(materialInfo.toString())
                    }
                }
                //历史记录
                Msg81 -> {
                    if (it.size > 18) {
                        //开始记录索引
                        val dataIndex = it.readByteArrayBE(8, 4).readInt32LE()
                        //记录条数
                        val dataNum = it.readByteArrayBE(12, 4).readInt32LE()
                        //数据记录
                        if (it[7] == FRAME00) {
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

                                    val mAlarm=it.readByteArrayBE(firstIndex+20,4).readInt32LE()
                                    val mHi=it.readByteArrayBE(firstIndex+24,4).readFloatLE()
                                    val mLo=it.readByteArrayBE(firstIndex+28,4).readFloatLE()
                                    val mTwa=it.readByteArrayBE(firstIndex+32,4).readFloatLE()
                                    val mStel=it.readByteArrayBE(firstIndex+36,4).readFloatLE()
                                    val mUserId=it.readByteArrayBE(firstIndex+40,4).readInt32LE()
                                    val mPlaceId=it.readByteArrayBE(firstIndex+44,4).readInt32LE()

                                    val defaultIndex=mmkv.getInt(ValueKey.dataIndex,0)
                                    val defaultName= mmkv.getString(ValueKey.dataName,"异丁烯")
                                    var name="异丁烯"
                                    if(mVocIndex==defaultIndex) name=defaultName?:"异丁烯" else {

                                        // TODO: 2022/2/10 去请求新的名称
                                        name="未知物质"
                                    }
                                    val dateRecord=Record(mDateStr,mReserve.toString(),mPpmStr,mCF.toString(),mVocIndex.toString(),
                                        mAlarm.toString(), mHi.toString(), mLo.toString(),mTwa.toString(),mStel.toString(),mUserId.toString()
                                        ,mPlaceId.toString(),name)
                                    recordArrayList.add(dateRecord)
                                }
                            }
                        }

                        //报警解析
                        else if ((it[7] == FRAME01)) {
                            //uiCallback.state(ByteUtils.AlarmRecOverFlag)
                            //报警记录协议条数不对
                            alarmArrayList=ArrayList(dataNum)
                            for (i in 0..dataNum){
                                val firstIndex=16+i*16
                                if (firstIndex+12<it.size&&dataNum>0){
                                    val mTimestamp=it.readByteArrayBE(firstIndex,4).readUInt32LE()
                                    val dateTimeStr=ByteUtils.getDateTime(mTimestamp.toString())

                                    val mAlarm=it.readByteArrayBE(firstIndex+4,4).readInt32LE()
                                    val mType=it.readByteArrayBE(firstIndex+8,4).readInt32LE()
                                    val mValue=it.readByteArrayBE(firstIndex+12,4).readInt32LE()
                                    val alarmRecord=Alarm(dateTimeStr,mAlarm.toString(),mType.toString(),mValue.toString())
                                    alarmArrayList.add(alarmRecord)
                                }
                            }
                        }

                        uiCallback.historyData(recordArrayList,alarmArrayList)
                    }
                }
                //查询物质信息
                MsgA1->{
                    if (it.size == 57) {
                        //物质索引号
                        val mIndex=it.readByteArrayBE(7,4).readInt32LE()
                        var i = 35
                        while (i < it.size)
                            if (it[i] == FRAME00) break else i++
                        val tempBytes: ByteArray = it.readByteArrayBE(35, i - 35)
                        val name = String(tempBytes)

                        mmkv.putInt(ValueKey.dataIndex,mIndex)
                        mmkv.putString(ValueKey.dataName,name)

                        uiCallback.state("查询物质信息: $mIndex: $name")
                    }
                }
            }
        }
    }

    /**
     * 特性写入回调
     * 后触发
     */
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        uiCallback.state("发出: ${if (status == BluetoothGatt.GATT_SUCCESS) "成功：" else "失败："}" +
                    "${characteristic.value.toHexString()} code: $status"
        )
    }

    /**
     * 描述符写入回调
     */
    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        if (getString(BleConstant.DESCRIPTOR_UUID) == descriptor.uuid.toString()
                .lowercase(Locale.getDefault())
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) readPhy()
                    readDescriptor(descriptor)
                    readRemoteRssi()
                }
                uiCallback.state("蓝牙连接完成")
                "通知开启成功，准备完成:".logE("xysLog")

            } else "通知开启失败".logE("xysLog")
        }
    }

    /**
     * 读取远程设备的信号强度回调
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) =
        "onReadRemoteRssi: rssi: $rssi".logE("xysLog")

    /**
     * UI回调
     */
    interface UiCallback {
        /**
         * 当前Ble状态信息
         */
        fun state(state:String?)
        fun realData(data: String?)
        fun mqttSendMsg(bytes:ByteArray)
        fun historyData(recordArrayList: ArrayList<Record>,alarmArrayList: ArrayList<Alarm>)
    }

}