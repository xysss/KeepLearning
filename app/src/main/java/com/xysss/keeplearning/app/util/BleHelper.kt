package com.xysss.keeplearning.app.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.*


object BleHelper {

    /**
     * 启用指令通知
     */
    fun enableIndicateNotification(gatt: BluetoothGatt): Boolean =
        setCharacteristicNotification(gatt, gatt.getService(UUID.fromString(getString(BleConstant.SERVICE_UUID)))
            .getCharacteristic(UUID.fromString(getString(BleConstant.CHARACTERISTIC_INDICATE_UUID))))

    /**
     * 设置特征通知
     * return true, if the write operation was initiated successfully
     */
    private fun setCharacteristicNotification(gatt: BluetoothGatt, gattCharacteristic: BluetoothGattCharacteristic): Boolean =
        if (gatt.setCharacteristicNotification(gattCharacteristic, true))
            gatt.writeDescriptor(gattCharacteristic.getDescriptor(UUID.fromString(getString(BleConstant.DESCRIPTOR_UUID)))
                .apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }) else false
    /**
     * 发送指令
     * @param gatt gatt
     * @param command 指令
     * @param isResponse 是否响应
     */
    fun sendCommand(gatt: BluetoothGatt, command: String, isResponse: Boolean = true): Boolean =
        gatt.writeCharacteristic(gatt.getService(UUID.fromString(getString(BleConstant.SERVICE_UUID)))
            .getCharacteristic(UUID.fromString(getString(BleConstant.CHARACTERISTIC_WRITE_UUID)))
            .apply {
                writeType = if (isResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                value = ByteUtils.hexStringToBytes(command) })


}