package com.xysss.keeplearning.app

import android.app.Application
import android.content.Context
import android.os.Process
import androidx.multidex.MultiDex
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.effective.android.anchors.AnchorsManager
import com.effective.android.anchors.Project
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadSir
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.BuildConfig
import com.xysss.keeplearning.app.etx.currentProcessName
import com.xysss.keeplearning.app.etx.getProcessName
import com.xysss.keeplearning.app.event.AppViewModel
import com.xysss.keeplearning.app.event.EventViewModel
import com.xysss.keeplearning.app.weight.loadcallback.EmptyCallback
import com.xysss.keeplearning.app.weight.loadcallback.ErrorCallback
import com.xysss.keeplearning.app.weight.loadcallback.LoadingCallback
import com.xysss.keeplearning.ui.activity.ErrorActivity
import com.xysss.keeplearning.ui.activity.WelcomeActivity


/**
 * Author:bysd-2
 * Time:2021/9/1415:02
 */

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // 获取当前进程名
        val processName = currentProcessName
        if (processName == packageName) {
            // 主进程初始化
            onMainProcessInit()
        } else {
            // 其他进程初始化
            processName?.let { onOtherProcessInit(it) }
        }
    }


    /**
     * @description  代码的初始化请不要放在onCreate直接操作，按照下面新建异步方法
     */
    private fun onMainProcessInit() {
        AnchorsManager.getInstance()
            .debuggable(BuildConfig.DEBUG)
            //设置锚点
            .addAnchor(InitNetWork.TASK_ID, InitUtils.TASK_ID, InitComm.TASK_ID, InitToast.TASK_ID).start(
                Project.Builder("app", AppTaskFactory())
                    .add(InitNetWork.TASK_ID)
                    .add(InitComm.TASK_ID)
                    .add(InitUtils.TASK_ID)
                    .add(InitToast.TASK_ID)
                    .build()
            )
    }

    /**
     * 其他进程初始化，[processName] 进程名
     */
    private fun onOtherProcessInit(processName: String) {}

}