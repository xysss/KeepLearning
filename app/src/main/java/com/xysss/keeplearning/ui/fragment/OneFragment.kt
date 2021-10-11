package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.ListActivity
import com.xysss.keeplearning.ui.activity.LoginActivity
import com.xysss.keeplearning.ui.activity.TestActivity
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.ext.msg
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.ext.toStartActivity

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<TestViewModel, FragmentOneBinding>() {

    private var downloadApkPath = ""

    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_read)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorOrange)
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }

    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload,mViewBinding.testCrash,
            mViewBinding.formEdit) {
            when (it.id) {
                R.id.loginBtn -> {
                    toStartActivity(LoginActivity::class.java)
                }
                R.id.testPageBtn -> {
                    toStartActivity(TestActivity::class.java)
                }
                R.id.testListBtn -> {
                    toStartActivity(ListActivity::class.java)
                }

                R.id.testDownload -> {
                    mViewModel.downLoad({
                        //下载中
                        mViewBinding.testUpdateText.text = "下载进度：${it.progress}%"
                    }, {
                        //下载完成
                        downloadApkPath = it
                        showDialogMessage("下载成功，路径为：${it}")
                    }, {
                        //下载失败
                        showDialogMessage(it.msg)
                    })
                }

                R.id.testUpload -> {
                    mViewModel.upload(downloadApkPath, {
                        //上传中 进度
                        mViewBinding.testUpdateText.text = "上传进度：${it.progress}%"
                    }, {
                        //上传完成
                        showDialogMessage("上传成功：${it}")
                    }, {
                        //上传失败
                        showDialogMessage("${it.msg}--${it.message}")
                    })
                }

                R.id.testCrash -> {
                    //测试捕获异常
                    CrashReport.testJavaCrash()
                }
            }
        }
    }
}