package com.xysss.keeplearning.viewmodel.state

import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.callback.databind.IntObservableField
import com.xysss.jetpackmvvm.callback.databind.StringObservableField
import com.xysss.keeplearning.app.util.ColorUtil

/**
 * Author:bysd-2
 * Time:2021/9/2313:47
 */
class MeViewModel :BaseViewModel(){

    var name = StringObservableField("请先登录~")

    var integral = IntObservableField(0)

    var info = StringObservableField("id：--　排名：-")

    var imageUrl = StringObservableField(ColorUtil.randomImage())

}