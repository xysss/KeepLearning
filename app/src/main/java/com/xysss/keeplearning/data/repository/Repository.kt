package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.ext.dataAlarmDao
import com.xysss.keeplearning.app.ext.dataMatterDao
import com.xysss.keeplearning.app.ext.dataRecordDao
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Matter
import com.xysss.keeplearning.data.response.JoinResult
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
    fun getJoinResultList(size:Int,index: Int): List<JoinResult>{
        return dataRecordDao.leftJoinLoadLimitRecord(size, index)
    }
    fun insertRecordList(recordList:ArrayList<Record>):List<Long>{
        return dataRecordDao.insertRecordList(recordList)
    }
    fun insertAlarm(alarm:Alarm):Long{
        return dataAlarmDao.insertAlarm(alarm)
    }
    fun insertAlarmList(alarmList:ArrayList<Alarm>):List<Long>{
        return dataAlarmDao.insertAlarmList(alarmList)
    }
    fun insertMatterList(matterList: ArrayList<Matter>):List<Long>{
        return dataMatterDao.insertMatterList(matterList)
    }
    fun insertMatter(matter:Matter):Long{
        return dataMatterDao.insertMatter(matter)
    }

//    fun getUser(userId: String): LiveData<User> {
//        val liveData = MutableLiveData<User>()
//        liveData.value = User(userId, userId, 0)
//        return liveData
//    }

}