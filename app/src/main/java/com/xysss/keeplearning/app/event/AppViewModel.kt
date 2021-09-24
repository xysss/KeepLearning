package com.xysss.keeplearning.app.event

import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.xysss.jetpackmvvm.base.appContext
import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.callback.livedata.event.EventLiveData
import com.xysss.keeplearning.app.util.CacheUtil
import com.xysss.keeplearning.app.util.SettingUtil
import com.xysss.keeplearning.data.model.bean.UserInfo

/**
 * Author:bysd-2
 * Time:2021/9/2218:09
 * 描述　:APP全局的ViewModel，可以存放公共数据，当他数据改变时，所有监听他的地方都会收到回调,也可以做发送消息
 * 比如 全局可使用的 地理位置信息，账户信息,App的基本配置等等，
 */
class AppViewModel :BaseViewModel(){
    //App的账户信息
    var userInfo = UnPeekLiveData.Builder<UserInfo>().setAllowNullValue(true).create()

    //App主题颜色 中大型项目不推荐以这种方式改变主题颜色，比较繁琐耦合，且容易有遗漏某些控件没有设置主题色
    var appColor = EventLiveData<Int>()

    //App 列表动画
    var appAnimation = EventLiveData<Int>()

    init {
        //默认值保存的账户信息，没有登陆过则为null
        userInfo.value = CacheUtil.getUser()
        //默认值颜色
        appColor.value = SettingUtil.getColor(appContext)
        //初始化列表动画
        appAnimation.value = SettingUtil.getListMode()
    }
}