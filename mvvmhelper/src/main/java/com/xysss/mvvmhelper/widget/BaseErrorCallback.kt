package com.xysss.mvvmhelper.widget

import com.kingja.loadsir.callback.Callback
import com.xysss.mvvmhelper.R

/**
 * Author:bysd-2
 * Time:2021/9/2716:54
 */
class BaseErrorCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_error
    }

}