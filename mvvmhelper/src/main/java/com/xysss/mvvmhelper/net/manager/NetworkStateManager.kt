package com.xysss.mvvmhelper.net.manager

import com.kunminx.architecture.ui.callback.UnPeekLiveData


/**
 * Author:bysd-2
 * Time:2021/9/1615:48
 * 描述　: 网络变化管理者
 */
class NetworkStateManager private constructor(){
    val mNetworkStateCallback = UnPeekLiveData<NetState>()
    companion object{
        val instance: NetworkStateManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkStateManager()
        }
    }
}