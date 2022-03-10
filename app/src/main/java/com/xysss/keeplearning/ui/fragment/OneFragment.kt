package com.xysss.keeplearning.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.isStopReqRealMsg
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.ext.recordFileName
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.FileUtils
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.*
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.msg
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.ext.toStartActivity

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<BlueToothViewModel, FragmentOneBinding>(){

    private var downloadApkPath = ""
    private lateinit var mService: MQTTService

    //状态缓存
    private var stringBuffer = StringBuffer()

    private val send00Msg="55000a0900000100"  //读取设备信息
    private val send10Msg="55000a0910000100"  //读取实时数据
    private val send20Msg="55000a0920000100"  //读取物质库信息
    private val send21Msg="55000D0921000401000000"  //读取物质条目信息
    private var isClickStart=true

    private val connection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as MQTTService.MyBinder
            mService = mBinder.service
            mViewModel.putService(mService)
        }

        //崩溃被杀掉的时候回调
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_read)
        //bugly进入首页检查更新
        //Beta.checkUpgrade(false, true)
        //开启服务
        val intentMqttService = Intent(appContext, MQTTService::class.java)
        bindService(intentMqttService, connection, Context.BIND_AUTO_CREATE)

        mViewModel.setCallBack()

        //去连接蓝牙
        val intentBle = Intent(appContext, LinkBleBlueToothActivity::class.java)
        requestDataLauncher.launch(intentBle)
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.bleDate.observe(this){
//            val id = Thread.currentThread().id
//            "state方法中的线程号：$id".logE("xysLog")
//            "state方回调运行在${if (isMainThread()) "主线程" else "子线程"}中".logE("xysLog")
//            mViewBinding.tvState.text = "收到转码后的数据长度: ${it?.length}: $it"
            mViewBinding.concentrationNum.text=it.concentrationNum
            mViewBinding.concentrationUnit.text=it.concentrationUnit
            mViewBinding.materialName.text=it.materialName
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
        rxPermissions.request(Manifest.permission.CAMERA).subscribe { aBoolean ->
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val device = result.data?.getParcelableExtra<BluetoothDevice>("device")
                //val data = result.data?.getStringExtra("data")
                mViewModel.connectBlueTooth(device)

                mViewBinding.blueLinkTv.text="已连接设备"
                mViewBinding.blueLinkTv.setTextColor(Color.parseColor("#4BDAFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.mipmap.connected_icon,null))
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewClick() {
        setOnclickNoRepeat(
            mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload, mViewBinding.testCrash,
            mViewBinding.getPermission, mViewBinding.testRoom, mViewBinding.linkBlueTooth,


            mViewBinding.blueLink,mViewBinding.testBackgroundImg,mViewBinding.toServiceBackImg,
            mViewBinding.synRecordBackgroundImg,mViewBinding.synAlarmBackgroundImg

        ) {
            when (it.id) {
                R.id.blueLink->{
                    val intentBle = Intent(appContext, LinkBleBlueToothActivity::class.java)
                    requestDataLauncher.launch(intentBle)
                }

                R.id.testBackgroundImg->{
                    if(isClickStart){
                        isStopReqRealMsg =false
                        BleHelper.addSendLinkedDeque(send10Msg)

                        isClickStart=false
                        mViewBinding.testText.text="停止"
                        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.mipmap.pause_icon,null))
                    }else{
                        isStopReqRealMsg =true

                        isClickStart=true
                        mViewBinding.testText.text="开始"
                        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.mipmap.start_icon,null))
                    }
                }

                R.id.toServiceBackImg->{
                    if (mmkv.getString(ValueKey.deviceId,"")!=""){
                        mViewModel.setMqttConnect()
                    }
                }

                R.id.synRecordBackgroundImg->{
                    BleHelper.sendRecordMsg()
                    mViewBinding.synLin.visibility=View.VISIBLE
                }

                R.id.synAlarmBackgroundImg->{
                    BleHelper.sendAlarmMsg()
                }





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
                    toStartActivity(TestActivity::class.java)
                }
                R.id.testListBtn -> {
                    toStartActivity(ListActivity::class.java)
                }
                R.id.linkBlueTooth -> {
                    //toStartActivity(LinkBleBlueTooth::class.java)
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

    override fun onDestroy() {
        BleHelper.gatt?.close()
        super.onDestroy()
    }
}