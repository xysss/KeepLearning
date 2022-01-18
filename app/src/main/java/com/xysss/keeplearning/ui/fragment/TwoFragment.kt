package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.bindViewPager2
import com.xysss.keeplearning.app.ext.init
import com.xysss.keeplearning.databinding.FragmentTwoBinding
import com.xysss.keeplearning.viewmodel.RequestProjectViewModel
import com.xysss.mvvmhelper.ext.logD
import com.xysss.mvvmhelper.ext.logE

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class TwoFragment : BaseFragment<RequestProjectViewModel, FragmentTwoBinding>() {

    //fragment集合
    private var fragments: ArrayList<Fragment> = arrayListOf()

    //标题集合
    private var mDataList: ArrayList<String> = arrayListOf()

    override fun initView(savedInstanceState: Bundle?) {
        //初始化viewpager2
        mViewBinding.viewPager.init(this, fragments)
        //初始化 magic_indicator
        mViewBinding.magicIndicator.bindViewPager2(mViewBinding.viewPager, mDataList)
        //发起请求
        onLoadRetry()
        createObserver()
    }

    private fun createObserver() {
        mViewModel.titleData.observe(this) {
            "titleData请求成功".logE("xysLog")
        }
        mViewModel.projectDataState.observe(this){
            "projectDataState请求成功".logE("xysLog")
        }
    }

    /**
     * 错误界面 空界面 点击重试
     */
    override fun onLoadRetry() {
        mViewModel.getPublicTitleData()
        //mViewModel.getPublicData(false,false,408)
        //mViewModel.getProjectTitleData()
        //mViewModel.getProjectData(false)
    }
}