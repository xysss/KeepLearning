package com.xysss.keeplearning.ui.fragment.me

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ToastUtils
import com.xysss.jetpackmvvm.util.nav
import com.xysss.jetpackmvvm.util.navigateAction
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.appViewModel
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.etx.joinQQGroup
import com.xysss.keeplearning.app.etx.jumpByLogin
import com.xysss.keeplearning.app.etx.setUiTheme
import com.xysss.keeplearning.data.model.bean.BannerResponse
import com.xysss.keeplearning.data.model.bean.IntegralResponse
import com.xysss.keeplearning.databinding.FragmentMeBinding
import com.xysss.keeplearning.viewmodel.request.RequestMeViewModel
import com.xysss.keeplearning.viewmodel.state.MeViewModel

/**
 * Author:bysd-2
 * Time:2021/9/2313:31
 */

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    private var rank: IntegralResponse? = null

    private val requestMeViewModel: RequestMeViewModel by viewModels()

    override fun layoutId() = R.layout.fragment_me
/*
    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.vm = mViewModel
        mDatabind.click = ProxyClick()
        appViewModel.appColor.value?.let { setUiTheme(it, me_linear, me_integral) }
        appViewModel.userInfo.value?.let { mViewModel.name.set(if (it.nickname.isEmpty()) it.username else it.nickname) }
        me_swipe.init {
            requestMeViewModel.getIntegral()
        }
    }

    override fun lazyLoadData() {
        appViewModel.userInfo.value?.run {
            me_swipe.isRefreshing = true
            requestMeViewModel.getIntegral()
        }
    }

    override fun createObserver() {

        requestMeViewModel.meData.observe(viewLifecycleOwner, Observer { resultState ->
            me_swipe.isRefreshing = false
            parseState(resultState, {
                rank = it
                mViewModel.info.set("id：${it.userId}　排名：${it.rank}")
                mViewModel.integral.set(it.coinCount)
            }, {
                ToastUtils.showShort(it.errorMsg)
            })
        })

        appViewModel.run {
            appColor.observeInFragment(this@MeFragment, Observer {
                setUiTheme(it, me_linear, me_swipe, me_integral)
            })
            userInfo.observeInFragment(this@MeFragment, Observer {
                it.notNull({
                    me_swipe.isRefreshing = true
                    mViewModel.name.set(if (it.nickname.isEmpty()) it.username else it.nickname)
                    requestMeViewModel.getIntegral()
                }, {
                    mViewModel.name.set("请先登录~")
                    mViewModel.info.set("id：--　排名：--")
                    mViewModel.integral.set(0)
                })
            })
        }
    }*/

    inner class ProxyClick {

        /** 登录 */
        fun login() {
            nav().jumpByLogin {}
        }

        /** 收藏 */
        fun collect() {
            nav().jumpByLogin {
                it.navigateAction(R.id.action_mainfragment_to_collectFragment)
            }
        }

        /** 积分 */
        fun integral() {
            nav().jumpByLogin {
                it.navigateAction(R.id.action_mainfragment_to_integralFragment,
                    Bundle().apply {
                        putParcelable("rank", rank)
                    }
                )
            }
        }

        /** 文章 */
        fun ariticle() {
            nav().jumpByLogin {
                it.navigateAction(R.id.action_mainfragment_to_ariticleFragment)
            }
        }

        fun todo() {
            nav().jumpByLogin {
                it.navigateAction(R.id.action_mainfragment_to_todoListFragment)
            }
        }

        /** 玩Android开源网站 */
        fun about() {
            nav().navigateAction(R.id.action_to_webFragment, Bundle().apply {
                putParcelable(
                    "bannerdata",
                    BannerResponse(
                        title = "玩Android网站",
                        url = "https://www.wanandroid.com/"
                    )
                )
            })
        }

        /** 加入我们 */
        fun join() {
            joinQQGroup("9n4i5sHt4189d4DvbotKiCHy-5jZtD4D")
        }

        /** 设置*/
        fun setting() {
            nav().navigateAction(R.id.action_mainfragment_to_settingFragment)
        }

        /**demo*/
        fun demo() {
            nav().navigateAction(R.id.action_to_demoFragment)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

}