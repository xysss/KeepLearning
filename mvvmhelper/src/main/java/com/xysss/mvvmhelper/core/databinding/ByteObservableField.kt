package com.xysss.mvvmhelper.core.databinding

import androidx.databinding.ObservableField

/**
 * Author:bysd-2
 * Time:2021/9/2717:09
 * 描述　:自定义的 Byte 类型 ObservableField  提供了默认值，避免取值的时候还要判空
 */
class ByteObservableField(value: Byte = 0) : ObservableField<Byte>(value) {

    override fun get(): Byte {
        return super.get()!!
    }

}