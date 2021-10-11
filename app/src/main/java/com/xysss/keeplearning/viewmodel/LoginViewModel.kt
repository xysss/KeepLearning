package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.data.repository.UserRepository
import com.xysss.keeplearning.data.response.UserInfo
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.core.databinding.BooleanObservableField
import com.xysss.mvvmhelper.core.databinding.StringObservableField
import com.xysss.mvvmhelper.ext.logA
import com.xysss.mvvmhelper.ext.rxHttpRequest
import com.xysss.mvvmhelper.net.LoadingType
import rxhttp.async

/**
 * Author:bysd-2
 * Time:2021/9/2811:12
 */

class LoginViewModel : BaseViewModel() {

    //登录请求信息
    val loginData = MutableLiveData<UserInfo>()

    /**
     * 登录
     * @param phoneNumber String
     * @param password String
     */
    fun login(phoneNumber: String, password: String) {
        rxHttpRequest {
            onRequest = {
                loginData.value = UserRepository.login(phoneNumber,password).await()
            }
            loadingType = LoadingType.LOADING_DIALOG //选传
            loadingMessage = "正在登录中....." // 选传
            requestCode = NetUrl.LOGIN // 如果要判断接口错误业务 - 必传
        }
    }

    /**
     * 演示一个并行 请求 写法
     * @param phoneNumber String
     * @param password String
     */
    fun test(phoneNumber: String, password: String) {
        rxHttpRequest {
            onRequest = {
                //下面2个接口同时请求，最后合并值 其中有任一接口请求失败都会走错误回调
                val listData = UserRepository.getList(0).async(this)
                val loginData = UserRepository.login(phoneNumber, password).async(this)
                //得到合并值
                val mergeValue = loginData.await().username + listData.await().hasMore()
                //打印一下
                mergeValue.logA()
            }
            loadingType = LoadingType.LOADING_DIALOG
            loadingMessage = "正在登录中....." // 选传
            requestCode = NetUrl.LOGIN // 如果要判断接口错误业务 - 必传
        }
    }

}