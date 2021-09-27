package com.xysss.mvvmhelper.ext

import java.lang.reflect.ParameterizedType

/**
 * Author:bysd-2
 * Time:2021/9/2717:17
 */
/**
 * 获取当前类绑定的泛型ViewModel-clazz
 */
@Suppress("UNCHECKED_CAST")
fun <VM> getVmClazz(obj: Any): VM {
    return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM
}
fun <DB> getDbClazz(obj: Any): DB {
    return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as DB
}
