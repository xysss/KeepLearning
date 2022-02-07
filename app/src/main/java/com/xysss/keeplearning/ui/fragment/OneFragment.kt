package com.xysss.keeplearning.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.*
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.msg
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.ext.toStartActivity

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<BlueToothViewModel, FragmentOneBinding>() {

    private var downloadApkPath = ""
    private val publishTopic = "HT308PRD/VP200/C2S/{SN}" //发送主题
    private var mService: MQTTService? = null

    private val connection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as MQTTService.MyBinder
            mService = mBinder.service
            //mService?.connect(appContext)

        }

        //崩溃被杀掉的时候回调
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_read)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorOrange)
        //bugly进入首页检查更新
        //Beta.checkUpgrade(false, true)
        //开启服务
        val intentMqttService = Intent(appContext, MQTTService::class.java)
        bindService(intentMqttService, connection, Context.BIND_AUTO_CREATE)
        //注册回调
        mViewModel.bleCallBack()
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }

    /**
     * 请求相机权限
     */
    @SuppressLint("CheckResult")
    private fun requestCameraPermissions() {
        ToastUtils.showShort("请求相机权限")
        //请求打开相机权限
        val rxPermissions = RxPermissions(requireActivity())
        rxPermissions.request(Manifest.permission.CAMERA)
            .subscribe { aBoolean ->
                if (aBoolean) {
                    ToastUtils.showShort("相机权限已经打开，直接跳入相机")
                } else {
                    ToastUtils.showShort("权限被拒绝")
                }
            }
    }

    override fun onDestroyView() {
        appContext.unbindService(connection)
        super.onDestroyView()
    }

    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val device = result.data?.getParcelableExtra<BluetoothDevice>("device")
                //val data = result.data?.getStringExtra("data")
                // Handle data from SecondActivity
                mService?.blueToothConnect(device)
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewClick() {
        setOnclickNoRepeat(
            mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload, mViewBinding.testCrash,
            mViewBinding.getPermission, mViewBinding.testRoom, mViewBinding.linkBlueTooth
        ) {
            when (it.id) {
                R.id.testRoom -> {
                    toStartActivity(RoomSampleActivity::class.java)
                }
                R.id.getPermission -> {
                    requestCameraPermissions()
                }
                R.id.loginBtn -> {
                    toStartActivity(LoginActivity::class.java)
                }
                R.id.testPageBtn -> {
                    //toStartActivity(TestActivity::class.java)
                    mService?.publish(publishTopic, "Test")
                }
                R.id.testListBtn -> {
                    //toStartActivity(ListActivity::class.java)
                    mService?.blueToothSendMsg("55000a09000001000023")
                }
                R.id.linkBlueTooth -> {
                    //toStartActivity(LinkBleBlueTooth::class.java)
                    val intentBle = Intent(appContext, LinkBleBlueTooth::class.java)
                    requestDataLauncher.launch(intentBle)

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