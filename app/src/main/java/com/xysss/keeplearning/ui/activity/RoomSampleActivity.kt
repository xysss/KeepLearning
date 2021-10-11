package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.*
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.User
import com.xysss.keeplearning.app.workmanager.SimpleWorker
import com.xysss.keeplearning.databinding.FragmentRoomBinding
import com.xysss.keeplearning.viewmodel.RoomSampleViewModel
import com.xysss.mvvmhelper.ext.loadListSuccess
import com.xysss.mvvmhelper.ext.logD
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Author:bysd-2
 * Time:2021/10/1114:13
 */
class RoomSampleActivity : BaseActivity<RoomSampleViewModel, FragmentRoomBinding>() {
    override fun initView(savedInstanceState: Bundle?) {

        mViewModel.userList.observe(this, Observer { userList->
            mViewBinding.infoText.text = ""
            mViewBinding.infoText.text = userList.toString()
            Log.d("aaaa",userList.toString())
        })

        val userDao = AppDatabase.getDatabase(this).userDao()
        val user1 = User("Tom", "Brady", 40)
        val user2 = User("Tom", "Hanks", 63)
        mViewBinding.addDataBtn.setOnClickListener {
            thread {
                userDao.insertUser(user1)
                userDao.insertUser(user2)
            }
        }
        mViewBinding.updateDataBtn.setOnClickListener {
            thread {
                user1.age = 13
                userDao.updateUser(user1)
            }
        }

        mViewBinding.getUserBtn.setOnClickListener {
            thread {
                mViewModel.userList.postValue(userDao.loadUsersOlderThan(45))
            }
        }

        mViewBinding.deleteDataBtn.setOnClickListener {
            thread {
                userDao.deleteUserByLastName("Tom")
            }
        }
        mViewBinding.queryDataBtn.setOnClickListener {
            thread {
                mViewModel.userList.postValue(userDao.loadAllUsers())
            }
        }

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
                        logD("do work succeeded")
                    }else if (workInfo.state==WorkInfo.State.FAILED){
                        logD("do work failed")
                    }
                }

        }



    }
}


