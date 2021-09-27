package com.xysss.mvvmhelper.widget

import android.content.Context
import android.view.View
import com.kingja.loadsir.callback.Callback
import com.xysss.mvvmhelper.R

/**
 * Author:bysd-2
 * Time:2021/9/2716:54
 */

class BaseLoadingCallback: Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_loading
    }

    /**
     * 是否是 点击不可重试
     */
    override fun onReloadEvent(context: Context?, view: View?): Boolean {
        return true
    }
}