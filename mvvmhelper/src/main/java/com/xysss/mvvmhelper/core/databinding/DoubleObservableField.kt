package com.xysss.mvvmhelper.core.databinding

import androidx.databinding.ObservableField

/**
 * Author:bysd-2
 * Time:2021/9/2717:09
 * 描述　:自定义的double类型 ObservableField  提供了默认值，避免取值的时候还要判空
 */
class DoubleObservableField(value: Double = 0.0) : ObservableField<Double>(value) {

    override fun get(): Double {
        return super.get()!!
    }

}