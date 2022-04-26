package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:36
 * 描述 : 描述
 */
@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAlarm(alarm: Alarm): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAlarmList(alarm: List<Alarm>): List<Long>

    @Update
    fun updateAlarm(newAlarm: Alarm)

    @Query("select * from Alarm")
    fun loadAllAlarm(): List<Alarm>

    @Query("select * from Alarm order by id desc limit :size offset :index")
    fun loadLimitAlarm(size:Int,index: Int): List<Alarm>

    @Delete
    fun deleteAlarm(alarm: Alarm)

    @Query("delete from Alarm where id = :voc_index")
    fun deleteAlarmByLastName(voc_index: String): Int

    //查询表中某单词是否存在  存在返回值为1，不存在返回值为0.
    @Query("select 1 from Alarm where timestamp =:time  limit 1")
    fun forgetAlarmIsExist(time:String):Int
}