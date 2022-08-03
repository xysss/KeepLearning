package com.xysss.keeplearning.app.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import com.xysss.mvvmhelper.base.appContext

/**
 * 作者 : xys
 * 时间 : 2022-08-02 16:19
 * 描述 : 描述
 */

class TimeTask<T : TimeTask.Task?>(context: Context, actionName: String, task: T) {

    private var mContext: Context= appContext
    private val mActionName: String
    private var mReceiver: TimeTaskReceiver? = null
    private val mTask: T?

    companion object {
        private var mPendingIntent: PendingIntent? = null
    }

    init {
        mContext = context
        mActionName = actionName
        mTask = task
        initReceiver(context, actionName)
    }

    fun startLooperTask() {
        if (null != mTask) {
            mTask.exeTask()
            configureAlarmManager(mTask.period())
        }
    }

    fun stopLooperTask() {
        cancelAlarmManager()
    }

    fun onClose() {
        mContext.unregisterReceiver(mReceiver)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun configureAlarmManager(time: Long) {
        val manager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendIntent = pendingIntent
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + time,
                    pendIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                manager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + time,
                    pendIntent
                )
            }
            else -> {
                manager[AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + time] = pendIntent
            }
        }
    }

    @get:SuppressLint("UnspecifiedImmutableFlag")
    private val pendingIntent: PendingIntent?
        get() {
            if (mPendingIntent == null) {
                val requestCode = 0
                val intent = Intent()
                intent.action = mActionName
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        mPendingIntent = PendingIntent.getBroadcast(
                            mContext, requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                    else -> {
                        mPendingIntent = PendingIntent.getBroadcast(
                            mContext,
                            requestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                }
            }
            return mPendingIntent
        }

    private fun cancelAlarmManager() {
        val manager = mContext!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent)
    }

    private fun initReceiver(context: Context, actionName: String) {
        mReceiver = TimeTaskReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(actionName)
        context.registerReceiver(mReceiver, intentFilter)
    }

    internal inner class TimeTaskReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            startLooperTask()
        }
    }

    interface Task {
        fun period(): Long {
            // 默认时间5S
            return 5000L
        }

        fun exeTask()
    }
}
