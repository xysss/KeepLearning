package com.xysss.mvvmhelper.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.xysss.mvvmhelper.ext.addActivity
import com.xysss.mvvmhelper.ext.removeActivity

/**
 * Author:bysd-2
 * Time:2021/9/2717:25
 */

class KtxActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStarted(p0: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        XLog.d(activity.javaClass.simpleName)
        addActivity(activity)
    }

    override fun onActivityResumed(p0: Activity) {
    }

}