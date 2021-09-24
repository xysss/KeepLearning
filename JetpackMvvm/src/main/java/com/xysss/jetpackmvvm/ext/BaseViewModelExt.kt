package com.xysss.jetpackmvvm.ext

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
 * Time:2021/9/2417:54
 * 描述　:BaseViewModel请求协程封装
 */
