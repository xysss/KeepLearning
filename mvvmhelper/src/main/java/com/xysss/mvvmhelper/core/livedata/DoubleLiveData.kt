package com.xysss.mvvmhelper.core.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Author:bysd-2
 * Time:2021/9/2717:11
 * 描述　:自定义的Double类型 MutableLiveData 提供了默认值，避免取值的时候还要判空
 */
class DoubleLiveData : MutableLiveData<Double>() {
    override fun getValue(): Double {
        return super.getValue() ?: 0.0
    }
}