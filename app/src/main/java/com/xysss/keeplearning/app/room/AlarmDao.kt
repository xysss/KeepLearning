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

    @Query("select * from Alarm order by id limit :size offset :index")
    fun loadLimitAlarm(size:Int,index: Int): List<Alarm>

    @Delete
    fun deleteAlarm(alarm: Alarm)

    @Query("delete from Alarm where id = :voc_index")
    fun deleteAlarmByLastName(voc_index: String): Int
}