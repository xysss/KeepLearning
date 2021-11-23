package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentThreeBinding
import com.xysss.keeplearning.viewmodel.TestViewModel
import jni.JniKit

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class ThreeFragment : BaseFragment<TestViewModel, FragmentThreeBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_report)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorPrimary_20)
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
        mViewBinding.testInput.text = JniKit.stringFromJNI()
    }
}