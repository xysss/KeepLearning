package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:41
 * 描述 : 描述
 */

@Dao
interface MatterDao {
    //OnConflictStrategy.REPLACE表示插入的时候有该数据的情况下会跳过
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMatter(matter: Matter): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMatterList(matter: List<Matter>): List<Long>

    @Update
    fun updateMatter(newMatter: Matter)

    @Query("select * from Matter")
    fun loadAllMatter(): List<Matter>

    @Query("select * from Matter order by mId limit :size offset :index")
    fun loadLimitMatter(size:Int,index: Int): List<Matter>

    @Delete
    fun deleteMatter(matter: Matter)

    @Query("delete from Matter where mId = :voc_index")
    fun deleteMatterByLastName(voc_index: String): Int

    //查询表中某单词是否存在  存在返回值为1，不存在返回值为0.
    @Query("select 1 from Matter where voc_index_matter =:index  limit 1")
    fun forgetMatterIsExist(index:Int):Int
}