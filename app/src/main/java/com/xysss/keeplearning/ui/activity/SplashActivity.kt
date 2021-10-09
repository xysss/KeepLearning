package com.xysss.keeplearning.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.databinding.ActivitySplashBinding
import com.xysss.keeplearning.ui.adapter.SplashBannerAdapter
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.gone
import com.xysss.mvvmhelper.ext.toStartActivity
import com.xysss.mvvmhelper.ext.visible
import com.zhpan.bannerview.BannerViewPager

/**
 * Author:bysd-2
 * Time:2021/9/2810:44
 */
class SplashActivity : BaseActivity<BaseViewModel, ActivitySplashBinding>() {

    private var resList = arrayOf(R.drawable.sanshang_teacher,R.drawable.yingkongtao_teacher, R.drawable.boduo_teacher)

    private lateinit var mViewPager: BannerViewPager<Int>

    override fun initView(savedInstanceState: Bundle?) {
        //该手机的操作系统版本号 比如8.1对应的SDK_INT是27
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        val isFirst = mmkv.getBoolean(ValueKey.isFirst,true)
        if(isFirst){
            mViewBinding.splashImage.gone()
            mViewPager = findViewById(R.id.splash_banner)
            mViewPager.apply {
                adapter = SplashBannerAdapter()
                setLifecycleRegistry(lifecycle)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == resList.size - 1) {
                            mViewBinding.splashJoin.visible()
                        } else {
                            mViewBinding.splashJoin.gone()
                        }
                    }
                })
                create(resList.toList())
            }
        }else{
            mViewBinding.splashImage.visible()
            mViewBinding.splashBanner.gone()
            jumpToMainActivity()
        }
        mViewBinding.splashJoin.setOnClickListener {
            mmkv.putBoolean(ValueKey.isFirst,false)
            jumpToMainActivity()
        }
    }

    private fun jumpToMainActivity(){
        toStartActivity(MainActivity::class.java)
        finish()
    }

    override fun showToolBar() = false
}