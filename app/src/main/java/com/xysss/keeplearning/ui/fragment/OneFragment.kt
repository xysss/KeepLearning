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
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.swallowsonny.convertextlibrary.writeFloatLE
import com.swallowsonny.convertextlibrary.writeInt32LE
import com.swallowsonny.convertextlibrary.writeInt8
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Survey
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.app.util.Crc8
import com.xysss.keeplearning.app.util.FileUtils
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.databinding.FragmentOneBinding
import com.xysss.keeplearning.ui.activity.*
import com.xysss.keeplearning.ui.activity.gaode.AMapTrackActivity
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.LoadingType.Companion.LOADING_CUSTOM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.toHexString
import java.nio.ByteBuffer
import java.util.*

/**
 * Author:bysd-2
 * Time:2021/9/2811:15
 */

class OneFragment : BaseFragment<BlueToothViewModel, FragmentOneBinding>() {
    private var downloadApkPath = ""
    private lateinit var mService: MQTTService
    private var loadingDialogEntity = LoadingDialogEntity()
    private val send00Msg = "55000a0900000100"  //读取设备信息
    private val send10Msg = "55000a0910000100"  //读取实时数据
    private val send20Msg = "55000a0920000100"  //读取物质库信息
    private val send21Msg = "55000D0921000401000000"  //读取物质条目信息
    private var isRealing = false
    private var mTimer: Timer? = null
    private var historyTask: HistoryTimerTask? = null
    private var realDataTask: RealTimeDataTimerTask? = null
    private var retryFlagCount = 0

    private val connection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as MQTTService.MyBinder
            mService = mBinder.service
            mViewModel.putService(mService)

            if (mmkv.getString(ValueKey.deviceId,"")!=""){
                if (!isConnectMqtt){
                    recTopic= mmkv.getString(ValueKey.recTopicValue, "").toString()
                    sendTopic= mmkv.getString(ValueKey.sendTopicValue, "").toString()
                    mService.connectMqtt(appContext)
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

    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n", "UseCompatLoadingForDrawables")
    override fun initObserver() {
        super.initObserver()
        mViewModel.bleDate.observe(this) {
            mViewBinding.concentrationNum.text = it.concentrationNum
            mViewBinding.concentrationUnit.text = it.concentrationUnit
            mViewBinding.materialName.text = it.materialName
        }

        mViewModel.bleState.observe(this) {
            mViewBinding.blueTv.text = it
            if (it == "已连接设备") {
                isBleReady=true
                mViewBinding.blueTv.setTextColor(Color.parseColor("#4BDAFF"))
                mViewBinding.blueLinkImg.setImageDrawable(resources.getDrawable(R.drawable.connected_icon, null))

                dismissProgressUI()
            } else if (it == "未连接设备") {
                isBleReady=true
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

        mViewModel.numShow.observe(this) {
            mViewBinding.numShowText.text = it
        }
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
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
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
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewClick() {
        setOnclickNoRepeat(
            mViewBinding.loginBtn, mViewBinding.testPageBtn, mViewBinding.testListBtn,
            mViewBinding.testDownload, mViewBinding.testUpload, mViewBinding.testCrash,
            mViewBinding.getPermission, mViewBinding.testRoom, mViewBinding.linkBlueTooth,

            mViewBinding.blueLink, mViewBinding.testBackgroundImg, mViewBinding.toServiceBackImg,
            mViewBinding.synRecordBackgroundImg, mViewBinding.synAlarmBackgroundImg

        ) {
            when (it.id) {
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
                        if (!isRealing) startTest()
                        toStartActivity(AMapTrackActivity::class.java)
                    }else{
                        ToastUtils.showShort("请先连接蓝牙")
                    }
                }

                R.id.synRecordBackgroundImg -> {
                    if (isBleReady){
                        synMessage(1)
                    }else{
                        ToastUtils.showShort("请先连接蓝牙")
                    }
                }
                R.id.synAlarmBackgroundImg -> {
                    if (isBleReady){
                        synMessage(2)
                    }else{
                        ToastUtils.showShort("请先连接蓝牙")
                    }
                }

                //以下为demo按钮
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
                    scope.launch(Dispatchers.IO) {
                        val surveyList = Repository.loadAllSurvey()
                        if (surveyList.isNotEmpty()) {
                            for (i in surveyList.indices){
                                "mqtt : item :$i, 一共： ${surveyList.size}".logE(LogFlag)
                                subpackage(getHistorySurveyByte(surveyList[i]))
                            }
                        } else {
                            dismissProgressUI()
                            ToastUtils.showShort("设备上未查询到数据")
                        }
                    }
                }
            }

            setNegativeButton("取消") { _, _ ->
            }
            show()
        }
    }

    private suspend fun subpackage(byteArray: ByteArray){
        if (byteArray.size>1024){
            val mList=ByteArray(1024)
            var j=0
            for (i in byteArray.indices){
                mList[j]=byteArray[i]
                j++
                if (i%1023==0){
                    mService.publish(mList)
                    "mqtt : ${byteArray.size}  $i, $j, ${mList.size}".logE(LogFlag)
                    j=0
                    delay(200)
                }
            }
            if (mList.isNotEmpty()) mService.publish(mList)
        }else{
            mService.publish(byteArray)
        }
    }

    private fun getHistorySurveyByte(survey: Survey) : ByteArray{
        val sqlLatLngList = ArrayList<LatLng>()
        var sqlConValueList = ArrayList<String>()
        var sqlTimeList = ArrayList<String>()
        var sqlPpmList = ArrayList<String>()
        var sqlCfList = ArrayList<String>()

        val latlngs = survey.longitudeLatitude.trim()
        val conValue = survey.concentrationValue.trim()
        val time = survey.time.trim()
        val ppm = survey.ppm.trim()
        val cf = survey.cf.trim()

        if (conValue.isNotEmpty()){
            sqlConValueList = conValue.split(delim).toList() as ArrayList<String>
            sqlTimeList = time.split(delim).toList() as ArrayList<String>
            sqlPpmList = ppm.split(delim).toList() as ArrayList<String>
            sqlCfList = cf.split(delim).toList() as ArrayList<String>
        }
        if (latlngs.isNotEmpty()) {
            val lonlats = latlngs.split(delim).toTypedArray()
            if (lonlats.isNotEmpty()) {
                for (i in lonlats.indices) {
                    val lonlat = lonlats[i]
                    val split = lonlat.split(cutOff).toTypedArray()
                    if (split.isNotEmpty()) {
                        try {
                            sqlLatLngList.add(LatLng(split[0].toDouble(), split[1].toDouble()))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        val bytDatsSize = (sqlLatLngList.size * 57 + 12).toByte()
        val bytSize = (sqlLatLngList.size * 57 + 21).toByte()

        val mHeadByte : ByteArray = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            bytSize,
            0x09.toByte(),
            0x94.toByte(),
            0x00.toByte(),
            bytDatsSize
        )

        val beginTimeBytes = ByteArray(4)
        beginTimeBytes.writeInt32LE(survey.beginTime)
        val endTimeBytes = ByteArray(4)
        endTimeBytes.writeInt32LE(survey.endTime)
        val listSizeBytes = ByteArray(4)
        listSizeBytes.writeInt32LE(sqlLatLngList.size.toLong())

        var itemByteArray = ByteArray(sqlLatLngList.size * 57 )
        var index:Int = 0

        for (i in sqlLatLngList.indices){
            val timeStamp = ByteArray(4)
            timeStamp.writeInt32LE(if(sqlTimeList[i].isNotEmpty()) sqlTimeList[i].toLong() else 0)
            val conBytes = ByteArray(4)
            conBytes.writeFloatLE(if(sqlConValueList[i].isNotEmpty()) sqlConValueList[i].toFloat() else 0F)
            val stateBytes = ByteArray(4)
            stateBytes.writeInt32LE(1234)
            val indexBytes = ByteArray(4)
            stateBytes.writeInt32LE(5678)
            val ppmBytes = ByteArray(1)
            ppmBytes.writeInt8(if(sqlPpmList[i].isNotEmpty()) sqlPpmList[i].toInt() else 0)
            val cfBytes = ByteArray(4)
            cfBytes.writeFloatLE(if(sqlCfList[i].isNotEmpty()) sqlCfList[i].toFloat() else 0F)
            val nameBytes = ByteArray(20)
            val nBytes ="异丁烯".toByteArray()

            for (k in nameBytes.indices){
                if (k<nBytes.size){
                    nameBytes[k]=nBytes[k]
                }else{
                    nameBytes[k]=0
                }
            }

            val mLongitudeBytes = ByteArray(8)
            mLongitudeBytes.writeFloatLE(sqlLatLngList[i].longitude.toFloat())
            val mLatitudeBytes = ByteArray(8)
            mLatitudeBytes.writeFloatLE(sqlLatLngList[i].latitude.toFloat())

            val itemSurvey = timeStamp+conBytes+stateBytes+indexBytes+ppmBytes+cfBytes+nameBytes+mLongitudeBytes+mLatitudeBytes

            System.arraycopy(itemSurvey,0,itemByteArray,index,itemSurvey.size)
            index +=itemSurvey.size
        }

        val beforeCrcCheckBytes = mHeadByte + beginTimeBytes + endTimeBytes + listSizeBytes + itemByteArray

        val resultBytes = beforeCrcCheckBytes+ Crc8.cal_crc8_t(beforeCrcCheckBytes,beforeCrcCheckBytes.size) + ByteUtils.FRAME_END
        return BleHelper.transSendCoding(resultBytes)
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
        loadingDialogEntity.loadingMessage = "同步数据信息中"
        loadingDialogEntity.isShow = true
        loadingDialogEntity.requestCode = "reqRecord"
        showCustomLoading(loadingDialogEntity)
    }

    private fun dismissProgressUI() {
        BleHelper.synFlag = ""
        mViewBinding.synLin.visibility = View.INVISIBLE
        mViewBinding.progressBar.visibility = View.INVISIBLE
        mViewBinding.numShowText.visibility = View.INVISIBLE

        dismissCustomLoading(loadingDialogEntity)
    }

    override fun onDestroy() {
        BleHelper.gatt?.close()
        realDataTask?.cancel()
        historyTask?.cancel()
        mTimer?.cancel()
        appContext.unbindService(connection)
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