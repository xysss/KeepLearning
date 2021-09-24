package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.appViewModel
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.etx.init
import com.xysss.keeplearning.app.etx.initMain
import com.xysss.keeplearning.app.etx.interceptLongClick
import com.xysss.keeplearning.app.etx.setUiTheme
import com.xysss.keeplearning.databinding.FragmentMainBinding
import com.xysss.keeplearning.viewmodel.state.MainViewModel

/**
 * Author:bysd-2
 * Time:2021/9/2215:43
 */
class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>() {

    lateinit var mainViewpager :ViewPager2
    override fun layoutId() = R.layout.fragment_main

    override fun initView(savedInstanceState: Bundle?) {

        //初始化viewpager2
        mDatabind.mainViewpager.initMain(this)
        //初始化 bottomBar
        mDatabind. mainBottom.init{
            when (it) {
                R.id.menu_main -> mainViewpager.setCurrentItem(0, false)
                R.id.menu_project -> mainViewpager.setCurrentItem(1, false)
                R.id.menu_system -> mainViewpager.setCurrentItem(2, false)
                R.id.menu_public -> mainViewpager.setCurrentItem(3, false)
                R.id.menu_me -> mainViewpager.setCurrentItem(4, false)
            }
        }
        mDatabind.mainBottom.interceptLongClick(R.id.menu_main,R.id.menu_project,R.id.menu_system,R.id.menu_public,R.id.menu_me)
    }

    override fun createObserver() {
        appViewModel.appColor.observeInFragment(this, Observer {
            setUiTheme(it, mDatabind.mainBottom)
        })
    }
}