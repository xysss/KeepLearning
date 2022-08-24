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
 * 时间 : 2022-08-24 13:52
 * 描述 : 描述
 */
class HistorySurveyViewModel : BaseViewModel(){
    val surveyListData: LiveData<DataHistoryResponse<Any>> get() = _surveyListData
    private var pageIndex = 0
    private val readNum=20
    private var _surveyListData = MutableLiveData<DataHistoryResponse<Any>>()
    fun getSurveyList(isRefresh: Boolean, loadingXml: Boolean = false) {
        if (isRefresh) {
            pageIndex = 0
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dataRsp= DataHistoryResponse(Repository.getSurveyList(readNum,pageIndex*readNum) as ArrayList<Any>,isRefresh,false)
            //val dataRsp= DataHistoryResponse(Repository.getRecordList(readNum,pageIndex*readNum) as ArrayList<Any>,isRefresh,false)
            _surveyListData.postValue(dataRsp)
            //请求成功 页码+1
            pageIndex++
        }
    }
}