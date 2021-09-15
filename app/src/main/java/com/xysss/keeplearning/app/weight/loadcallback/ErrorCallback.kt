package com.xysss.keeplearning.app.weight.loadcallback

import com.kingja.loadsir.callback.Callback
import com.xysss.keeplearning.R

/**
 * Author:bysd-2
 * Time:2021/9/1511:23
 */
class ErrorCallback : Callback() {
    override fun onCreateView(): Int {
        return R.layout.layout_error
    }
}