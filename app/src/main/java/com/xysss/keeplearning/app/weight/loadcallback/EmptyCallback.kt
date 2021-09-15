package com.xysss.keeplearning.app.weight.loadcallback

/**
 * Author:bysd-2
 * Time:2021/9/1511:22
 */
import com.kingja.loadsir.callback.Callback
import com.xysss.keeplearning.R

class EmptyCallback : Callback() {
    override fun onCreateView(): Int {
        return R.layout.layout_empty
    }
}