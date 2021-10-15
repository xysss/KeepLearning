package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import android.widget.LinearLayout
import com.just.agentweb.AgentWeb
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.ActivityWebBinding
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.getStringExt


class WebActivity : BaseActivity<BaseViewModel,ActivityWebBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        //初始化toolbar
        mToolbar.initBack(getStringExt(R.string.me_web)) {
            finish()
        }

        val bundle:Bundle?=intent.extras
        val name:String?=bundle!!.getString("url")

        AgentWeb.with(this)
            .setAgentWebParent(mViewBinding.webcontent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .createAgentWeb()
            .ready()
            .go(name)

    }

}