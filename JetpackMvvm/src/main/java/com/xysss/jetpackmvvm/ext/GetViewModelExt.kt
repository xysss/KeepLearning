package com.xysss.jetpackmvvm.ext

import java.lang.reflect.ParameterizedType

/**
 * Author:bysd-2
 * Time:2021/9/1615:42
 */


/**
 * 获取当前类绑定的泛型ViewModel-clazz
 */
@Suppress("UNCHECKED_CAST")
fun <VM> getVmClazz(obj: Any): VM {
    return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM
}