package com.xysss.keeplearning.app.bluetooth

import android.bluetooth.*
import android.os.Build
import android.util.Log
import com.xysss.keeplearning.app.util.BleConstant
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.getString
import java.util.*
import kotlin.concurrent.thread
import android.bluetooth.BluetoothGattCharacteristic

import android.bluetooth.BluetoothGattService
import com.google.gson.annotations.Until
import com.xysss.keeplearning.app.util.ByteUtils.getCrc16Str
import com.xysss.mvvmhelper.ext.logD
import com.xysss.mvvmhelper.ext.logE
import kotlin.collections.ArrayList
import kotlin.experimental.and


/**
 * Ble回调
 * @description BleCallback
 */
class BleCallback : BluetoothGattCallback() {
    private val TAG = BleCallback::class.java.simpleName
    private lateinit var tempBytes: ByteArray
    private val tempBytesList = ArrayList<Byte>()
    private val dealBytesList = ArrayList<Byte>()
    private val FRAME_START: Byte = 0x55
    private val FRAME_END: Byte = 0x23
    private val FRAMEFF: Byte = 0xFF.toByte()
    private val FRAME00: Byte = 0x00

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
        var write_UUID_service: UUID? = null
        var write_UUID_chara: UUID? = null
        var read_UUID_service: UUID? = null
        var read_UUID_chara: UUID? = null
        var notify_UUID_service: UUID? = null
        var notify_UUID_chara: UUID? = null
        var indicate_UUID_service: UUID? = null
        var indicate_UUID_chara: UUID? = null
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
            if (characteristic.value[i]== FRAME_START) {
                tempBytesList.add(characteristic.value[i])
            } else {
                tempBytesList.add(characteristic.value[i])
                if (tempBytesList[tempBytesList.size - 1] == FRAME_END) {
                    if (tempBytesList[0]==FRAME_START){
                        dealBytes(tempBytesList)
                    }
                    tempBytesList.clear()
                }
            }
        }
    }

    fun dealBytes(recData:ArrayList<Byte>?){
        recData?.let {
            var i=0
            while (i < it.size) {
                if (it[i] ==FRAMEFF){
                    if (it[i+1] ==FRAMEFF){
                        dealBytesList.add(FRAMEFF)
                        i++
                    }
                    else if (it[i+1] ==FRAME00){
                        dealBytesList.add(FRAME_START)
                        i++
                    }
                    else{
                        dealBytesList.add(it[i])
                    }
                }else{
                    dealBytesList.add(it[i])
                }
                i++
            }
        }


        dealBytesList.let {
            tempBytes=ByteArray(it.size)
            for (i in tempBytes.indices){
                tempBytes[i]= it[i]
            }
        }

        if (tempBytes[tempBytes.size-1]==FRAME_END && tempBytes[0]==FRAME_START){
            //CRC校验
            getCrc16Str(tempBytes)
            val content = ByteUtils.bytesToHexString(tempBytes)
            uiCallback.state(content)
            dealBytesList.clear()
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