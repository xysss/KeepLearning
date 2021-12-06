package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.network.ListDataUiState
import com.xysss.keeplearning.data.repository.UserRepository
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.rxHttpRequest
import com.xysss.mvvmhelper.net.LoadingType

/**
 * Author:bysd-2
 * Time:2021/10/1815:36
 */
class RequestProjectViewModel : BaseViewModel() {
    //页码
    var pageIndex = 1
    var titleData = MutableLiveData<ArrayList<ClassifyResponse>>()
    var projectDataState = MutableLiveData<ListDataUiState<AriticleResponse>>()

    /**
    获取标题数据
     */
    fun getProjectTitleData() {
        rxHttpRequest {
            onRequest = {
                titleData.value = UserRepository.getProjectTitleData().await()
            }
        }
    }

    /**
     * 获取列表数据
     */
    fun getProjectData(isRefresh: Boolean, loadingXml: Boolean = false) {
        if (isRefresh) {
            //是刷新 玩Android的这个接口pageIndex 是0 开始 （真操蛋啊...）
            pageIndex = 0
        }
        rxHttpRequest {
            onRequest = {
                projectDataState.value = UserRepository.getProjectData(pageIndex).await()
                //请求成功 页码+1
                pageIndex++
            }
            loadingType = if (loadingXml) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
            requestCode = NetUrl.Project_Data
            isRefreshRequest = isRefresh
        }
    }

    /**
    获取标题数据
     */
    fun getPublicTitleData() {
        rxHttpRequest {
            onRequest = {
                titleData.value = UserRepository.getPublicTitle().await()
            }
        }
    }

    /**
    获取标题数据
     */
    fun getPublicData(isRefresh: Boolean, loadingXml: Boolean = false, id: Int) {
        if (isRefresh) {
            //是刷新 玩Android的这个接口pageIndex 是0 开始 （真操蛋啊...）
            pageIndex = 0
        }
        rxHttpRequest {
            onRequest = {
                projectDataState.value = UserRepository.getPublicData(pageIndex, id).await()
                //请求成功 页码+1
                pageIndex++
            }
            loadingType = if (loadingXml) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
            requestCode = NetUrl.Project_Data
            isRefreshRequest = isRefresh
        }
    }
}