package com.xysss.keeplearning.app.util

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import com.blankj.utilcode.util.ToastUtils
import com.swallowsonny.convertextlibrary.toHexString
import com.swallowsonny.convertextlibrary.writeInt32LE
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.mvvmhelper.base.appContext
import java.util.*


object BleHelper {

    //Gatt
    public var gatt: BluetoothGatt?=null
    private var findDevice: BluetoothDevice? = null

    lateinit var transSendCodingBytes: ByteArray
    private val transSendCodingList = ArrayList<Byte>()

    /**
     * 启用指令通知
     */
    fun enableIndicateNotification(gatt: BluetoothGatt): Boolean =
        setCharacteristicNotification(gatt, gatt.getService(UUID.fromString(mmkv.getString(ValueKey.SERVICE_UUID,"")))
            .getCharacteristic(UUID.fromString(mmkv.getString(ValueKey.CHARACTERISTIC_INDICATE_UUID,""))))

    /**
     * 设置特征通知
     * return true, if the write operation was initiated successfully
     */
    private fun setCharacteristicNotification(gatt: BluetoothGatt, gattCharacteristic: BluetoothGattCharacteristic): Boolean =
        if (gatt.setCharacteristicNotification(gattCharacteristic, true))
            gatt.writeDescriptor(gattCharacteristic.getDescriptor(UUID.fromString(mmkv.getString(ValueKey.DESCRIPTOR_UUID,"")))
                .apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }) else false
    /**
     * 发送指令
     * @param gatt gatt
     * @param command 指令
     * @param isResponse 是否响应
     */
    private fun sendCommand(command: String, isResponse: Boolean = true){
        gatt?.writeCharacteristic(gatt?.getService(UUID.fromString(mmkv.getString(ValueKey.SERVICE_UUID,"")))?.
        getCharacteristic(UUID.fromString(mmkv.getString(ValueKey.CHARACTERISTIC_WRITE_UUID,"")))
            ?.apply {
                val sendData=ByteUtils.hexStringToBytes(command)
                val sendDataAll = sendData+Crc8.cal_crc8_t(sendData,sendData.size) + ByteUtils.FRAME_END
                writeType = if (isResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                value=transSendCoding(sendDataAll)
            }) ?: ToastUtils.showShort("蓝牙断开，请重新连接")
    }


    fun connectBlueTooth(device: BluetoothDevice?, bleCallback: BleCallback){
        //gatt连接 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        //第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。
        findDevice=device
        gatt = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                device?.connectGatt(appContext, false, bleCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M_MASK) as BluetoothGatt
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                device?.connectGatt(appContext, false, bleCallback, BluetoothDevice.TRANSPORT_LE) as BluetoothGatt
            }
            else -> {
                device?.connectGatt(appContext, false, bleCallback) as BluetoothGatt
            }
        }
    }

    @Synchronized
    fun addRecLinkedDeque(byte: Byte) {
        recLinkedDeque.add(byte)
    }

    @Synchronized
    fun addSendLinkedDeque(sendMsg:String) {
        sendLinkedDeque.add(sendMsg)
    }

    fun sendBlueToothMsg(command: String){
        if (command.trim().isEmpty()) {
            ToastUtils.showShort("指令为空，请再次尝试")
        }
        val stringBuffer = StringBuffer()
        if (command.length>40){
            for (i in command.indices){
                stringBuffer.append(command[i])
                if (stringBuffer.length==40){
                    sendCommand(stringBuffer.toString())
                    stringBuffer.delete( 0, stringBuffer.length)
                }
            }
            sendCommand(stringBuffer.toString())
        }else{
            sendCommand(command)
        }
    }

    fun sendRecordMsg(){
        recordIndex =1L
        recordReadNum =5L
        val sendBytes= startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(
            recordReadNum
        )
        val command= recordHeadMsg +sendBytes.toHexString(false).trim()
        addSendLinkedDeque(command)
        recordIndex += recordReadNum
    }

    fun sendAlarmMsg(){
        alarmIndex =1L
        alarmReadNum =10L
        val sendBytes= startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(
            alarmReadNum
        )
        val command= alarmHeadMsg +sendBytes.toHexString(false).trim()
        addSendLinkedDeque(command)
        alarmIndex += alarmReadNum
    }

    private fun transSendCoding(bytes: ByteArray): ByteArray {
        bytes.let {
            var i = 1
            if (it[0] == ByteUtils.FRAME_START) {
                transSendCodingList.clear()
                transSendCodingList.add(it[0])
            }
            while (i < it.size) {
                //校验开头
                //开始转码
                when {
                    it[i] == ByteUtils.FRAME_START -> {
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                        transSendCodingList.add(ByteUtils.FRAME_00)
                    }
                    it[i] == ByteUtils.FRAME_FF -> {
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                    }
                    else -> transSendCodingList.add(it[i])
                }
                i++
            }
        }

        transSendCodingList.let {
            transSendCodingBytes = ByteArray(it.size)
            for (k in transSendCodingBytes.indices) {
                transSendCodingBytes[k] = it[k]
            }
        }
        return transSendCodingBytes
    }
}