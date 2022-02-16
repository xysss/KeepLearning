package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * 作者 : xys
 * 时间 : 2022-02-10 10:42
 * 描述 : 描述
 */
@Dao
interface RecordDao {
    @Insert
    fun insertRecord(record: Record): Long

    @Update
    fun updateRecord(newRecord: Record)

    @Query("select * from Record")
    fun loadAllRecord(): List<Record>

    @Query("select * from Record order by id limit :size offset :index")
    fun loadLimitRecord(size:Int,index: Int): List<Record>

    @Delete
    fun deleteRecord(record: Record)

    @Query("delete from Record where id = :voc_index")
    fun deleteRecordByLastName(voc_index: String): Int
}