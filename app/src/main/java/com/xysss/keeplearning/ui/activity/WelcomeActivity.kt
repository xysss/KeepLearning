package com.xysss.keeplearning.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.ext.view.gone
import com.xysss.jetpackmvvm.ext.view.visible
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.util.CacheUtil
import com.xysss.keeplearning.app.util.SettingUtil
import com.xysss.keeplearning.viewmodel.state.WelcomeViewModel
import com.xysss.keeplearning.app.weight.banner.WelcomeBannerAdapter
import com.xysss.keeplearning.app.weight.banner.WelcomeBannerViewHolder
import com.xysss.keeplearning.databinding.ActivityWelcomeBinding
import com.zhpan.bannerview.BannerViewPager

/**
 * Author:bysd-2
 * Time:2021/9/1515:50
 */
@Suppress("DEPRECATED_IDENTITY_EQUALS")
class WelcomeActivity : BaseActivity<BaseViewModel, ActivityWelcomeBinding>() {

    private var resList = arrayOf("呼", "哈", "嘿")
    private lateinit var mViewPager: BannerViewPager<String, WelcomeBannerViewHolder>
    override fun layoutId() = R.layout.activity_welcome

    override fun initView(savedInstanceState: Bundle?) {
        //防止出现按Home键回到桌面时，再次点击重新进入该界面bug
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT !== 0) {
            finish()
            return
        }

        mDatabind.click = ProxyClick()
        mDatabind.welcomeBaseview.setBackgroundColor(SettingUtil.getColor(this))
        mViewPager = findViewById(R.id.banner_view)
        if (CacheUtil.isFirst()) {
            //是第一次打开App 显示引导页
            mDatabind.welcomeImage.gone()
            mViewPager.apply {  //apply函数 在函数范围内，可以任意调用该对象的任意方法，并返回该对象
                adapter = WelcomeBannerAdapter()
                setLifecycleRegistry(lifecycle)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == resList.size - 1) {
                            mDatabind.welcomeJoin.visible()
                        } else {
                            mDatabind.welcomeJoin.gone()
                        }
                    }
                })
                create(resList.toList())
            }
        } else {
            //不是第一次打开App 0.3秒后自动跳转到主页
            mDatabind.welcomeImage.visible()
            mViewPager.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                //带点渐变动画
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 300)
        }
    }

    inner class ProxyClick {
        fun toMain() {
            CacheUtil.setFirst(false)
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            finish()
            //带点渐变动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

}