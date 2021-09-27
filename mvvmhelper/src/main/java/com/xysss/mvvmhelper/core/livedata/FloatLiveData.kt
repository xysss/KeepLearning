package com.xysss.mvvmhelper.core.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Author:bysd-2
 * Time:2021/9/2717:11
 * 描述　:自定义的Float类型 MutableLiveData 提供了默认值，避免取值的时候还要判空
 */
class FloatLiveData(value: Float = 0f) : MutableLiveData<Float>(value) {
    override fun getValue(): Float {
        return super.getValue()!!
    }
}