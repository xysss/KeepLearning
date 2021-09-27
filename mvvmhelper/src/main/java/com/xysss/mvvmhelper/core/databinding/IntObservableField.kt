package com.xysss.mvvmhelper.core.databinding

import androidx.databinding.ObservableField

/**
 * Author:bysd-2
 * Time:2021/9/2717:10
 * 描述　:自定义的Int类型 ObservableField  提供了默认值，避免取值的时候还要判空
 */
class IntObservableField(value: Int = 0) : ObservableField<Int>(value) {

    override fun get(): Int {
        return super.get()!!
    }

}