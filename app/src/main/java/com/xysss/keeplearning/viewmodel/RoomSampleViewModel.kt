package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.User
import com.xysss.keeplearning.app.room.UserDao
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import kotlin.concurrent.thread

/**
 * Author:bysd-2
 * Time:2021/10/1114:18
 */
class RoomSampleViewModel : BaseViewModel() {

    val userList: LiveData<List<User>> get() = _userList

    private val userDao = AppDatabase.getDatabase().userDao()
    private val user1 = User("Tom", "Brady", 40)
    private val user2 = User("Tom", "Hanks", 63)

    private val _userList = MutableLiveData<List<User>>()
//    init {
//        _userList.value=
//    }
    fun loadUsers() {
        thread {
            _userList.postValue(userDao.loadUsersOlderThan(45))
        }
    }
    fun updateUser(){
        thread {
            user1.age = 13
            userDao.updateUser(user1)
        }
    }
    fun insertUser(){
        thread {
            userDao.insertUser(user1)
            userDao.insertUser(user2)
        }
    }
    fun deleteUserByLastName(){
        thread {
            userDao.deleteUserByLastName("Tom")
        }
    }
    fun loadAllUsers(){
        thread {
            _userList.postValue(userDao.loadAllUsers())
        }
    }

}