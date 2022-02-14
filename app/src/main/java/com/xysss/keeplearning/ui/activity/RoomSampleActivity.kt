package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import androidx.work.*
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.workmanager.SimpleWorker
import com.xysss.keeplearning.databinding.FragmentRoomBinding
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.net.interception.logging.util.LogUtils.Companion.debugInfo
import java.util.concurrent.TimeUnit

/**
 * Author:bysd-2
 * Time:2021/10/1114:13
 */
class RoomSampleActivity : BaseActivity<BaseViewModel, FragmentRoomBinding>() {
    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.doWorkBtn.setOnClickListener {
            //周期性任务 间隔不能短于15分钟
            val request1 = PeriodicWorkRequest.Builder(SimpleWorker::class.java,15,TimeUnit.MINUTES)
                //添加标签
                .addTag("simple")
                //失败后(linear线性方式)或者 (exponential指数方式)延时执行
                .setBackoffCriteria(BackoffPolicy.LINEAR,10,TimeUnit.SECONDS)
                .build()

            //单次任务
            val request = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
                .build()

            WorkManager.getInstance(this).enqueue(request)
            //取消任务
            //WorkManager.getInstance(this).cancelWorkById(request.id)
            //取消所有后台任务请求
            //WorkManager.getInstance(this).cancelAllWork()

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(request.id)
                .observe(this){workInfo->
                    if (workInfo.state==WorkInfo.State.SUCCEEDED){
                        debugInfo("do work succeeded")
                    }else if (workInfo.state==WorkInfo.State.FAILED){
                        debugInfo("do work failed")
                    }
                }
        }
    }
}


