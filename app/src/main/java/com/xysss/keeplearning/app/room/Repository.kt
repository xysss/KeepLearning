package com.xysss.keeplearning.app.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Author:bysd-2
 * Time:2021/10/1116:05
 */
object Repository {

    fun getUser(userId: String): LiveData<User> {
        val liveData = MutableLiveData<User>()
        liveData.value = User(userId, userId, 0)
        return liveData
    }

}