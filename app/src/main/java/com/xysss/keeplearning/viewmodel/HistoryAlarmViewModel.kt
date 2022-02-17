package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.DataHistoryResponse
import com.xysss.mvvmhelper.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * 作者 : xys
 * 时间 : 2022-02-15 11:35
 * 描述 : 描述
 */
class HistoryAlarmViewModel :BaseViewModel(){
    val alarmListData: LiveData<DataHistoryResponse<Any>> get() = _alarmListData
    private var pageIndex = 0
    private val readNum=20
    private var _alarmListData = MutableLiveData<DataHistoryResponse<Any>>()
    fun getAlarmList(isRefresh: Boolean, loadingXml: Boolean = false) {
        if (isRefresh) {
            pageIndex = 0
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dataRsp= DataHistoryResponse(Repository.getAlarmList(readNum,pageIndex*readNum) as ArrayList<Any>,isRefresh,false)
            _alarmListData.postValue(dataRsp)
            //请求成功 页码+1
            pageIndex++
        }
    }
}