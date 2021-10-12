package com.xysss.keeplearning.app.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xysss.mvvmhelper.net.interception.logging.util.LogUtils

/**
 * Author:bysd-2
 * Time:2021/10/1115:41
 */
class SimpleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        LogUtils.debugInfo("do work in SimpleWorker")
        return Result.success()
    }

}