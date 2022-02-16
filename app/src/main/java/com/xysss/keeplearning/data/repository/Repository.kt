package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.ext.dataAlarmDao
import com.xysss.keeplearning.app.ext.dataRecordDao
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Record

/**
 * Author:bysd-2
 * Time:2021/10/1116:05
 */
object Repository {

    fun getRecordList(size:Int,index: Int): List<Record>{
        return dataRecordDao.loadLimitRecord(size, index)
    }
    fun getAlarmList(size:Int,index: Int): List<Alarm>{
        return dataAlarmDao.loadLimitAlarm(size, index)
    }

//    fun getUser(userId: String): LiveData<User> {
//        val liveData = MutableLiveData<User>()
//        liveData.value = User(userId, userId, 0)
//        return liveData
//    }

}