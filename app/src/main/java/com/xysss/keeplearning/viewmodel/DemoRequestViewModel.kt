package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.data.repository.UserRepository
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.rxHttpRequest

/**
 * Author:bysd-2
 * Time:2021/12/617:48
 */
class DemoRequestViewModel : BaseViewModel(){

    var isSetDateShow = MutableLiveData<Any>()

    /**
    获取标题数据
     */
    fun setDateShow() {
        rxHttpRequest {
            onRequest = {
                isSetDateShow.value = UserRepository.setDateShow().await()
            }
        }
    }

}