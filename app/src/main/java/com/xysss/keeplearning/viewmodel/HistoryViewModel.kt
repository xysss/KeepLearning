package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.repository.UserRepository
import com.xysss.keeplearning.data.response.ApiPagerResponse
import com.xysss.keeplearning.data.response.DataRecordResponse
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.rxHttpRequest
import com.xysss.mvvmhelper.net.LoadingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 作者 : xys
 * 时间 : 2022-02-10 14:23
 * 描述 : 描述
 */
class HistoryViewModel: BaseViewModel() {
    val recordListData: LiveData<DataRecordResponse<Any>> get() = _recordListData

    private var pageIndex = 1
    private var _recordListData = MutableLiveData<DataRecordResponse<Any>>()
    //var recordListData = MutableLiveData<ArrayList<Any>>()

    fun getRecordList(isRefresh: Boolean, loadingXml: Boolean = false) {
        if (isRefresh) {
            //是刷新 玩Android的这个接口pageIndex 是0 开始 （真操蛋啊...）
            pageIndex = 0
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dataRsp= DataRecordResponse(Repository.getRecordList(20,pageIndex*20),isRefresh,false)
            _recordListData.postValue(dataRsp)
            //请求成功 页码+1
            pageIndex++
        }
    }

    /**
     * 获取列表数据
     * @param isRefresh Boolean 是否是刷新
     * @param loadingXml Boolean 请求时是否需要展示界面加载中loading
     */
//    fun getList(isRefresh: Boolean, loadingXml: Boolean = false) {
//        if (isRefresh) {
//            //是刷新 玩Android的这个接口pageIndex 是0 开始 （真操蛋啊...）
//            pageIndex = 0
//        }
//        rxHttpRequest {
//            onRequest = {
//                listData.value = UserRepository.getList(pageIndex).await()
//                //请求成功 页码+1
//                pageIndex++
//            }
//            loadingType = if (loadingXml) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
//            requestCode = NetUrl.HOME_LIST
//            isRefreshRequest = isRefresh
//        }
//    }
}