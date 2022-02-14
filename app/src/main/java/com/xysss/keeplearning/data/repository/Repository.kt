package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.data.response.DataRecordResponse

/**
 * Author:bysd-2
 * Time:2021/10/1116:05
 */
object Repository {

    private val dataRecordDao = AppDatabase.getDatabase().dataRecordDao()

    fun getRecordList(size:Int,index: Int): ArrayList<Any>{
        return dataRecordDao.loadLimitRecord(size, index)  as ArrayList<Any>
    }

//    fun getUser(userId: String): LiveData<User> {
//        val liveData = MutableLiveData<User>()
//        liveData.value = User(userId, userId, 0)
//        return liveData
//    }

}