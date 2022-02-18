package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.bindViewPager2
import com.xysss.keeplearning.app.ext.init
import com.xysss.keeplearning.databinding.FragmentTwoBinding
import com.xysss.keeplearning.viewmodel.HistoryTreeViewModel

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class TwoFragment : BaseFragment<HistoryTreeViewModel, FragmentTwoBinding>() {

    var titleData= arrayListOf("数据记录","报警记录")

    //fragment集合
    private var fragments: ArrayList<Fragment> = arrayListOf()

    init {
        fragments.add(HistoryRecordFragment())
        fragments.add(HistoryAlarmFragment())
    }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.customTwoToolbar.setCenterTitle(R.string.bottom_title_paper)

        //初始化viewpager2
        //mViewBinding.viewPager.init(this, fragments)
        //初始化 magic_indicator
        //mViewBinding.magicIndicator.bindViewPager2(mViewBinding.viewPager, mDataList)
        //初始化viewpager2
        mViewBinding.viewPager.init(this, fragments).offscreenPageLimit = fragments.size
        //初始化 magic_indicator
        mViewBinding.magicIndicator.bindViewPager2(mViewBinding.viewPager, mStringList = titleData){
            if (it != 0) {
                //mViewBinding.includeViewpagerToolbar.menu.clear()
            } else {
//                mViewBinding.includeViewpagerToolbar.menu.hasVisibleItems().let { flag ->
//                    if (!flag) mViewBinding.includeViewpagerToolbar.inflateMenu(R.menu.todo_menu)
//                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customTwoToolbar)
        }
    }

    override fun lazyLoadData() {
//        //初始化viewpager2
//        mViewBinding.viewPager.init(this, fragments).offscreenPageLimit = fragments.size
//        //初始化 magic_indicator
//        mViewBinding.magicIndicator.bindViewPager2(mViewBinding.viewPager, mStringList = titleData)
        }
    }