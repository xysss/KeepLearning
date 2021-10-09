package com.xysss.keeplearning.app.base

import androidx.viewbinding.ViewBinding
import com.xysss.mvvmhelper.base.BaseVbFragment
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * Author:bysd-2
 * Time:2021/9/1518:30
 * 描述　: 需要自定义修改什么就重写什么 具体方法可以 搜索 BaseIView 查看
 */
abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> : BaseVbFragment<VM, VB>(){

}