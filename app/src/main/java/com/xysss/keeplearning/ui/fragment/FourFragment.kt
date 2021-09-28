package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentFourBinding
import com.xysss.keeplearning.viewmodel.TestViewModel

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class FourFragment : BaseFragment<TestViewModel, FragmentFourBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mDataBind.userHeadImg)
        }
    }

}