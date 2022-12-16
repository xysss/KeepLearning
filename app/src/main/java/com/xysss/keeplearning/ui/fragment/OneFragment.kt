package com.xysss.keeplearning.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.Context.POWER_SERVICE
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.gyf.immersionbar.ktx.immersionBar
import com.swallowsonny.convertextlibrary.writeInt16LE
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ble.BleDevice
import com.xysss.keeplearning.app.ble.BleDeviceAdapter
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.location.LocationService
import com.xysss.keeplearning.app.location.LocationStatusManager
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.Crc8
import com.xysss.keeplearning.app.util.FileUtils
import com.xysss.keeplearning.app.wheel.dialog.UpdateDialog
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.viewmodel.OneFragmentViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.getAppVersionCode
import com.xysss.mvvmhelper.ext.getAppVersionName
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.LoadingType.Companion.LOADING_CUSTOM
import getRouteWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
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
    private var isRealTesting = false
    private var mTimer: Timer? = null
    private var historyTask: HistoryTimerTask? = null
    private var realDataTask: RealTimeDataTimerTask? = null
    private var blueConnectTask: BlueToothConnectTimerTask? = null
    private var retryFlagCount = 0
    private var isDrawPoint = false  //是否要画起点和终点
    private var isSurveying = false  //是否巡测中
    private var ppmChoice = -1
    private lateinit var layNoEquipment : LinearLayout
    private lateinit var blueToothProgressBar : ProgressBar
    private lateinit var blueListRv : RecyclerView

    //默认蓝牙适配器
    private var defaultAdapter = BluetoothAdapter.getDefaultAdapter()
    //低功耗蓝牙适配器
    private lateinit var bleAdapter: BleDeviceAdapter
    //蓝牙列表
    private var mList: MutableList<BleDevice> = ArrayList()
    //地址列表
    private var addressList: MutableList<String> = ArrayList()
    //当前是否扫描
    private var isScanning = false
    //当前扫描设备是否过滤设备名称为Null的设备
    private var isScanNullNameDevice = false
    //当前扫描设备是否过滤设备信号值强度低于目标值的设备
    private var rssi = -100
    private lateinit var blueListDialog: AlertDialog
    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) ToastUtils.showShort(if (defaultAdapter.isEnabled) "蓝牙已打开" else "蓝牙未打开")
        }

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

        //检查版本
        mViewModel.checkVersion()
        //bugly进入首页检查更新
        //Beta.checkUpgrade(false, true)
        //开启服务
        val intentMqttService = Intent(appContext, MQTTService::class.java)
        bindService(intentMqttService, connection, Context.BIND_AUTO_CREATE)

        mViewModel.setCallBack()

//        //去连接蓝牙
//        val intentBle = Intent(appContext, LinkBleBlueToothActivity::class.java)
//        requestDataLauncher.launch(intentBle)
        //请求权限
        requestCameraPermissions()

        ignoreBatteryOptimization(mActivity)

        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mViewBinding.mMapView.onCreate(savedInstanceState)
        mViewBinding.mMapView.map.uiSettings.isZoomControlsEnabled = false

        mViewBinding.trackRl.visibility=View.GONE
        mViewBinding.functionCl.visibility=View.VISIBLE

        initBlueTooth()

        val intentFilter = IntentFilter()
        intentFilter.addAction(RECEIVER_ACTION)
        mActivity.registerReceiver(locationChangeBroadcastReceiver, intentFilter)
    }

    private val locationChangeBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == RECEIVER_ACTION) {
                val bd = intent.getBundleExtra("result")
                val latitude= bd?.getDouble("latitude")
                val longitude= bd?.getDouble("longitude")
                mViewModel.dealAMapLocation(longitude!!,latitude!!)
                mViewModel.startCollect()
            }
        }
    }

    private fun startLocationService() {
        mActivity.startService(Intent(mActivity, LocationService::class.java))
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
        mViewModel.appVersionInfo.observe(this) { appVersionInfo->
            if (appVersionInfo.appUrl.isNotEmpty()){
                "appVersionInfo: $appVersionInfo".logE(LogFlag)
                if (appVersionInfo.version.toInt()> getAppVersionCode(mActivity)){
                    appVersionInfo.logE(LogFlag)
                    // 升级对话框
                    UpdateDialog.Builder(mActivity)
                        // 版本名
                        .setVersionName("${getAppVersionName(mActivity)}.${appVersionInfo.version.toInt()}")
                        // 是否强制更新
                        .setForceUpdate(false)
                        // 更新日志
                        .setUpdateLog("修复一些已知问题")
                        // 下载 URL
                        .setDownloadUrl(appVersionInfo.appUrl)
                        // 文件 MD5
                        //.setFileMd5("df2f045dfa854d8461d9cefe08b813c8")
                        .show()
                }
            }
        }
        mViewModel.bleDate.observe(this) {
            mViewBinding.concentrationNum.text = it.concentrationNum
            mViewBinding.concentrationUnit.text = it.concentrationUnit
            mViewBinding.materialName.text = it.materialName
        }

        mViewModel.setRealLocationListener(object : OneFragmentViewModel.RealLocationCallBack {
            override fun sendRealLocation(mList: MutableList<LatLng>) {
                drawMapLine(mList)
            }
        })

//        mViewModel.latLngResultList.observe(this){
//            drawMapLine(it)
//        }

        mViewModel.bleState.observe(this) {
            mViewBinding.blueTv.text = it
            if (it == "已连接设备") {
                isBleReady=true
                mViewBinding.blueTv.setTextColor(Color.parseColor("#4BDAFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.drawable.connected_icon, null))
                blueListDialog.dismiss()
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

    /**
     * 忽略电池优化
     */
    fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager?
        var hasIgnored = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasIgnored = powerManager!!.isIgnoringBatteryOptimizations(activity.packageName)
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                try { //先调用系统显示 电池优化权限
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:" + activity.packageName)
                    startActivity(intent)
                } catch (e: Exception) { //如果失败了则引导用户到电池优化界面
                    try {
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addCategory(Intent.CATEGORY_LAUNCHER)
                        val cn = ComponentName.unflattenFromString("com.android.settings/.Settings\$HighPowerApplicationsActivity")
                        intent.component = cn
                        startActivity(intent)
                    } catch (ex: Exception) { //如果全部失败则说明没有电池优化功能
                    }
                }
            }
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
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).subscribe { aBoolean ->
            if (aBoolean) {
                ToastUtils.showShort("权限已经打开")
                val rxPermissionsBACKGROUND = RxPermissions(requireActivity())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    rxPermissionsBACKGROUND.request(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ).subscribe { mBoolean ->
                        if (mBoolean) {
                            ToastUtils.showShort("BACKGROUND权限已经打开")
                        } else {
                            ToastUtils.showShort("权限被拒绝")
                        }
                    }
                }
            } else {
                ToastUtils.showShort("权限被拒绝")
            }
        }
    }

    override fun onDestroyView() {
        mActivity.unregisterReceiver(locationChangeBroadcastReceiver)
        super.onDestroyView()
    }

    //扫描结果回调
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.device.name ?: "Unknown"
            addDeviceList(BleDevice(result.device, result.rssi, name))
        }
    }

    private fun openBluetooth() = defaultAdapter.let {
        if (it.isEnabled) ToastUtils.showShort("蓝牙已打开，可以开始扫描设备了") else activityResult.launch(
            Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        )
    }

    private fun initBlueTooth(){
        openBluetooth()
        mmkv.putString(ValueKey.SERVICE_UUID, sUUID)
        mmkv.putString(ValueKey.DESCRIPTOR_UUID, dUUID)
        mmkv.putString(ValueKey.CHARACTERISTIC_WRITE_UUID, wUUID)
        mmkv.putString(ValueKey.CHARACTERISTIC_INDICATE_UUID, rUUID)
        //默认过滤设备名为空的设备
        mmkv.putBoolean(ValueKey.NULL_NAME, true)
        mmkv.getInt(ValueKey.RSSI, 100)

        mmkv.putString(ValueKey.SERVICE_UUID, sUUID)
        mmkv.putString(ValueKey.DESCRIPTOR_UUID, dUUID)
        mmkv.putString(ValueKey.CHARACTERISTIC_WRITE_UUID, wUUID)
        mmkv.putString(ValueKey.CHARACTERISTIC_INDICATE_UUID, rUUID)

        bleAdapter = BleDeviceAdapter(mList).apply {
            setOnItemClickListener { _, _, position ->
                if (checkUuid()) {
                    stopScan()
                    blueListDialog.dismiss()
                    val device = mList[position].device
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
            animationEnable = true
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight)
        }
    }

    /**
     * 扫描蓝牙
     */
    private fun scan() {
        if (!defaultAdapter.isEnabled) {
            ToastUtils.showShort("蓝牙未打开");return
        }
        if (isScanning) {
            ToastUtils.showShort("正在扫描中...");return
        }
        isScanning = true
        addressList.clear()
        mList.clear()
        BluetoothLeScannerCompat.getScanner().startScan(scanCallback)
        blueToothProgressBar.visibility = View.VISIBLE
    }

    /**
     * 停止扫描
     */
    private fun stopScan() {
        if (!defaultAdapter.isEnabled) {
            ToastUtils.showShort("蓝牙未打开");return
        }
        if (isScanning) {
            isScanning = false
            BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
            blueToothProgressBar.visibility = View.INVISIBLE
        }
    }

    /**
     * 添加到设备列表
     */
    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    private fun addDeviceList(bleDevice: BleDevice) {
        //过滤设备名为null的设备
        if (mmkv.getBoolean(ValueKey.NULL_NAME, false) && bleDevice.device.name == null) {
            return
        }
        rssi = -mmkv.getInt(ValueKey.RSSI, 100)
        filterDeviceList()
        if (bleDevice.rssi < rssi) {
            return
        }
        //检查之前所添加的设备地址是否存在当前地址列表
        val address = bleDevice.device.address
        if (!addressList.contains(address)) {
            addressList.add(address)
            mList.add(bleDevice)
        }
        //无设备UI展示
        layNoEquipment.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
        //刷新列表适配器
        bleAdapter.notifyDataSetChanged()
    }
    /**
     * 过滤设备列表
     */
    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    private fun filterDeviceList() {
        if (mList.size > 0) {
            val mIterator = mList.iterator()
            while (mIterator.hasNext()) {
                val next = mIterator.next()
                if ((mmkv.getBoolean(ValueKey.NULL_NAME, false) && next.device.name == null) || next.rssi < rssi) {
                    addressList.remove(next.device.address)
                    mIterator.remove()
                }
            }
            bleAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 检查UUID
     */
    private fun checkUuid(): Boolean {
        val serviceUuid = mmkv.getString(ValueKey.SERVICE_UUID, "")
        val descriptorUuid = mmkv.getString(ValueKey.DESCRIPTOR_UUID, "")
        val writeUuid = mmkv.getString(ValueKey.CHARACTERISTIC_WRITE_UUID, "")
        val indicateUuid = mmkv.getString(ValueKey.CHARACTERISTIC_INDICATE_UUID, "")
        if (serviceUuid.isNullOrEmpty()) {
            ToastUtils.showShort("请输入Service UUID")
            return false
        }
        if (serviceUuid.length < 32) {
            ToastUtils.showShort("请输入正确的Service UUID")
            return false
        }
        if (descriptorUuid.isNullOrEmpty()) {
            ToastUtils.showShort("请输入Descriptor UUID")
            return false
        }
        if (descriptorUuid.length < 32) {
            ToastUtils.showShort("请输入正确的Descriptor UUID")
            return false
        }
        if (writeUuid.isNullOrEmpty()) {
            ToastUtils.showShort("请输入Characteristic Write UUID")
            return false
        }
        if (writeUuid.length < 32) {
            ToastUtils.showShort("请输入正确的Characteristic Write UUID")
            return false
        }
        if (indicateUuid.isNullOrEmpty()) {
            ToastUtils.showShort("请输入Characteristic Indicate UUID")
            return false
        }
        if (indicateUuid.length < 32) {
            ToastUtils.showShort("请输入正确的Characteristic Indicate UUID")
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewClick() {
        setOnclickNoRepeat(
            mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload, mViewBinding.testCrash,
            mViewBinding.getPermission, mViewBinding.testRoom, mViewBinding.linkBlueTooth,
            mViewBinding.blueLink, mViewBinding.testBackgroundImg, mViewBinding.toServiceBackImg,
            mViewBinding.synRecordBackgroundImg, mViewBinding.synAlarmBackgroundImg,
            mViewBinding.btnSurVey, mViewBinding.btnShow,mViewBinding.trackBackIv
        ) {
            when (it.id) {
                R.id.track_back_iv -> {
                    if (isSurveying){
                        ToastUtils.showShort("请先结束走航")
                    }else{
                        exitTrackUI()
                    }
                }
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
//                    val intentBle = Intent(appContext, LinkBleBlueToothActivity::class.java)
//                    requestDataLauncher.launch(intentBle)
                    val blueListBuilder = AlertDialog.Builder(mActivity)
                    val view = View.inflate(mActivity, R.layout.activity_link_bluetooth, null)
                    //        builder.setCancelable(false);  // 点外侧和返回键都不起作用；
                    blueListBuilder.setView(view)
                    blueListDialog = blueListBuilder.show()
                    blueListDialog.setCanceledOnTouchOutside(false) // 点外侧不消失，但返回键起作用
                    layNoEquipment = view.findViewById<View>(R.id.lay_no_equipment) as LinearLayout
                    blueToothProgressBar = view.findViewById<View>(R.id.progress_bar) as ProgressBar
                    blueListRv = view.findViewById<View>(R.id.rv_device) as RecyclerView
                    blueListRv.apply {
                        layoutManager = LinearLayoutManager(appContext)
                        adapter = bleAdapter
                    }
                    addressList.clear()
                    mList.clear()

                    if (isScanning){
                        stopScan()
                        scan()
                    }
                    else scan()
                }
                R.id.testBackgroundImg -> {
                    if (isBleReady){
                        if (!isRealTesting)
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
                        showTrackUI()
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

//                //以下为demo按钮
//                R.id.testRoom -> {
//                    toStartActivity(RoomSampleActivity::class.java)
//                }
//                R.id.loginBtn -> {
//                    toStartActivity(LoginActivity::class.java)
//                }
//                R.id.testPageBtn -> {
//                    toStartActivity(TestActivity::class.java)
//                }
//                R.id.testListBtn -> {
//                    toStartActivity(ListActivity::class.java)
//                }
//                R.id.linkBlueTooth -> {
//                    //toStartActivity(LinkBleBlueTooth::class.java)
//                }
//                R.id.testDownload -> {
//                    mViewModel.downLoad({
//                        //下载中
//                        mViewBinding.testUpdateText.text = "下载进度：${it.progress}%"
//                    }, {
//                        //下载完成
//                        downloadApkPath = it
//                        showDialogMessage("下载成功，路径为：${it}")
//                    }, {
//                        //下载失败
//                        showDialogMessage(it.msg)
//                    })
//                }
//                R.id.testUpload -> {
//                    mViewModel.upload(downloadApkPath, {
//                        //上传中 进度
//                        mViewBinding.testUpdateText.text = "上传进度：${it.progress}%"
//                    }, {
//                        //上传完成
//                        showDialogMessage("上传成功：${it}")
//                    }, {
//                        //上传失败
//                        showDialogMessage("${it.msg}--${it.message}")
//                    })
//                }
//                R.id.testCrash -> {
//                    //测试捕获异常
//                    CrashReport.testJavaCrash()
//                }
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
        val singleChoiceDialog = AlertDialog.Builder(mActivity)
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
        isRealTesting = false
        mViewBinding.testText.text = "开始"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.drawable.start_icon, null))
        mViewBinding.synLin.visibility = View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
        realDataTask?.cancel()
        mTimer?.cancel()
    }

    private fun startTrack(){
        mViewBinding.mMapView.map.clear()
        //mViewModel.startLocation()
        //mViewModel.startCollect()

        startLocationService()
    }

    private fun startTest() {
        //切换实时数据模式
        BleHelper.synFlag = "实时数据模式"
        isRealTimeModel = true
        isRealTesting = true
        mViewBinding.testText.text = "停止"
        mViewBinding.testImg.setImageDrawable(resources.getDrawable(R.drawable.pause_icon, null))
        //展示进度条
        mViewBinding.synLin.visibility = View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
        //发送请求实时数据指令
        BleHelper.addSendLinkedDeque(reqRealTimeDataMsg)
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
                if (!isRealTesting){
                    startTest()
                }
                sendStartState()
                startTrack()
            }
            LocationStatusManager.instance.resetToInit(appContext)
        }else{
            ToastUtils.showShort("网络未连接，请先连接网络")
        }
    }

    private fun sendStartState(){
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

    private fun exitTrackUI(){
        mViewBinding.functionCl.visibility=View.VISIBLE
        mViewBinding.trackRl.visibility=View.GONE
        isPollingModel=false
    }
    private fun showTrackUI(){
        mViewBinding.functionCl.visibility=View.GONE
        mViewBinding.trackRl.visibility=View.VISIBLE
        isPollingModel=true
    }

    override fun onPause() {
        super.onPause()
        stopScan()
    }

    override fun handleBackPressed(): Boolean {
        //处理自己的逻辑
        return if (isSurveying){
            ToastUtils.showShort("请先结束走航")
            true
        }else{
            exitTrackUI()
            false
        }
    }

    @SuppressLint("MissingPermission")
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

                    "数据接收15秒超时".logE(LogFlag)
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
                    if (retryFlagCount < 3) {  //超时最多连续重发3次
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