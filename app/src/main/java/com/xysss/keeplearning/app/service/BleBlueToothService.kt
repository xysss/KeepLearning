package com.xysss.keeplearning.app.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.xysss.keeplearning.app.ble.BleCallback

class BleBlueToothService : Service() {
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}