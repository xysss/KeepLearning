package com.xysss.keeplearning.ui.activity

import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.ActivityErrorBinding
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.clickNoRepeat
import com.xysss.mvvmhelper.ext.getStringExt
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.util.clipboardManager

/**
 * Author:bysd-2
 * Time:2021/9/2914:09
 */

class ErrorActivity : BaseActivity<BaseViewModel, ActivityErrorBinding>() {

    override fun initView(savedInstanceState: Bundle?)  {
        //初始化toolbar
        mToolbar.initBack(getStringExt(R.string.error_tips)) {
            finish()
        }
        val defaultColor = ContextCompat.getColor(this, R.color.colorPrimary)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(defaultColor))
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        mViewBinding.errorRestart.clickNoRepeat{
            config?.run {
                CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
            }
        }
        mViewBinding.errorSendError.clickNoRepeat {
            CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
                showDialogMessage(it,"发现有Bug不去打作者脸？","必须打",{
                    val mClipData = ClipData.newPlainText("errorLog",it)
                    // 将ClipData内容放到系统剪贴板里。
                    clipboardManager?.setPrimaryClip(mClipData)
                    ToastUtils.showShort("已复制错误日志")
                    try {
                        val url = "mqqwpa://im/chat?chat_type=wpa&uin=1343025166"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        ToastUtils.showShort("请先安装QQ")
                    }
                },"我不敢")
            }


        }
    }

}