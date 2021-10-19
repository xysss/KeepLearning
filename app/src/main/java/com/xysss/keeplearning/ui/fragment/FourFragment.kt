package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentFourBinding
import com.xysss.keeplearning.ui.activity.SettingActivity
import com.xysss.keeplearning.ui.activity.WebActivity
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.ext.toStartActivity

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class FourFragment : BaseFragment<TestViewModel, FragmentFourBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.web,mViewBinding.setting) {
            when (it.id) {
                R.id.web -> {
                    val bundle = Bundle()
                    bundle.putString("url","https://github.com/xysss")
                    toStartActivity(WebActivity::class.java,bundle)
                }
                R.id.setting -> {
                    toStartActivity(SettingActivity::class.java)
                }
            }
        }
    }

}