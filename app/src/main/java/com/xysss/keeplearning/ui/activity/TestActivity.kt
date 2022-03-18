package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.ActivityTestBinding
import com.xysss.keeplearning.ui.fragment.TestFragment
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * Author:bysd-2
 * Time:2021/9/2811:09
 */
class TestActivity : BaseActivity<BaseViewModel, ActivityTestBinding>() {

    private val titles = arrayOf("页面1", "页面2", "页面3")

    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.initBack("测试mFragment") {
            finish()
        }
        mViewBinding.testViewPager.adapter = object :
            FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return TestFragment()
            }

            override fun getCount(): Int {
                return titles.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }
        }
        mViewBinding.testTableLayout.setupWithViewPager(mViewBinding.testViewPager)
        mViewBinding.testViewPager.offscreenPageLimit = titles.size
    }
}