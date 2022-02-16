package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:41
 * 描述 : 描述
 */

@Dao
interface MatterDao {
    @Insert
    fun insertMatter(matter: Matter): Long

    @Update
    fun updateMatter(newMatter: Matter)

    @Query("select * from Matter")
    fun loadAllMatter(): List<Matter>

    @Query("select * from Matter order by id limit :size offset :index")
    fun loadLimitMatter(size:Int,index: Int): List<Matter>

    @Delete
    fun deleteMatter(matter: Matter)

    @Query("delete from Matter where id = :voc_index")
    fun deleteMatterByLastName(voc_index: String): Int
}