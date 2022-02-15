package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.DataRecordResponse
import com.xysss.mvvmhelper.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 作者 : xys
 * 时间 : 2022-02-15 11:35
 * 描述 : 描述
 */
class HistoryAlarmViewModel :BaseViewModel(){
    val recordListData: LiveData<DataRecordResponse<Any>> get() = _recordListData
    private var pageIndex = 1
    private var _recordListData = MutableLiveData<DataRecordResponse<Any>>()
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
}