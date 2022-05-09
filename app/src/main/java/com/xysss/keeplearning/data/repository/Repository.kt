package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.ext.dataAlarmDao
import com.xysss.keeplearning.app.ext.dataMatterDao
import com.xysss.keeplearning.app.ext.dataRecordDao
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Matter
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.data.response.JoinResult

/**
 * Author:bysd-2
 * Time:2021/10/1116:05
 */
object Repository {

    @Synchronized
    fun insertRecord(record:Record):Long{
       return dataRecordDao.insertRecord(record)
    }
    @Synchronized
    fun insertRecordList(recordList:ArrayList<Record>):List<Long>{
        return dataRecordDao.insertRecordList(recordList)
    }
    @Synchronized
    fun getRecordList(size:Int,index: Int): List<Record>{
        return dataRecordDao.loadLimitRecord(size, index)
    }
    @Synchronized
    fun getAllRecordList(): List<Record>{
        return dataRecordDao.loadAllRecord()
    }
    @Synchronized
    fun getJoinResultList(size:Int,index: Int): List<JoinResult>{
        return dataRecordDao.leftJoinLoadLimitRecord(size, index)
    }
    @Synchronized
    fun forgetRecordIsExist(time :String):Int{
        return dataRecordDao.forgetRecordIsExist(time)
    }
    @Synchronized
    fun deleteAllRecords(){
        return dataRecordDao.deleteAllRecords()
    }

    @Synchronized
    fun insertAlarm(alarm:Alarm):Long{
        return dataAlarmDao.insertAlarm(alarm)
    }
    @Synchronized
    fun insertAlarmList(alarmList:ArrayList<Alarm>):List<Long>{
        return dataAlarmDao.insertAlarmList(alarmList)
    }
    @Synchronized
    fun getAlarmList(size:Int,index: Int): List<Alarm>{
        return dataAlarmDao.loadLimitAlarm(size, index)
    }
    @Synchronized
    fun forgetAlarmIsExist(time :String):Int{
        return dataAlarmDao.forgetAlarmIsExist(time)
    }
    @Synchronized
    fun deleteAllAlarm(){
        return dataAlarmDao.deleteAllAlarm()
    }

    @Synchronized
    fun insertMatter(matter:Matter):Long{
        return dataMatterDao.insertMatter(matter)
    }
    @Synchronized
    fun insertMatterList(matterList: ArrayList<Matter>):List<Long>{
        return dataMatterDao.insertMatterList(matterList)
    }
    @Synchronized
    fun forgetMatterIsExist(index:Int):Int{
        return dataMatterDao.forgetMatterIsExist(index)
    }

//    fun getUser(userId: String): LiveData<User> {
//        val liveData = MutableLiveData<User>()
//        liveData.value = User(userId, userId, 0)
//        return liveData
//    }

}