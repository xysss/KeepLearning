package com.xysss.keeplearning.ui.activity.gaode.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.xysss.keeplearning.ui.activity.gaode.collect.TripTrackCollection
import com.xysss.keeplearning.ui.activity.gaode.contract.ITripTrackCollection

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:32
 * 描述 : 描述
 */
class TrackCollectService : Service(){
    var mTrackCollection: TripTrackCollection? = null
    private var isstarting = false

    override fun onBind(intent: Intent?): IBinder {
        return DataBinder()
    }

    override fun onCreate() {
        super.onCreate()
        mTrackCollection = TripTrackCollection.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTrackCollection?.destory()
    }

    inner class DataBinder : Binder(), ITripTrackCollection {
        override fun start() {
            if (isstarting) {
                return
            }
            if (mTrackCollection == null) {
                mTrackCollection = TripTrackCollection.getInstance()
            }
            mTrackCollection?.start()
            isstarting = true
        }

        override fun stop() {
            if (mTrackCollection != null) {
                mTrackCollection?.stop()
            }
            isstarting = false
        }

        override fun pause() {}
        override fun saveHoldStatus() {}
        override fun destory() {}
    }
}
