package com.xysss.keeplearning.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.app.room.User

/**
 * Author:bysd-2
 * Time:2021/10/1116:05
 */
object Repository {

    private val dataRecordDao = AppDatabase.getDatabase().dataRecordDao()

    fun getRecordList(size:Int,index: Int): List<Record>{
        return dataRecordDao.loadLimitRecord(size, index)
    }

//    fun getUser(userId: String): LiveData<User> {
//        val liveData = MutableLiveData<User>()
//        liveData.value = User(userId, userId, 0)
//        return liveData
//    }

}