package com.xysss.keeplearning.app

import android.view.Gravity
import androidx.multidex.MultiDex
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.effective.android.anchors.Project
import com.effective.android.anchors.Task
import com.hjq.toast.ToastUtils
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadSir
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.api.NetHttpClient
import com.xysss.keeplearning.ui.activity.ErrorActivity
import com.xysss.keeplearning.ui.activity.SplashActivity
import com.xysss.mvvmhelper.BuildConfig
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.dp
import com.xysss.mvvmhelper.ext.getColorExt
import com.xysss.mvvmhelper.util.mvvmHelperLog
import com.xysss.mvvmhelper.widget.BaseEmptyCallback
import com.xysss.mvvmhelper.widget.BaseErrorCallback
import com.xysss.mvvmhelper.widget.BaseLoadingCallback
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.param.RxHttp
import java.util.*


/**
 * Author:bysd-2
 * Time:2021/9/2716:13
 */


object TaskCreator : com.effective.android.anchors.TaskCreator {
    override fun createTask(taskName: String): Task {
        return when (taskName) {
            InitNetWork.TASK_ID -> InitNetWork()
            InitComm.TASK_ID -> InitComm()
            InitUtils.TASK_ID -> InitUtils()
            InitToast.TASK_ID -> InitToast()
            else -> InitDefault()
        }
    }
}

class InitDefault : Task(TASK_ID, true) {
    companion object {
        const val TASK_ID = "0"
    }
    override fun run(name: String) {

    }
}


/**
 * 初始化网络
 */
//构建一个 Task, 第一个参数指定 name,也是唯一 id，第二个参数指定该 Task 是否异步运行
class InitNetWork : Task(TASK_ID, true) {
    companion object {
        const val TASK_ID = "1"
    }
    override fun run(name: String) {
        //传入自己的OKHttpClient 并添加了自己的拦截器
        RxHttpPlugins.init(NetHttpClient.getDefaultOkHttpClient().build())
    }
}



//初始化常用控件类
class InitComm : Task(TASK_ID, true) {
    companion object {
        const val TASK_ID = "2"
    }

    override fun run(name: String) {
        /**
         * SmartRefreshLayout 上下拉刷新加载更多的全局配置
         */
        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
            //设置 SmartRefreshLayout 通用配置
            layout.setEnableScrollContentWhenLoaded(true)//是否在加载完成时滚动列表显示新的内容
            layout.setFooterTriggerRate(0.6f)
        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            //设置 Head
            ClassicsHeader(context).apply {
                setAccentColor(getColorExt(R.color.colorBlack))
            }
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            //设置 Footer
            ClassicsFooter(context).apply {
                setAccentColor(getColorExt(R.color.colorBlack))
            }
        }

        //注册界面状态管理
        LoadSir.beginBuilder()
            .addCallback(BaseErrorCallback())
            .addCallback(BaseEmptyCallback())
            .addCallback(BaseLoadingCallback())
            .setDefaultCallback(SuccessCallback::class.java)
            .commit()

        //当您的应用及其引用的库包含的方法数超过 65536 时，您会遇到一个构建错误，指明您的应用已达到 Android 构建架构规定的引用限制：
        //为方法数超过 64K 的应用启用 MultiDex
        MultiDex.install(appContext)

        // 设置是否为上报进程
        /*val strategy = CrashReport.UserStrategy(appContext)
        strategy.isUploadProcess = processName == null || processName == packageName*/
        // 初始化Bugly
        Bugly.init(appContext,"f874442b1e", BuildConfig.DEBUG)

        //防止项目崩溃，崩溃后打开错误界面
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true)//是否启用CustomActivityOnCrash崩溃拦截机制 必须启用！不然集成这个库干啥？？？
            .showErrorDetails(true) //是否必须显示包含错误详细信息的按钮 default: true
            .showRestartButton(true) //是否必须显示“重新启动应用程序”按钮或“关闭应用程序”按钮default: true
            .logErrorOnRestart(true) //是否必须重新堆栈堆栈跟踪 default: true
            .trackActivities(true) //是否必须跟踪用户访问的活动及其生命周期调用 default: false
            .minTimeBetweenCrashesMs(2000) //应用程序崩溃之间必须经过的时间 default: 3000
            .restartActivity(SplashActivity::class.java) // 重启的activity
            .errorActivity(ErrorActivity::class.java) //发生错误跳转的activity
            .apply()
    }
}

//初始化Utils
class InitUtils : Task(TASK_ID, true) {
    companion object {
        const val TASK_ID = "3"
    }

    override fun run(name: String) {
        //初始化Log打印
        MMKV.initialize(appContext)
        //框架全局打印日志开关
        //mvvmHelperLog = BuildConfig.DEBUG
    }
}

//初始化Utils
class InitToast : Task(TASK_ID, false) {
    companion object {
        const val TASK_ID = "4"
    }

    override fun run(name: String) {
        //初始化吐司 这个吐司必须要主线程中初始化
        ToastUtils.init(appContext)
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 100.dp)
    }
}

class AppTaskFactory : Project.TaskFactory(TaskCreator)

/**
 * 模拟初始化SDK
 * @param millis Long
 */
fun doJob(millis: Long) {
    val nowTime = System.currentTimeMillis()
    while (System.currentTimeMillis() < nowTime + millis) {
        //程序阻塞指定时间
        val min = 10
        val max = 99
        val random = Random()
        val num = random.nextInt(max) % (max - min + 1) + min
    }
}