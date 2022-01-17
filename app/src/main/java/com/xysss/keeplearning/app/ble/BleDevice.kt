package com.xysss.keeplearning.app.bluetooth

import android.bluetooth.BluetoothDevice

data class BleDevice(var device: BluetoothDevice, var rssi:Int, var name:String?)