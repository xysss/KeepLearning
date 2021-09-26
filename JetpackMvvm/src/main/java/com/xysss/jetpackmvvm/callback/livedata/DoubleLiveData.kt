package com.xysss.jetpackmvvm.callback.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Author:bysd-2
 * Time:2021/9/2610:30
 */
class DoubleLiveData : MutableLiveData<Double>() {
    override fun getValue(): Double {
        return super.getValue() ?: 0.0
    }
}