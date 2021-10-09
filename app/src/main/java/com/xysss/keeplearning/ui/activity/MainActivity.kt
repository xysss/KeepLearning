package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import android.widget.Toast
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.databinding.ActivityMainBinding
import com.xysss.keeplearning.ui.adapter.MainAdapter
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.net.manager.NetState

class MainActivity : BaseActivity<TestViewModel, ActivityMainBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.setCenterTitle(R.string.bottom_title_read)
        mViewBinding.mainViewPager.adapter = MainAdapter(this)
        mViewBinding.mainViewPager.offscreenPageLimit = mViewBinding.mainViewPager.adapter!!.itemCount
        mViewBinding.mainViewPager.isUserInputEnabled = false
        mViewBinding.mainNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationRead -> {
                    mViewBinding.mainViewPager.setCurrentItem(0, false)
                }
                R.id.navigationPaper -> {
                    mViewBinding.mainViewPager.setCurrentItem(1, false)
                }
                R.id.navigationReport -> {
                    mViewBinding.mainViewPager.setCurrentItem(2, false)
                }
                R.id.navigationUser -> {
                    mViewBinding.mainViewPager.setCurrentItem(3, false)
                }
            }
            true
        }
    }

    /**
     * 示例，在Activity/Fragment中如果想监听网络变化，可重写onNetworkStateChanged该方法
     */
    override fun onNetworkStateChanged(netState: NetState) {
        super.onNetworkStateChanged(netState)
        if (netState.isSuccess) {
            Toast.makeText(applicationContext, "我特么终于有网了啊!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "我特么怎么断网了!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showToolBar() = false

}
