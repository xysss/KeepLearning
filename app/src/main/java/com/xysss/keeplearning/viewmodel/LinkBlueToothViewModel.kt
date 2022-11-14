package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.ext.scope
import com.xysss.keeplearning.data.response.AppVersionResponse
import com.xysss.mvvmhelper.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rxhttp.toClass
import rxhttp.wrapper.param.RxHttp

class LinkBlueToothViewModel : BaseViewModel(){
    val appVersionInfo: LiveData<AppVersionResponse> get() = _appVersionInfo
    private var _appVersionInfo= MutableLiveData<AppVersionResponse>()

    fun checkVersion(){
        scope.launch(Dispatchers.IO) {
            _appVersionInfo.postValue(getAppVersionInfo())
        }
    }

    private suspend fun getAppVersionInfo(): AppVersionResponse {
        return RxHttp.get("access/integration/versioninfo/Vp200App")
            .toClass<AppVersionResponse>()
            .await()
    }
}