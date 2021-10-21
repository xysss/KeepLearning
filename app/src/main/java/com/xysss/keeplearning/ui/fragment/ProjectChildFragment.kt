package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.xysss.keeplearning.databinding.IncludeListBinding
import com.xysss.mvvmhelper.base.BaseVbFragment
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * Author:bysd-2
 * Time:2021/10/2117:40
 */
class ProjectChildFragment : BaseVbFragment<BaseViewModel, IncludeListBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    companion object {
        fun newInstance(cid: Int, isNew: Boolean): ProjectChildFragment {
            val args = Bundle()
            args.putInt("cid", cid)
            args.putBoolean("isNew", isNew)
            val fragment = ProjectChildFragment()
            fragment.arguments = args
            return fragment
        }
    }

}