package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.network.ListDataUiState
import com.xysss.keeplearning.data.repository.UserRepository
import com.xysss.keeplearning.data.response.ApiProjectTitleData
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.rxHttpRequest

/**
 * Author:bysd-2
 * Time:2021/10/1815:36
 */
class RequestProjectViewModel : BaseViewModel() {

    //页码
    var pageIndex = 0

    var titleData: MutableLiveData<ApiProjectTitleData<ClassifyResponse>> = MutableLiveData()

    var projectDataState: MutableLiveData<ListDataUiState<AriticleResponse>> = MutableLiveData()

    fun getList() {
        rxHttpRequest {
            onRequest = {
                titleData.value = UserRepository.getProjectTitleData().await()
            }
        }
    }

}