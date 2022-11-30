package com.xysss.keeplearning.app.base

import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.action.HandlerAction
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.widget.CustomToolBar
import com.xysss.mvvmhelper.base.BaseVbActivity
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * Author:bysd-2
 * Time:2021/9/1518:30
 * 描述　: 你项目中的Activity基类，在这里实现显示弹窗，吐司，还有加入自己的需求操作
 */
abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : BaseVbActivity<VM, VB>(),
    HandlerAction {

    lateinit var mToolbar: CustomToolBar

    override fun getTitleBarView(): View? {
        val titleBarView = LayoutInflater.from(this).inflate(R.layout.layout_titlebar_view, null)
        mToolbar = titleBarView.findViewById(R.id.customToolBar)
        return titleBarView
    }

    override fun initImmersionBar() {
        //设置共同沉浸式样式
        if (showToolBar()) {
            mToolbar.setBackgroundResource(R.color.colorPrimary)
            ImmersionBar.with(this).titleBar(mToolbar).init()
        }
    }

}