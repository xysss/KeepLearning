package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.room.User
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * Author:bysd-2
 * Time:2021/10/1114:18
 */
class RoomSampleViewModel : BaseViewModel() {
    val userList = MutableLiveData<List<User>>()
}