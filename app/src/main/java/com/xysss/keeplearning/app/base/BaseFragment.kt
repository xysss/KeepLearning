package com.xysss.keeplearning.app.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.xysss.keeplearning.app.etx.dismissLoadingExt
import com.xysss.keeplearning.app.etx.hideSoftKeyboard
import com.xysss.keeplearning.app.etx.showLoadingExt
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.BaseVmDbFragment

/**
 * Author:bysd-2
 * Time:2021/9/1518:30
 * 描述　: 需要自定义修改什么就重写什么 具体方法可以 搜索 BaseIView 查看
 */
abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> : BaseVmDbFragment<VM, DB>(){

}