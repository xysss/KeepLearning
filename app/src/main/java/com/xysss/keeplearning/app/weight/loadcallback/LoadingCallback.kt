package com.xysss.keeplearning.app.weight.loadcallback

import android.content.Context
import android.view.View
import com.kingja.loadsir.callback.Callback
import com.xysss.keeplearning.R

/**
 * Author:bysd-2
 * Time:2021/9/1511:23
 */
class LoadingCallback : Callback(){
    override fun onCreateView(): Int {
        return R.layout.layout_loading
    }
    override fun onReloadEvent(context: Context?, view: View?): Boolean {
        return true
    }
}