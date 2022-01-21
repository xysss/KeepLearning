package com.xysss.keeplearning.app.ble

import android.bluetooth.*
import android.os.Build
import com.swallowsonny.convertextlibrary.readByteArrayBE
import com.swallowsonny.convertextlibrary.toAsciiString
import com.xysss.keeplearning.app.util.BleConstant
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.getString
import com.xysss.keeplearning.viewmodel.DeviceInfo
import com.xysss.keeplearning.viewmodel.MaterialInfo
import com.xysss.mvvmhelper.ext.logE
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList


/**
 * Ble回调
 * @description BleCallback
 */
class BleCallback : BluetoothGattCallback() {
    private val TAG = BleCallback::class.java.simpleName
    private lateinit var afterBytes: ByteArray
    private val tempBytesList = ArrayList<Byte>()
    private val dealBytesList = ArrayList<Byte>()
    private val FRAME_START: Byte = 0x55
    private val FRAME_END: Byte = 0x23
    private val FRAMEFF: Byte = 0xFF.toByte()
    private val FRAME00: Byte = 0x00

    private val Msg80: Byte = 0x80.toByte()
    private val Msg90: Byte = 0x90.toByte()

    private lateinit var uiCallback: UiCallback

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

        val content = ByteUtils.bytesToHexString(characteristic.value)
        "收到数据：$content".logE("xysLog")

        for (i in characteristic.value.indices) {
            if (characteristic.value[i] == FRAME_START) {
                tempBytesList.clear()
                tempBytesList.add(characteristic.value[i])
            } else {
                tempBytesList.add(characteristic.value[i])
                if (tempBytesList[tempBytesList.size - 1] == FRAME_END) {
                    if (tempBytesList[0] == FRAME_START) {
                        dealBytes(tempBytesList)
                    }
                    tempBytesList.clear()
                }
            }
        }
    }

    fun dealBytes(recData: ArrayList<Byte>?) {
        recData?.let {
            var i = 0
            while (i < it.size) {
                if (it[i] == FRAMEFF) {
                    if (it[i + 1] == FRAMEFF) {
                        dealBytesList.add(FRAMEFF)
                        i++
                    } else if (it[i + 1] == FRAME00) {
                        dealBytesList.add(FRAME_START)
                        i++
                    } else {
                        dealBytesList.add(it[i])
                    }
                } else {
                    dealBytesList.add(it[i])
                }
                i++
            }
        }

        dealBytesList.let {
            afterBytes = ByteArray(it.size)
            for (i in afterBytes.indices) {
                afterBytes[i] = it[i]
            }
        }

        if (afterBytes[afterBytes.size - 1] == FRAME_END && afterBytes[0] == FRAME_START) {
            //CRC校验
            //val crc16Str = getCrc16Str(tempBytes)
            dealBytesList.clear()
            val realStr = ByteUtils.bytesToHexString(afterBytes)
            uiCallback.state(realStr)

            handMessage(afterBytes)

//            val id = Thread.currentThread().id
//            "蓝牙回调方法中的线程号：$id".logE("xysLog")
//            "蓝牙回调运行在${if (isMainThread()) "主线程" else "子线程"}中".logE("xysLog")

        }
    }

    fun handMessage(mBytes: ByteArray?){
        mBytes?.let {
            when(it[4]){
                Msg80->{
                    if (it.size==71){
                        val hardWareMainVersion:Int=it[7].toInt()
                        val hardWareSecondVersion:Int=it[8].toInt()
                        val softWareMainVersion:Int=it[9].toInt()
                        val softWareSecondVersion:Int=it[10].toInt()
                        //设备序列号
                        var i=49
                        while (i<it.size)
                            if (it[i]==FRAME00) break else i++
                        var tempBytes: ByteArray = it.readByteArrayBE(49,i-49)
                        val deviceId = tempBytes.toAsciiString()

                        var deviceInfo=DeviceInfo("$hardWareMainVersion:$hardWareSecondVersion",
                            "$softWareMainVersion:$softWareSecondVersion",deviceId)
                        uiCallback.state(deviceInfo.toString())
//                        val tempBytesStr = ByteUtils.bytesToHexString(tempBytes)
//                        uiCallback.state(tempBytesStr)
                    }
                }
                Msg90->{
                    if (it.size==22){
                        val concentrationNumBefore :ByteArray=it.readByteArrayBE(7,4)
                        val reverse =ByteArray(4)

                        reverse[3]=concentrationNumBefore[0]
                        reverse[2]=concentrationNumBefore[1]
                        reverse[1]=concentrationNumBefore[2]
                        reverse[0]=concentrationNumBefore[3]
                        //浓度值
                        val concentrationNumTemp=ByteBuffer.wrap(reverse).float
                        val concentrationNum=String.format("%.3f", concentrationNumTemp)
                        //
                        val concentrationState=ByteBuffer.wrap(it.readByteArrayBE(11,4)).int
                        val materialLibraryIndex=ByteBuffer.wrap(it.readByteArrayBE(15,4)).int
                        //浓度单位
                        val concentrationUnit:String
                        when(it[19].toInt()){
                            0->concentrationUnit="ppm"
                            1->concentrationUnit="ppb"
                            2->concentrationUnit="mg/m3"
                            else->concentrationUnit=""
                        }

                        val materialInfo= MaterialInfo(concentrationNum, concentrationState.toString(),
                            materialLibraryIndex.toString(),concentrationUnit)
                        uiCallback.state(materialInfo.toString())
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
        val command = ByteUtils.bytesToHexString(characteristic.value)
        "发出: ${if (status == BluetoothGatt.GATT_SUCCESS) "成功：" else "失败："}$command code: $status".logE(
            "xysLog"
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
                uiCallback.state("通知开启成功，准备完成!")
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
        fun state(state: String?)
    }

}