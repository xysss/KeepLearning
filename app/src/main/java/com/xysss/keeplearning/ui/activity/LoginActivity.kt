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
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.interception.logging.util.LogUtils


/**
 * Author:bysd-2
 * Time:2021/9/2811:08
 * 描述　: 虽然在Activity代码少了，但是DataBinding 不太好用
 */
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        //初始化toolbar
        mToolbar.initBack(getStringExt(R.string.login_submit)) {
            finish()
        }
        setOnclickNoRepeat(
            mViewBinding.clearText,
            mViewBinding.downText,
            mViewBinding.loginBtn
        ) {
            when (it.id) {
                R.id.clearText -> {
                    mViewBinding.phoneEdt.setText("")
                }
                R.id.downText -> {

                }
                R.id.loginBtn -> {
                    when {
                        mViewBinding.phoneEdt.text.toString().isBlank() -> showDialogMessage("手机号不能为空")
                        mViewBinding.passwordEdt.text.toString().isBlank() -> showDialogMessage("密码不能为空")
                        else -> mViewModel.login(
                            mViewBinding.phoneEdt.text.toString(),
                            mViewBinding.passwordEdt.text.toString()
                        )
                    }
                }
            }
        }

        mViewBinding.showPassword.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            //showPwd(mViewBinding.passwordEdt,isChecked)
        })
    }

    /**
     * 请求成功
     */
    override fun onRequestSuccess() {
        //监听登录结果
        mViewModel.loginData.observe(this){
            //做保存信息等操作
            LogUtils.debugInfo(it.toString())
            it.logA()
            finish()
        }
    }

    /**
     * 请求失败
     * @param loadStatus LoadStatusEntity
     */
    override fun onRequestError(loadStatus: LoadStatusEntity) {
        when (loadStatus.requestCode) {
            NetUrl.LOGIN -> {
                showDialogMessage(loadStatus.errorMessage)
            }
        }
    }

    override fun showCustomLoading(setting: LoadingDialogEntity) {
        if (setting.requestCode == NetUrl.LOGIN) {
            //可以根据不同的code 做不同的loading处理
            showLoadingUi()
        }
    }

    override fun dismissCustomLoading(setting: LoadingDialogEntity) {
        if (setting.requestCode == NetUrl.LOGIN) {
            //可以根据不同的code 做不同的loading处理
            showSuccessUi()
        }
    }

}