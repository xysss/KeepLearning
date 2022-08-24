package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * 作者 : xys
 * 时间 : 2022-08-24 14:42
 * 描述 : 描述
 */
@Dao
interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertSurvey(survey: Survey): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertSurveyList(survey: List<Survey>): List<Long>

    @Update
    fun updateSurvey(newSurvey: Survey)

    @Query("select * from Survey")
    fun loadAllSurvey(): List<Survey>

    @Query("select * from Survey order by id desc limit :size offset :index")
    fun loadLimitSurvey(size:Int,index: Int): List<Survey>

    @Query("select * from Survey where beginTime = :time")
    fun getSurveyByBeginTime(time:Long): Survey

    @Delete
    fun deleteSurvey(survey: Survey)

    @Query("delete from Survey where id = :voc_index")
    fun deleteSurveyByLastName(voc_index: String): Int

    //查询表中某单词是否存在  存在返回值为1，不存在返回值为0.
    @Query("select 1 from Survey where beginTime = :time limit 1")
    fun forgetSurveyIsExist(time:Long):Int

    @Query("delete from Survey")
    fun deleteAllSurvey()
}