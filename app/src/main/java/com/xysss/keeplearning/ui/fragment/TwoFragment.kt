package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.bindViewPager2
import com.xysss.keeplearning.app.ext.init
import com.xysss.keeplearning.databinding.FragmentTwoBinding
import com.xysss.keeplearning.viewmodel.RequestProjectViewModel
import com.xysss.mvvmhelper.ext.logD

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class TwoFragment : BaseFragment<RequestProjectViewModel, FragmentTwoBinding>() {

    //fragment集合
    var fragments: ArrayList<Fragment> = arrayListOf()

    //标题集合
    var mDataList: ArrayList<String> = arrayListOf()

    override fun initView(savedInstanceState: Bundle?) {

        //初始化viewpager2
        mViewBinding.viewPager.init(this, fragments)
        //初始化 magic_indicator
        mViewBinding.magicIndicator.bindViewPager2(mViewBinding.viewPager, mDataList)

        //发起请求
        onLoadRetry()
        createObserver()
    }

    fun createObserver() {
        mViewModel.titleData.observe(viewLifecycleOwner, Observer {
            "titleData请求成功".logD()
            mDataList.clear()
            fragments.clear()
            mDataList.add("最新项目")
            /*mDataList.addAll(it.datas.map { it.name })*/
            fragments.add(ProjectChildFragment.newInstance(0, true))
            /*it.data.forEach { classify ->
                //fragments.add(ProjectChildFragment.newInstance(classify.id, false))
            }*/
        })

        mViewModel.projectDataState.observe(viewLifecycleOwner,{
            "projectDataState请求成功".logD()
            it.toString().logD()
        })

    }

    /**
     * 错误界面 空界面 点击重试
     */
    override fun onLoadRetry() {
        //请求标题数据
        mViewModel.getList()

        mViewModel.getProjectData(false)

    }



}