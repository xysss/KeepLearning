package com.xysss.keeplearning.app.room

import androidx.room.*
import com.xysss.keeplearning.data.response.JoinResult

/**
 * 作者 : xys
 * 时间 : 2022-02-10 10:42
 * 描述 : 描述
 */
@Dao
interface RecordDao {
    @Insert
    fun insertRecord(record: Record): Long

    @Insert
    fun insertRecordList(record: List<Record>): List<Long>

    @Update
    fun updateRecord(newRecord: Record)

    @Query("select * from Record")
    fun loadAllRecord(): List<Record>

    @Query("select * from Record order by id limit :size offset :index")
    fun loadLimitRecord(size:Int,index: Int): List<Record>

    //使用内连接查询
    @Query("select id,timestamp,cf,alarm,userId,placeId,matterName from Record left join Matter on Record.voc_index=Matter.voc_index order by id limit :size offset :index")
    fun leftJoinLoadLimitRecord(size:Int,index: Int): List<JoinResult>

    @Delete
    fun deleteRecord(record: Record)

    @Query("delete from Record where id = :voc_index")
    fun deleteRecordByLastName(voc_index: String): Int
}