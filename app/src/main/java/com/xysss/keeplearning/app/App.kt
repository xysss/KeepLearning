package com.xysss.keeplearning.app

import android.app.Application
import com.effective.android.anchors.AnchorsManager
import com.effective.android.anchors.Project
import com.xysss.keeplearning.BuildConfig
import com.xysss.mvvmhelper.ext.currentProcessName


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
        //支持同异步依赖任务初始化 Android 启动框架
        //如果一个任务要确保在 application#onCreate 前执行完毕，则该任务成为锚点任务
        AnchorsManager.getInstance()
            .debuggable(BuildConfig.DEBUG)
            //传递任务 id 设置锚点任务
            .addAnchor(InitNetWork.TASK_ID, InitUtils.TASK_ID, InitComm.TASK_ID, InitToast.TASK_ID).start(
                Project.Builder("app", AppTaskFactory())  //可选，构建依赖图可以使用工厂，
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