package com.xysss.jetpackmvvm.ext

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.network.AppException
import com.xysss.jetpackmvvm.network.BaseResponse
import com.xysss.jetpackmvvm.network.ExceptionHandle
import com.xysss.jetpackmvvm.state.ResultState
import com.xysss.jetpackmvvm.state.paresException
import com.xysss.jetpackmvvm.state.paresResult
import kotlinx.coroutines.*

/**
 * Author:bysd-2
 * Time:2021/9/2417:56
 */

fun Fragment.nav(): NavController {
    return NavHostFragment.findNavController(this)
}

fun nav(view: View): NavController {
    return Navigation.findNavController(view)
}

var lastNavTime = 0L

/**
 * 防止短时间内多次快速跳转Fragment出现的bug
 * @param resId 跳转的action Id
 * @param bundle 传递的参数
 * @param interval 多少毫秒内不可重复点击 默认0.5秒
 */
fun NavController.navigateAction(resId: Int, bundle: Bundle? = null, interval: Long = 500) {
    val currentTime = System.currentTimeMillis()
    if (currentTime >= lastNavTime + interval) {
        lastNavTime = currentTime
        try {
            navigate(resId, bundle)
        }catch (ignore:Exception){
            //防止出现 当 fragment 中 action 的 duration设置为 0 时，连续点击两个不同的跳转会导致如下崩溃 #issue53
        }
    }
}

