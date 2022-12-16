package com.xysss.keeplearning.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import java.util.concurrent.ThreadFactory

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:51
 * 描述 : 获得PARTIAL_WAKE_LOCK	， 保证在息屏状体下，CPU可以正常运行
 */
class PowerManagerUtil {
    private object Holder {
        var instance = PowerManagerUtil()
    }

    private var pm: PowerManager? = null
    private var pmLock: PowerManager.WakeLock? = null

    /**
     * 上次唤醒屏幕的触发时间
     */
    private var mLastWakupTime = System.currentTimeMillis()

    /**
     * 最小的唤醒时间间隔，防止频繁唤醒。默认5分钟
     */
    private val mMinWakupInterval = (5 * 60 * 1000).toLong()

    /**
     * 内部线程工厂
     */
    private var mInnerThreadFactory: InnerThreadFactory? = null

    /**
     * 判断屏幕是否处于点亮状态
     *
     * @param context
     */
    fun isScreenOn(context: Context): Boolean {
        return try {
            val isScreenMethod =
                PowerManager::class.java.getMethod(
                    "isScreenOn"
                )
            if (pm == null) {
                pm =
                    context.getSystemService(Context.POWER_SERVICE) as PowerManager
            }
            isScreenMethod.invoke(pm) as Boolean
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 唤醒屏幕
     */
    fun wakeUpScreen(context: Context?) {
        try {
            acquirePowerLock(
                context,
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK
            )
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 根据levelAndFlags，获得PowerManager的WaveLock
     * 利用worker thread去获得锁，以免阻塞主线程
     * @param context
     * @param levelAndFlags
     */
    @SuppressLint("InvalidWakeLockTag")
    private fun acquirePowerLock(context: Context?, levelAndFlags: Int) {
        if (context == null) {
            throw NullPointerException("when invoke aquirePowerLock ,  context is null which is unacceptable")
        }
        val currentMills = System.currentTimeMillis()
        if (currentMills - mLastWakupTime < mMinWakupInterval) {
            return
        }
        mLastWakupTime = currentMills
        if (mInnerThreadFactory == null) {
            mInnerThreadFactory = InnerThreadFactory()
        }
        mInnerThreadFactory!!.newThread {
            if (pm == null) {
                pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            }
            if (pmLock != null) { // release
                pmLock!!.release()
                pmLock = null
            }
            pmLock = pm?.newWakeLock(levelAndFlags, "MyTag")
            pmLock?.acquire()
            pmLock?.release()
        }.start()
    }

    /**
     * 线程工厂
     */
    private inner class InnerThreadFactory : ThreadFactory {
        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable)
        }
    }

    companion object {
        val instance: PowerManagerUtil
            get() = Holder.instance
    }
}
