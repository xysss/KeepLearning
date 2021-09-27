package com.xysss.keeplearning.ui.activity

import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.etx.init
import com.xysss.keeplearning.app.etx.showMessage
import com.xysss.keeplearning.app.util.SettingUtil
import com.xysss.keeplearning.app.util.StatusBarUtil
import com.xysss.keeplearning.databinding.ActivityErrorBinding
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.clickNoRepeat
import com.xysss.mvvmhelper.util.clipboardManager


/**
 * Author:bysd-2
 * Time:2021/9/1516:50
 */
class ErrorActivity : BaseActivity<BaseViewModel, ActivityErrorBinding>(){

    private lateinit var toolbar:Toolbar
    private lateinit var errorRestart:Button
    private lateinit var errorSendError:Button

    override fun layoutId() = R.layout.activity_error

    override fun initView(savedInstanceState: Bundle?)  {
        toolbar=findViewById(R.id.toolbar)
        errorRestart=findViewById(R.id.errorRestart)
        errorSendError=findViewById(R.id.errorSendError)



        toolbar.init("发生错误")
        supportActionBar?.setBackgroundDrawable(ColorDrawable(SettingUtil.getColor(this)))
        StatusBarUtil.setColor(this, SettingUtil.getColor(this), 0)
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        errorRestart.clickNoRepeat{
            config?.run {
                CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
            }
        }
        errorSendError.clickNoRepeat {
            CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
                showMessage(it,"发现有Bug不去打作者脸？","必须打",{
                    val mClipData = ClipData.newPlainText("errorLog",it)
                    // 将ClipData内容放到系统剪贴板里。
                    clipboardManager?.setPrimaryClip(mClipData)
                    ToastUtils.showShort("已复制错误日志")
                    try {
                        val url = "mqqwpa://im/chat?chat_type=wpa&uin=824868922"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        ToastUtils.showShort("请先安装QQ")
                    }
                },"我不敢")
            }


        }
    }

}