package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.ActivityLoginBinding
import com.xysss.keeplearning.viewmodel.LoginViewModel
import com.xysss.mvvmhelper.ext.getStringExt
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.net.LoadingDialogEntity

/**
 * Author:bysd-2
 * Time:2021/9/2811:08
 * 描述　: 虽然在Activity代码少了，但是DataBinding 不太好用
 */
class LoginActivity: BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        //初始化toolbar
        mToolbar.initBack(getStringExt(R.string.login_submit)) {
            finish()
        }
        mDataBind.viewModel = mViewModel
        mDataBind.click = LoginClickProxy()
    }

    /**
     * 请求成功
     */
    override fun onRequestSuccess() {
        //监听登录结果
        mViewModel.loginData.observe(this, Observer {
            //做保存信息等操作
            finish()
        })
    }

    /**
     * 请求失败
     * @param loadStatus LoadStatusEntity
     */
    override fun onRequestError(loadStatus: LoadStatusEntity) {
        when(loadStatus.requestCode){
            NetUrl.LOGIN ->{
                showDialogMessage(loadStatus.errorMessage)
            }
        }
    }

    inner class LoginClickProxy{

        fun clear() {
            mViewModel.userName.set("")
        }

        //登录
        fun login(){
            when {
                mViewModel.userName.get().isEmpty() -> showDialogMessage("手机号不能为空")
                mViewModel.password.get().isEmpty() -> showDialogMessage("密码不能为空")
                else -> mViewModel.login(mViewModel.userName.get(), mViewModel.password.get())
            }
        }

        var onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            mViewModel.isShowPwd.set(isChecked)
        }
    }

    override fun showCustomLoading(setting: LoadingDialogEntity) {
        if(setting.requestCode== NetUrl.LOGIN){
            //可以根据不同的code 做不同的loading处理
            showLoadingUi()
        }
    }

    override fun dismissCustomLoading(setting: LoadingDialogEntity) {
        if(setting.requestCode==NetUrl.LOGIN){
            //可以根据不同的code 做不同的loading处理
            showSuccessUi()
        }
    }

}