package com.xysss.keeplearning.viewmodel.request

import androidx.lifecycle.MutableLiveData
import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.state.ResultState
import com.xysss.keeplearning.data.model.bean.IntegralResponse

/**
 * Author:bysd-2
 * Time:2021/9/2316:00
 */
class RequestMeViewModel : BaseViewModel() {

    var meData = MutableLiveData<ResultState<IntegralResponse>>()

    /*fun getIntegral() {
        request({ apiService.getIntegral() }, meData)
    }*/
}