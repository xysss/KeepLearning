package com.xysss.keeplearning.ui.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.blankj.utilcode.util.ToastUtils
import com.tencent.bugly.beta.Beta
import com.xysss.jetpackmvvm.network.manager.NetState
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.appViewModel
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.util.StatusBarUtil
import com.xysss.keeplearning.databinding.ActivityMainBinding
import com.xysss.keeplearning.databinding.ActivityMainTestBinding
import com.xysss.keeplearning.viewmodel.state.MainViewModel


class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    //测试捕获异常
    //CrashReport.testJavaCrash();
    var exitTime = 0L
    override fun layoutId() = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {
        //bugly进入首页检查更新
        Beta.checkUpgrade(false, true)
        //返回按键的监听
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val nav = Navigation.findNavController(this@MainActivity, R.id.host_fragment)
                if (nav.currentDestination != null && nav.currentDestination!!.id != R.id.mainfragment) {
                    //如果当前界面不是主页，那么直接调用返回即可
                    nav.navigateUp()
                } else {
                    //是主页
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        ToastUtils.showShort("再按一次退出程序")
                        exitTime = System.currentTimeMillis()
                    } else {
                        finish()
                    }
                }
            }
        })

        appViewModel.appColor.value?.let { //let函数 默认当前这个对象作为闭包的it参数，返回值是函数里面最后一行，或者指定return
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
            StatusBarUtil.setColor(this, it, 0) }
    }


    override fun createObserver() {
        appViewModel.appColor.observeInActivity(this, Observer {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
            StatusBarUtil.setColor(this, it, 0)
        })
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

}