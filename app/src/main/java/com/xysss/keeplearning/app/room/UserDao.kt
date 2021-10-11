package com.xysss.keeplearning.app.room

import androidx.room.*

/**
 * Author:bysd-2
 * Time:2021/10/1115:21
 */
@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(newUser: User)

    @Query("select * from User")
    fun loadAllUsers(): List<User>

    @Query("select * from User where age > :age")
    fun loadUsersOlderThan(age: Int): List<User>

    @Delete
    fun deleteUser(user: User)

    @Query("delete from User where firstName = :firstName")
    fun deleteUserByLastName(firstName: String): Int

}