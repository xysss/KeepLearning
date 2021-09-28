package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.databinding.ActivityMainBinding
import com.xysss.keeplearning.ui.adapter.MainAdapter
import com.xysss.keeplearning.viewmodel.TestViewModel

class MainActivity : BaseActivity<TestViewModel, ActivityMainBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.setCenterTitle(R.string.bottom_title_read)
        mDataBind.mainViewPager.adapter = MainAdapter(this)
        mDataBind.mainViewPager.offscreenPageLimit = mDataBind.mainViewPager.adapter!!.itemCount
        mDataBind.mainViewPager.isUserInputEnabled = false
        mDataBind.mainNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationRead -> {
                    mDataBind.mainViewPager.setCurrentItem(0, false)
                }
                R.id.navigationPaper -> {
                    mDataBind.mainViewPager.setCurrentItem(1, false)
                }
                R.id.navigationReport -> {
                    mDataBind.mainViewPager.setCurrentItem(2, false)
                }
                R.id.navigationUser -> {
                    mDataBind.mainViewPager.setCurrentItem(3, false)
                }
            }
            true
        }
    }

    override fun showToolBar() = false

}
