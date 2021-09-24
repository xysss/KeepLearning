package com.xysss.keeplearning.app.event

import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.callback.livedata.event.EventLiveData
import com.xysss.keeplearning.data.model.bean.CollectBus

/**
 * Author:bysd-2
 * Time:2021/9/2218:17
 * 描述　:APP全局的ViewModel，可以在这里发送全局通知替代EventBus，LiveDataBus等
 */

class EventViewModel : BaseViewModel() {
    //全局收藏，在任意一个地方收藏或取消收藏，监听该值的界面都会收到消息
    val collectEvent = EventLiveData<CollectBus>()

    //分享文章通知
    val shareArticleEvent = EventLiveData<Boolean>()

    //添加TODO通知
    val todoEvent = EventLiveData<Boolean>()
}