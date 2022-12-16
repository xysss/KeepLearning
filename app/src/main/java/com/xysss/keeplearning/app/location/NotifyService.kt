package com.xysss.keeplearning.app.location

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import androidx.annotation.Nullable
import com.xysss.keeplearning.ILocationHelperServiceAIDL
import com.xysss.keeplearning.ILocationServiceAIDL
import com.xysss.mvvmhelper.base.appContext

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:10
 * 描述 : 利用双service进行notification绑定，进而将Service的OOM_ADJ提高到1
 * 同时利用LocationHelperService充当守护进程，在NotiService被关闭后，重启他。（如果LocationHelperService被停止，NotiService不负责唤醒)
 */

open class NotifyService : Service() {
    companion object {
        private const val NOTI_ID = 123321
    }
    var mBinder: Binder? = null
    private var mCloseReceiver: LocationUtil.CloseServiceReceiver? = null
    private var connection: ServiceConnection? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mCloseReceiver = LocationUtil.CloseServiceReceiver(this)
        registerReceiver(mCloseReceiver, LocationUtil.getCloseServiceFilter())
        return START_STICKY
    }

    override fun onDestroy() {
        if (mCloseReceiver != null) {
            unregisterReceiver(mCloseReceiver)
            mCloseReceiver = null
        }
        super.onDestroy()
    }

    /**
     * 触发利用notification增加进程优先级
     */
    protected fun applyNotiKeepMech() {
        startForeground(NOTI_ID, LocationUtil.buildNotification(appContext))
        startBindHelperService()
    }

    fun unApplyNotiKeepMech() {
        stopForeground(true)
    }

    private fun startBindHelperService() {
        val mHelperServiceName = "com.xysss.keeplearning.app.location.LocationHelperService"
        connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                //doing nothing
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val l: ILocationHelperServiceAIDL =
                    ILocationHelperServiceAIDL.Stub.asInterface(service)
                try {
                    l.onFinishBind(NOTI_ID)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        val intent = Intent()
        intent.action = mHelperServiceName
        bindService(
            LocationUtil.getExplicitIntent(applicationContext, intent),
            connection as ServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        if (mBinder == null) {
            mBinder = LocationServiceBinder()
        }
        return mBinder
    }

    inner class LocationServiceBinder : ILocationServiceAIDL.Stub() {
        override fun onFinishBind() {}
    }
}
