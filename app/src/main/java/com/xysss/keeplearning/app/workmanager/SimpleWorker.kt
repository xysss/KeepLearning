package com.xysss.keeplearning.app.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xysss.mvvmhelper.ext.logD

/**
 * Author:bysd-2
 * Time:2021/10/1115:41
 */
class SimpleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        logD("do work in SimpleWorker")
        return Result.success()
    }

}