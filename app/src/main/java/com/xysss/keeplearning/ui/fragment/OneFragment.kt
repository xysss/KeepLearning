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
import android.opengl.Visibility
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.swallowsonny.convertextlibrary.writeInt16LE
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.Crc8
import com.xysss.keeplearning.app.util.FileUtils
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.*
import com.xysss.keeplearning.viewmodel.OneFragmentViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.LoadingType.Companion.LOADING_CUSTOM
import getRouteWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<OneFragmentViewModel, FragmentOneBinding>() {
    private var downloadApkPath = ""
    private lateinit var mqttService: MQTTService
    private var loadingDialogEntity = LoadingDialogEntity()
    private val send00Msg = "55000a0900000100"  //读取设备信息
    private val send10Msg = "55000a0910000100"  //读取实时数据
    private val send20Msg = "55000a0920000100"  //读取物质库信息
    private val send21Msg = "55000D0921000401000000"  //读取物质条目信息
    private var isRealing = false
    private var mTimer: Timer? = null
    private var historyTask: HistoryTimerTask? = null
    private var realDataTask: RealTimeDataTimerTask? = null
    private var blueConnectTask: BlueToothConnectTimerTask? = null
    private var retryFlagCount = 0

    private var isDrawPoint = false  //是否要画起点和终点
    private var isSurveying = false  //是否巡测中
    private var ppmChoice = -1

    private val connection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as MQTTService.MyBinder
            mqttService = mBinder.service
            mViewModel.putService(mqttService)

            if (mmkv.getString(ValueKey.deviceId,"")!=""){
                if (!isConnectMqtt){
                    recTopic= mmkv.getString(ValueKey.recTopicValue, "").toString()
                    sendTopic= mmkv.getString(ValueKey.sendTopicValue, "").toString()
                    mqttService.connectMqtt(appContext)
                }
            }
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

        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mViewBinding.mMapView.onCreate(savedInstanceState)
        mViewBinding.mMapView.map.uiSettings.isZoomControlsEnabled = false
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
        // 在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mViewBinding.mMapView.onResume()
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n", "UseCompatLoadingForDrawables")
    override fun initObserver() {
        super.initObserver()
        mViewModel.bleDate.observe(this) {
            mViewBinding.concentrationNum.text = it.concentrationNum
            mViewBinding.concentrationUnit.text = it.concentrationUnit
            mViewBinding.materialName.text = it.materialName
        }

        mViewModel.latLngResultList.observe(this){
            drawMapLine(it)
        }

        mViewModel.bleState.observe(this) {
            mViewBinding.blueTv.text = it
            if (it == "已连接设备") {
                isBleReady=true
                mViewBinding.blueTv.setTextColor(Color.parseColor("#4BDAFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.drawable.connected_icon, null))

                dismissProgressUI()
            } else if (it == "未连接设备") {
                isBleReady=false
                mViewBinding.blueTv.setTextColor(Color.parseColor("#FFFFFFFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.drawable.no_connected_icon, null))
            }
        }

        mViewModel.progressNum.observe(this) {
            mViewBinding.progressBar.progress = it
            mViewBinding.synNumText.text = "$it%"
            if (it == 100) {
                dismissProgressUI()
                ToastUtils.showShort("同步完成")
                historyTask?.cancel()
                mTimer?.cancel()
            }
        }

        mViewModel.dialogStatus.observe(this) {
            if (it){
                dismissProgressUI()
            }else{
                showProgressUI()
            }
        }

        mViewModel.numShow.observe(this) {
            mViewBinding.numShowText.text = it
        }
    }

    private fun drawMapLine(list: MutableList<LatLng>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        val mBuilder = LatLngBounds.Builder()
        val polylineOptions = PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
            .color(mViewModel.getDriveColor())
            .width(getRouteWidth())
            .addAll(list)
        //mMap.clear()
        mViewBinding.mMapView.map.addPolyline(polylineOptions)
        if (isDrawPoint){
            if (isSurveying) {
                val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
                val markerBegin: Marker = mViewBinding.mMapView.map.addMarker(
                    MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker")
                )
            } else {
                val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
                val markerEnd: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))
                mViewModel.stopLocation()
                mViewBinding.functionCl.visibility=View.VISIBLE
                mViewBinding.trackRl.visibility=View.GONE
                isPollingModel=false
            }
            isDrawPoint=false
        }
        //mMap.mapType=AMap.MAP_TYPE_NORMAL  //白昼地图（即普通地图）

        list.forEach {
            mBuilder.include(it)
        }

        val cameraUpdate: CameraUpdate
        // 判断,区域点计算出来,的两个点相同,这样地图视角发生改变,SDK5.0.0会出现异常白屏(定位到海上了)
        val northeast = mBuilder.build().northeast
        cameraUpdate = if (northeast != null && northeast == mBuilder.build().southwest) {
            CameraUpdateFactory.newLatLng(mBuilder.build().southwest)
        } else {
            CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 20)
        }
        mViewBinding.mMapView.map.animateCamera(cameraUpdate)
    }

    /**
     * 请求权限
     */
    @SuppressLint("CheckResult")
    private fun requestCameraPermissions() {
        ToastUtils.showShort("请求权限")
        //请求打开相机权限
        val rxPermissions = RxPermissions(requireActivity())
        rxPermissions.request(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe { aBoolean ->
            if (aBoolean) {
                ToastUtils.showShort("权限已经打开")
            } else {
                ToastUtils.showShort("权限被拒绝")
            }
        }
    }

    override fun onDestroyView() {
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
                loadingDialogEntity.loadingType = LOADING_CUSTOM
                loadingDialogEntity.loadingMessage = "连接蓝牙中"
                loadingDialogEntity.isShow = true
                loadingDialogEntity.requestCode = "linkBle"
                showCustomLoading(loadingDialogEntity)

                mTimer = Timer()
                blueConnectTask = BlueToothConnectTimerTask()
                mTimer?.schedule(blueConnectTask, 10 * 1000, 10 * 1000)
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewClick() {
        setOnclickNoRepeat(
            mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload, mViewBinding.testCrash,
            mViewBinding.getPermission, mViewBinding.testRoom, mViewBinding.linkBlueTooth,

            mViewBinding.blueLink, mViewBinding.testBackgroundImg, mViewBinding.toServiceBackImg,
            mViewBinding.synRecordBackgroundImg, mViewBinding.synAlarmBackgroundImg,
            mViewBinding.btnSurVey, mViewBinding.btnShow

        ) {
            when (it.id) {
                R.id.btn_surVey -> {
                    when (mmkv.getInt(ValueKey.ppmValue, 0)) {
                        5 -> {
                            mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.five_ppm_icon, null))
                            surVeyClick()
                        }
                        10 -> {
                            mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.ten_ppm_icon, null))
                            surVeyClick()
                        }
                        else -> {
                            ToastUtils.showShort("请先设置ppm参数")
                        }
                    }
                }

                R.id.btn_show -> {
                    showSingleChoiceDialog()
                    //mViewModel.onShowClick()
                }

                R.id.blueLink -> {
                    val intentBle = Intent(appContext, LinkBleBlueToothActivity::class.java)
                    requestDataLauncher.launch(intentBle)
                }
                R.id.testBackgroundImg -> {
                    if (isBleReady){
                        if (!isRealing)
                            startTest()
                        else
                            stopTest()
                    }else{
                        ToastUtils.showShort("请先连接蓝牙")
                    }

                }
                //进入巡测模式
                R.id.toServiceBackImg -> {
                    if (isBleReady){
                        if (!isRealing){
                            startTest()
                        }
                        mViewBinding.functionCl.visibility=View.GONE
                        mViewBinding.trackRl.visibility=View.VISIBLE
                        isPollingModel=true
                        //toStartActivity(AMapTrackActivity::class.java)
                    }else{
                        ToastUtils.showShort("请先连接蓝牙")
                    }
                }

                R.id.synRecordBackgroundImg -> {
                    if (netConnectIsOK){
                        if (isBleReady){
                            synMessage(3)
                        }else{
                            ToastUtils.showShort("请先连接蓝牙")
                        }
                    }else{
                        ToastUtils.showShort("网络未连接")
                    }
                }
                R.id.synAlarmBackgroundImg -> {
                    if (netConnectIsOK){
                        if (isBleReady){
                            synMessage(2)
                        }else{
                            ToastUtils.showShort("请先连接蓝牙")
                        }
                    }else{
                        ToastUtils.showShort("网络未连接")
                    }
                }

                //以下为demo按钮
                R.id.testRoom -> {
                    toStartActivity(RoomSampleActivity::class.java)
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showSingleChoiceDialog() {
        val lastPpmValue: Int = when (mmkv.getInt(ValueKey.ppmValue, 0)) {
            5 -> {
                mmkv.putInt(ValueKey.ppmValue, 5)
                mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.five_ppm_icon, null))
                0
            }
            10 ->{
                mmkv.putInt(ValueKey.ppmValue, 10)
                mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.ten_ppm_icon, null))
                1
            }
            else -> {
                mmkv.putInt(ValueKey.ppmValue, 10)
                mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.ten_ppm_icon, null))
                1
            }
        }
        val items = arrayOf("5ppm", "10ppm")
        val singleChoiceDialog = androidx.appcompat.app.AlertDialog.Builder(mActivity)
        singleChoiceDialog.setTitle("请选择")
        // 第二个参数是默认选项，此处设置
        singleChoiceDialog.setSingleChoiceItems(items, lastPpmValue) { _, which ->
            ppmChoice = which
        }
        singleChoiceDialog.setPositiveButton("确定") { _, _ ->
            if (ppmChoice != -1) {
                ToastUtils.showShort("你选择了" + items[ppmChoice])
                when (ppmChoice) {
                    0 -> {
                        mmkv.putInt(ValueKey.ppmValue, 5)
                        mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.five_ppm_icon, null))
                    }
                    1 -> {
                        mmkv.putInt(ValueKey.ppmValue, 10)
                        mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.ten_ppm_icon, null))
                    }
                    else ->{
                        mmkv.putInt(ValueKey.ppmValue, 10)
                        mViewBinding.imageIcon.setImageDrawable(resources.getDrawable(R.drawable.ten_ppm_icon, null))
                    }
                }
            }
        }
        singleChoiceDialog.show()
    }


    private fun synMessage(flag: Int) {
        //已经废弃，不建议使用
//                    val dialog = progressDialog("正在努力加载页面", "请稍候")
//                    dialog.setCanceledOnTouchOutside(false)  //禁止外部点击消失
//                    dialog.progress = 10  //设置进度条,默认总进度为100
//                    dialog.show()

        AlertDialog.Builder(context).apply {
            setTitle("提示")
            setMessage("是否开始同步记录，这可能需要等待一段时间")
            setCancelable(false)
            setPositiveButton("确定") { _, _ ->
                stopTest()

                if (flag == 1) {
                    recordSum = mmkv.getInt(ValueKey.deviceRecordSum, 0)
                    if (recordSum != 0) {
                        showProgressUI()
                        BleHelper.synFlag = BleHelper.synRecord
                        BleHelper.sendRecordMsg()
                        mTimer = Timer()
                        historyTask = HistoryTimerTask()
                        mTimer?.schedule(historyTask, 10 * 1000, 10 * 1000)
                        scope.launch(Dispatchers.IO) {
                            //清空正常数据
                            Repository.deleteAllRecords()
                            FileUtils.deleteSingleFile(FileUtils.sdPath + FileUtils.recordFileName)
                        }
                    } else {
                        ToastUtils.showShort("设备上未查询到数据")
                    }
                } else if (flag == 2) {
                    alarmSum = mmkv.getInt(ValueKey.deviceAlarmSum, 0)
                    if (alarmSum != 0) {
                        showProgressUI()
                        BleHelper.synFlag = BleHelper.synAlarm
                        BleHelper.sendAlarmMsg()
                        mTimer = Timer()
                        historyTask = HistoryTimerTask()
                        mTimer?.schedule(historyTask, 10 * 1000, 10 * 1000)
                        //清空报警数据
                        scope.launch(Dispatchers.IO) {
                            Repository.deleteAllAlarm()
                            FileUtils.deleteSingleFile(FileUtils.sdPath + FileUtils.AlarmFileName)
                        }
                    } else {
                        ToastUtils.showShort("设备上未查询到数据")
                    }
                } else if (flag == 3) {
                    showProgressUI()
                    mViewModel.sendSurveys()
                }
            }

            setNegativeButton("取消") { _, _ ->
            }
            show()
        }
    }

    private fun stopTest() {
        isRealTimeModel = false
        isRealing = false
        mViewBinding.testText.text = "开始"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.drawable.start_icon, null))

        mViewBinding.synLin.visibility = View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE

        realDataTask?.cancel()
        mTimer?.cancel()
    }

    private fun startTrack(){
        mViewModel.startLocation()
        mViewModel.startCollect()
    }

    private fun startTest() {
        //切换实时数据模式
        BleHelper.synFlag = "实时数据模式"
        isRealTimeModel = true
        isRealing = true
        mViewBinding.testText.text = "停止"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.drawable.pause_icon, null))
        //展示进度条
        mViewBinding.synLin.visibility = View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
        //发送请求实时数据指令
        BleHelper.addSendLinkedDeque(send10Msg)
        //开启超时监测
        mTimer = Timer()
        realDataTask = RealTimeDataTimerTask()
        mTimer?.schedule(realDataTask, 15 * 1000, 15 * 1000)
    }

    private fun showProgressUI() {
        mViewBinding.progressBar.progress = 0
        mViewBinding.synNumText.text = "0%"
        mViewBinding.numShowText.text = "0/0"

        mViewBinding.synLin.visibility = View.VISIBLE
        mViewBinding.progressBar.visibility = View.VISIBLE
        mViewBinding.numShowText.visibility = View.VISIBLE

        loadingDialogEntity.loadingType = LOADING_CUSTOM
        loadingDialogEntity.loadingMessage = "同步巡测信息中"
        loadingDialogEntity.isShow = true
        loadingDialogEntity.requestCode = "reqRecord"
        showCustomLoading(loadingDialogEntity)
    }

    private fun dismissProgressUI() {
        scope.launch (Dispatchers.Main){
            BleHelper.synFlag = ""
            mViewBinding.synLin.visibility = View.INVISIBLE
            mViewBinding.progressBar.visibility = View.INVISIBLE
            mViewBinding.numShowText.visibility = View.INVISIBLE

            dismissCustomLoading(loadingDialogEntity)
        }
    }

    private fun surVeyClick() {
        if(netConnectIsOK){
            isDrawPoint = true
            if (isSurveying){
                setEndState()
            }else {
                setStartState()
                startTrack()
            }
        }else{
            ToastUtils.showShort("网络未连接，请先连接网络")
        }
    }

    private fun setStartState(){
        mViewBinding.btnSurVey.text="结束"
        isSurveying = true
        trackBeginTime = System.currentTimeMillis()/1000
        scope.launch(Dispatchers.IO) {
            val mByte : ByteArray = byteArrayOf(
                0x55.toByte(),
                0x00.toByte(),
                0x0B.toByte(),
                0x09.toByte(),
                0x0C.toByte(),
                0x00.toByte(),
                0x02.toByte(),
            )
            val setPpmBytes = ByteArray(2)
            setPpmBytes.writeInt16LE(mmkv.getInt(ValueKey.ppmValue, 0))
            val realByte=mByte + setPpmBytes
            val startSurveyByte=realByte + Crc8.cal_crc8_t(realByte,realByte.size) + ByteUtils.FRAME_END
            mqttService.publish(startSurveyByte,2)
        }
    }


    private fun setEndState(){
        mViewBinding.btnSurVey.text="开始"
        isSurveying = false
        trackEndTime = System.currentTimeMillis()/1000
        scope.launch(Dispatchers.IO) {
            val mByte : ByteArray = byteArrayOf(
                0x55.toByte(),
                0x00.toByte(),
                0x0A.toByte(),
                0x09.toByte(),
                0x0D.toByte(),
                0x00.toByte(),
                0x01.toByte(),
                0x00.toByte(),
            )
            val endSurveyByte=mByte + Crc8.cal_crc8_t(mByte,mByte.size) + ByteUtils.FRAME_END
            mqttService.publish(endSurveyByte,2)
        }
    }

    override fun onDestroy() {
        BleHelper.gatt?.close()
        realDataTask?.cancel()
        historyTask?.cancel()
        blueConnectTask?.cancel()
        mTimer?.cancel()
        mActivity.unbindService(connection)

        isPollingModel=false
        //setEndState()
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mViewBinding.mMapView.onDestroy()
        //tt.onClose()
        "AmapTrackActivity onDestory".logE(LogFlag)
        super.onDestroy()
    }

    inner class RealTimeDataTimerTask : TimerTask() {
        override fun run() {
            if (isRecOK) {
                isRecOK = false
            } else {
                scope.launch(Dispatchers.Main) {
                    dismissProgressUI()
                    ToastUtils.showShort("数据接收错误,请重新尝试")
                    realDataTask?.cancel()
                    mTimer?.cancel()
                    stopTest()

                    ToastUtils.showShort("重连成功！")
                    startTest()
                }
            }
        }
    }

    inner class BlueToothConnectTimerTask : TimerTask() {
        override fun run() {
            if (isBleReady){
                blueConnectTask?.cancel()
                mTimer?.cancel()
            }else{
                scope.launch(Dispatchers.Main) {
                    dismissProgressUI()
                    ToastUtils.showShort("蓝牙连接失败,请重新尝试")
                    blueConnectTask?.cancel()
                    mTimer?.cancel()
                }
            }
        }
    }

    inner class HistoryTimerTask : TimerTask() {
        override fun run() {
//            val id = Thread.currentThread().id
//            "此时运行在${if (isMainThread()) "主线程" else "子线程"}中   线程号：$id".logE("LogFlag")
            if (isRecOK) {
                isRecOK = false
                retryFlagCount = 0
            } else {
                scope.launch(Dispatchers.Main) {
                    if (retryFlagCount < 4) {  //超时最多连续重发3次
                        retryFlagCount++
                        BleHelper.retryHistoryMessage()
                        "接收超时进行第 $retryFlagCount 次重发尝试".logE("LogFlag")
                    } else {
                        retryFlagCount = 0
                        dismissProgressUI()
                        ToastUtils.showShort("数据接收错误,请重新尝试")
                        historyTask?.cancel()
                        mTimer?.cancel()
                        "同步尝试超时".logE("LogFlag")
                    }
                }
            }
        }
    }

}