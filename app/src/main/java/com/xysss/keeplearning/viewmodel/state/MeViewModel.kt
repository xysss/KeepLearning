package com.xysss.keeplearning.viewmodel.state


import com.xysss.keeplearning.app.util.ColorUtil
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.core.databinding.IntObservableField
import com.xysss.mvvmhelper.core.databinding.StringObservableField

/**
 * Author:bysd-2
 * Time:2021/9/2313:47
 */
class MeViewModel : BaseViewModel(){

    var name = StringObservableField("请先登录~")

    var integral = IntObservableField(0)

    var info = StringObservableField("id：--　排名：-")

    var imageUrl = StringObservableField(ColorUtil.randomImage())

}