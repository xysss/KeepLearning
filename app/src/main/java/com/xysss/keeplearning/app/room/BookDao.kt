package com.xysss.keeplearning.app.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Author:bysd-2
 * Time:2021/10/1115:24
 */
@Dao
interface BookDao {

    @Insert
    fun insertBook(book: Book): Long

    @Query("select * from Book")
    fun loadAllBooks(): List<Book>

}