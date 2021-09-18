package com.xysss.keeplearning.app.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.xysss.jetpackmvvm.base.activity.BaseVmDbActivity
import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.keeplearning.app.etx.dismissLoadingExt
import com.xysss.keeplearning.app.etx.showLoadingExt

/**
 * Author:bysd-2
 * Time:2021/9/1518:30
 * 描述　: 你项目中的Activity基类，在这里实现显示弹窗，吐司，还有加入自己的需求操作
 */
abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> : BaseVmDbActivity<VM, DB>() {

    abstract override fun layoutId(): Int

    abstract override fun initView(savedInstanceState: Bundle?)

    /**
     * 创建liveData观察者
     */
    override fun createObserver() {}

    /**
     * 打开等待框
     */
    override fun showLoading(message: String) {
        showLoadingExt(message)
    }

    /**
     * 关闭等待框
     */
    override fun dismissLoading() {
        dismissLoadingExt()
    }


}