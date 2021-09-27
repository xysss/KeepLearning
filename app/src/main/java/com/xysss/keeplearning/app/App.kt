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
import com.xysss.jetpackmvvm.BuildConfig
import com.xysss.jetpackmvvm.base.BaseApp
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
    companion object{
        lateinit var instance:App
        lateinit var context: Context
        lateinit var eventViewModelInstance: EventViewModel
        lateinit var appViewModelInstance: AppViewModel
    }

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

        //目录：/data/data/包名/files/mmkv
        MMKV.initialize(this.filesDir.absolutePath + "/mmkv")
        instance=this

        context=applicationContext
        //当您的应用及其引用的库包含的方法数超过 65536 时，您会遇到一个构建错误，指明您的应用已达到 Android 构建架构规定的引用限制：
        //为方法数超过 64K 的应用启用 MultiDex
        MultiDex.install(this)
        //界面加载管理 初始化
        LoadSir.beginBuilder()
                .addCallback(LoadingCallback())//加载
                .addCallback(ErrorCallback())//错误
                .addCallback(EmptyCallback())//空
                .setDefaultCallback(SuccessCallback::class.java)//设置默认加载状态页
                .commit()

        val context = applicationContext

        // 设置是否为上报进程
        val strategy = CrashReport.UserStrategy(context)
        strategy.isUploadProcess = processName == null || processName == packageName

        // 初始化Bugly
        Bugly.init(context,"f874442b1e", BuildConfig.DEBUG)
        //防止项目崩溃，崩溃后打开错误界面
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true)//是否启用CustomActivityOnCrash崩溃拦截机制 必须启用！不然集成这个库干啥？？？
            .showErrorDetails(true) //是否必须显示包含错误详细信息的按钮 default: true
            .showRestartButton(true) //是否必须显示“重新启动应用程序”按钮或“关闭应用程序”按钮default: true
            .logErrorOnRestart(true) //是否必须重新堆栈堆栈跟踪 default: true
            .trackActivities(true) //是否必须跟踪用户访问的活动及其生命周期调用 default: false
            .minTimeBetweenCrashesMs(2000) //应用程序崩溃之间必须经过的时间 default: 3000
            .restartActivity(WelcomeActivity::class.java) // 重启的activity
            .errorActivity(ErrorActivity::class.java) //发生错误跳转的activity
            .apply()
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