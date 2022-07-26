package com.xysss.keeplearning.app.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager.WakeLock
import com.baidu.trace.model.StatusCodes

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:13
 * 描述 : 描述
 */
class TrackReceiver(wakeLock: WakeLock) : BroadcastReceiver(){
    private var wakeLock: WakeLock? = null

    init {
        this.wakeLock = wakeLock
    }

    @SuppressLint("Wakelock")
    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_SCREEN_OFF == action) {
            if (null != wakeLock && !wakeLock!!.isHeld) {
                wakeLock!!.acquire()
            }
        } else if (Intent.ACTION_SCREEN_ON == action || Intent.ACTION_USER_PRESENT == action) {
            if (null != wakeLock && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }
        } else if (StatusCodes.GPS_STATUS_ACTION == action) {
            val statusCode = intent.getIntExtra("statusCode", 0)
            val statusMessage = intent.getStringExtra("statusMessage")
            println(
                String.format(
                    "GPS status, statusCode:%d, statusMessage:%s", statusCode,
                    statusMessage
                )
            )
        }
    }

}