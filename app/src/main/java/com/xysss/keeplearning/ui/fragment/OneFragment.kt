package com.xysss.keeplearning.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.xysss.keeplearning.app.ext.isRecOK
import com.xysss.keeplearning.app.ext.isStopReqRealMsg
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.ext.scope
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.*
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.msg
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.ext.showDialogMessage
import com.xysss.mvvmhelper.ext.toStartActivity
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.LoadingType.Companion.LOADING_CUSTOM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<BlueToothViewModel, FragmentOneBinding>(){

    private var downloadApkPath = ""
    private lateinit var mService: MQTTService
    private var loadingDialogEntity=LoadingDialogEntity()
    private val send00Msg="55000a0900000100"  //读取设备信息
    private val send10Msg="55000a0910000100"  //读取实时数据
    private val send20Msg="55000a0920000100"  //读取物质库信息
    private val send21Msg="55000D0921000401000000"  //读取物质条目信息
    private var isClickStart=true
    private lateinit var mTimer : Timer
    private lateinit var historyTask: HistoryTimerTask
    private lateinit var realDataTask: RealTimeDataTimerTask

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
        //请求权限
        requestCameraPermissions()
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun initObserver() {
        super.initObserver()
        mViewModel.bleDate.observe(this){
            mViewBinding.concentrationNum.text=it.concentrationNum
            mViewBinding.concentrationUnit.text=it.concentrationUnit
            mViewBinding.materialName.text=it.materialName
        }

        mViewModel.bleState.observe(this){
            mViewBinding.blueTv.text=it
            if (it=="已连接设备"){
                mViewBinding.blueTv.setTextColor(Color.parseColor("#4BDAFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.mipmap.connected_icon,null))

                dismissProgressUI()
            }else if (it=="未连接设备"){
                mViewBinding.blueTv.setTextColor(Color.parseColor("#FFFFFFFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.mipmap.no_connected_icon,null))
            }
        }

        mViewModel.progressNum.observe(this){
            mViewBinding.progressBar.progress=it
            mViewBinding.synNumText.text="$it%"
            if (it==100){
                dismissProgressUI()
                ToastUtils.showShort("同步完成")

                historyTask.cancel()
                mTimer.cancel()
            }
        }

        mViewModel.numShow.observe(this){
            mViewBinding.numShowText.text=it
        }
    }

    /**
     * 请求相机权限
     */
    @SuppressLint("CheckResult")
    private fun requestCameraPermissions() {
        ToastUtils.showShort("请求相机权限")
        //请求打开相机权限swdxz
        val rxPermissions = RxPermissions(requireActivity())
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE).subscribe { aBoolean ->
                if (aBoolean) {
                    ToastUtils.showShort("权限已经打开")
                } else {
                    ToastUtils.showShort("权限被拒绝")
                }
            }
    }

    override fun onDestroyView() {
        appContext.unbindService(connection)
        super.onDestroyView()
    }

    //蓝牙页面回调
    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val device = result.data?.getParcelableExtra<BluetoothDevice>("device")
                //val data = result.data?.getStringExtra("data")
                mViewModel.connectBlueTooth(device)
                //等待页面
                loadingDialogEntity.loadingType= LOADING_CUSTOM
                loadingDialogEntity.loadingMessage="连接蓝牙中"
                loadingDialogEntity.isShow=true
                loadingDialogEntity.requestCode="linkBle"
                showCustomLoading(loadingDialogEntity)
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
                    if(isClickStart)
                        startTest()
                    else
                        stopTest()
                }
                R.id.toServiceBackImg->{
                    if (mmkv.getString(ValueKey.deviceId,"")!=""){
                        mViewModel.setMqttConnect()
                    }
                }
                R.id.synRecordBackgroundImg->{
                    synMessage(1)
                }
                R.id.synAlarmBackgroundImg->{
                    synMessage(2)
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

    private fun synMessage(flag:Int){
        stopTest()

        //已经废弃，不建议使用
//                    val dialog = progressDialog("正在努力加载页面", "请稍候")
//                    dialog.setCanceledOnTouchOutside(false)  //禁止外部点击消失
//                    dialog.progress = 10  //设置进度条,默认总进度为100
//                    dialog.show()

        AlertDialog.Builder(context).apply {
            setTitle("提示")
            setMessage("是否开始同步记录，这可能需要等待一段时间")
            setCancelable(false)
            setPositiveButton("确定"){ _, _ ->
                showProgressUI()
                if (flag==1)
                    BleHelper.sendRecordMsg()
                else if (flag==2)
                    BleHelper.sendAlarmMsg()
                mTimer = Timer()
                historyTask = HistoryTimerTask()
                mTimer.schedule(historyTask,15*1000,15*1000)
            }

            setNegativeButton("取消"){ _, _ ->
            }
            show()
        }
    }

    private fun stopTest(){
        isStopReqRealMsg =true
        isClickStart=true
        mViewBinding.testText.text="开始"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.mipmap.start_icon,null))

        mViewBinding.synLin.visibility= View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
    }
    private fun startTest(){
        isStopReqRealMsg =false
        isClickStart=false
        mViewBinding.testText.text="停止"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.mipmap.pause_icon,null))

        mViewBinding.synLin.visibility= View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE

        BleHelper.addSendLinkedDeque(send10Msg)

        mTimer = Timer()
        realDataTask = RealTimeDataTimerTask()
        mTimer.schedule(realDataTask,15*1000,15*1000)
    }

    private fun showProgressUI(){
        mViewBinding.progressBar.progress=0
        mViewBinding.synNumText.text="0%"
        mViewBinding.numShowText.text="0/0"

        mViewBinding.synLin.visibility= View.VISIBLE
        mViewBinding.progressBar.visibility = View.VISIBLE
        mViewBinding.numShowText.visibility = View.VISIBLE

        loadingDialogEntity.loadingType= LOADING_CUSTOM
        loadingDialogEntity.loadingMessage="同步数据信息中"
        loadingDialogEntity.isShow=true
        loadingDialogEntity.requestCode="reqRecord"
        showCustomLoading(loadingDialogEntity)
    }

    private fun dismissProgressUI(){
        mViewBinding.synLin.visibility= View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
        mViewBinding.numShowText.visibility = View.INVISIBLE

        dismissCustomLoading(loadingDialogEntity)
    }

    override fun onDestroy() {
        BleHelper.gatt?.close()
        realDataTask.cancel()
        historyTask.cancel()
        mTimer.cancel()
        super.onDestroy()
    }

    inner class RealTimeDataTimerTask : TimerTask() {
        override fun run() {
            if (isRecOK){
                isRecOK=false
            }else{
                scope.launch(Dispatchers.Main) {
                    dismissProgressUI()
                    ToastUtils.showShort("数据接收错误,请重新尝试")
                    realDataTask.cancel()
                    mTimer.cancel()
                    stopTest()
                }
            }
        }
    }

    inner class HistoryTimerTask : TimerTask() {
        override fun run() {
//            val id = Thread.currentThread().id
//            "此时运行在${if (isMainThread()) "主线程" else "子线程"}中   线程号：$id".logE("xysLog")
            if (isRecOK){
                isRecOK=false
            }else{
                scope.launch(Dispatchers.Main) {
                    dismissProgressUI()
                    ToastUtils.showShort("数据接收错误,请重新尝试")
                    historyTask.cancel()
                    mTimer.cancel()
                }
            }
        }
    }

}