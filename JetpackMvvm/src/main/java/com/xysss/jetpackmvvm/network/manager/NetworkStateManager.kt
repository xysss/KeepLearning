package com.xysss.jetpackmvvm.network.manager

import com.xysss.jetpackmvvm.callback.livedata.event.EventLiveData

/**
 * Author:bysd-2
 * Time:2021/9/1615:48
 * 描述　: 网络变化管理者
 */
class NetworkStateManager private constructor(){
    val mNetworkStateCallback = EventLiveData<NetState>()
    companion object{
        val instance: NetworkStateManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkStateManager()
        }
    }
}