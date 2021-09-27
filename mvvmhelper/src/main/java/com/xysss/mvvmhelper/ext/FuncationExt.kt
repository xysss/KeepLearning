package com.xysss.mvvmhelper.ext

/**
 * Author:bysd-2
 * Time:2021/9/2717:16
 */

inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}
