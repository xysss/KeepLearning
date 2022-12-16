package com.xysss.keeplearning.app.location

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import androidx.annotation.Nullable
import com.xysss.keeplearning.ILocationHelperServiceAIDL
import com.xysss.keeplearning.ILocationServiceAIDL
import com.xysss.mvvmhelper.base.appContext

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:08
 * 描述 : 描述
 */

class LocationHelperService : Service() {
    private var mCloseReceiver: LocationUtil.CloseServiceReceiver? = null
    override fun onCreate() {
        super.onCreate()
        startBind()
        mCloseReceiver = LocationUtil.CloseServiceReceiver(this)
        registerReceiver(mCloseReceiver, LocationUtil.getCloseServiceFilter())
    }

    override fun onDestroy() {
        if (mInnerConnection != null) {
            unbindService(mInnerConnection!!)
            mInnerConnection = null
        }
        if (mCloseReceiver != null) {
            unregisterReceiver(mCloseReceiver)
            mCloseReceiver = null
        }
        super.onDestroy()
    }

    private var mInnerConnection: ServiceConnection? = null
    private fun startBind() {
        val locationServiceName = "com.xysss.keeplearning.app.location.LocationService"
        mInnerConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                val intent = Intent()
                intent.action = locationServiceName
                startService(LocationUtil.getExplicitIntent(applicationContext, intent))
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val l: ILocationServiceAIDL = ILocationServiceAIDL.Stub.asInterface(service)
                try {
                    l.onFinishBind()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        val intent = Intent()
        intent.action = locationServiceName
        bindService(LocationUtil.getExplicitIntent(appContext, intent), mInnerConnection as ServiceConnection, BIND_AUTO_CREATE)
    }

    private var mBinder: HelperBinder? = null

    private inner class HelperBinder : ILocationHelperServiceAIDL.Stub() {
        @Throws(RemoteException::class)
        override fun onFinishBind(notiId: Int) {
            startForeground(
                notiId,
                LocationUtil.buildNotification(this@LocationHelperService.applicationContext)
            )
            stopForeground(true)
        }
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        if (mBinder == null) {
            mBinder = HelperBinder()
        }
        return mBinder
    }
}
