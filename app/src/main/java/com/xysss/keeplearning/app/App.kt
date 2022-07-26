package com.xysss.keeplearning.app

import android.app.Application
import android.content.SharedPreferences
import com.baidu.trace.LBSTraceClient
import com.baidu.trace.Trace
import com.baidu.trace.api.entity.LocRequest
import com.baidu.trace.api.entity.OnEntityListener
import com.baidu.trace.api.track.LatestPointRequest
import com.baidu.trace.api.track.OnTrackListener
import com.baidu.trace.model.BaseRequest
import com.baidu.trace.model.ProcessOption
import com.baidu.trace.model.TransportMode
import com.effective.android.anchors.AnchorsManager
import com.effective.android.anchors.Project
import com.xysss.keeplearning.BuildConfig
import com.xysss.keeplearning.app.util.NetUtil
import com.xysss.mvvmhelper.base.MvvmHelper
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.currentProcessName
import java.util.concurrent.atomic.AtomicInteger


/**
 * Author:bysd-2
 * Time:2021/9/1415:02
 */

class App: Application() {

    companion object  {
        @JvmField
        val mSequenceGenerator = AtomicInteger()
        @JvmField
        var locRequest: LocRequest? = null
        @JvmField
        var trackConf: SharedPreferences? = null
        /**
         * 轨迹客户端
         */
        @JvmField
        var mClient: LBSTraceClient? = null
        /**
         * 轨迹服务
         */
        @JvmField
        var mTrace: Trace? = null
        /**
         * 轨迹服务ID
         */
        @JvmField
        var serviceId: Long = 0 //这里是申请的鹰眼服务id
        /**
         * Entity标识
         */
        @JvmField
        var entityName = "myTrace"
        @JvmField
        var isRegisterReceiver = false
        /**
         * 服务是否开启标识
         */
        @JvmField
        var isTraceStarted = false
        /**
         * 采集是否开启标识
         */
        @JvmField
        var isGatherStarted = false
        @JvmField
        var screenWidth = 0
        @JvmField
        var screenHeight = 0

        fun initRequest(request: BaseRequest) {
            request.setTag(getTag())
            request.setServiceId(serviceId)
        }


        /**
         * 获取屏幕尺寸
         */
        fun getScreenSize() {
            val dm = appContext.resources.displayMetrics
            screenHeight = dm.heightPixels
            screenWidth = dm.widthPixels
        }

        /**
         * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
         *
         * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
         */
        fun clearTraceStatus() {
            if (trackConf!!.contains("is_trace_started") || trackConf!!.contains("is_gather_started")) {
                val editor = trackConf!!.edit()
                editor.remove("is_trace_started")
                editor.remove("is_gather_started")
                editor.apply()
            }
        }

        /**
         * 获取请求标识
         *
         * @return
         */
        fun getTag(): Int {
            return mSequenceGenerator.incrementAndGet()
        }


        /**
         * 获取当前位置
         */
        fun getCurrentLocation(entityListener: OnEntityListener?, trackListener: OnTrackListener?) {
            // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
            if (NetUtil.isNetworkAvailable(appContext)
                && trackConf!!.contains("is_trace_started")
                && trackConf!!.contains("is_gather_started")
                && trackConf!!.getBoolean("is_trace_started", false)
                && trackConf!!.getBoolean("is_gather_started", false)
            ) {
                val request = LatestPointRequest(getTag(), serviceId, entityName)
                val processOption = ProcessOption()
                processOption.radiusThreshold = 50
                processOption.transportMode = TransportMode.walking
                processOption.isNeedDenoise = true
                processOption.isNeedMapMatch = true
                request.processOption = processOption
                mClient!!.queryLatestPoint(request, trackListener)
            } else {
                mClient!!.queryRealTimeLoc(locRequest, entityListener)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        MvvmHelper.init(this,BuildConfig.DEBUG)
        // 获取当前进程名
        val processName = currentProcessName
        if (currentProcessName == packageName) {
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
            .addAnchor(InitNetWork.TASK_ID, InitUtils.TASK_ID, InitComm.TASK_ID, InitToast.TASK_ID,InitBaiDuMap.TASK_ID).start(
                Project.Builder("app", AppTaskFactory())  //可选，构建依赖图可以使用工厂，
                    .add(InitNetWork.TASK_ID)
                    .add(InitComm.TASK_ID)
                    .add(InitUtils.TASK_ID)
                    .add(InitToast.TASK_ID)
                    .add(InitBaiDuMap.TASK_ID)
                    .build()
            )
    }

    /**
     * 其他进程初始化，[processName] 进程名
     */
    private fun onOtherProcessInit(processName: String) {}
}

/**
 * 初始化请求公共参数
 *
 * @param request
 */
fun initRequest(request: BaseRequest) {
    request.setTag(App.getTag())
    request.setServiceId(App.serviceId)
}