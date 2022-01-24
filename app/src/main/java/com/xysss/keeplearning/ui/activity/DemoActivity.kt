package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.databinding.ActivityDemoBinding
import com.xysss.keeplearning.viewmodel.DemoRequestViewModel
import com.xysss.mvvmhelper.ext.logD
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat

class DemoActivity : BaseActivity<DemoRequestViewModel, ActivityDemoBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewModel.isSetDateShow.observe(this){
            "isSetDateShow".logD()
        }
    }


    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.demoButton1) {
            when (it.id) {
                R.id.button1 -> {
                    mViewModel.setDateShow()
                }
            }
        }
    }
}