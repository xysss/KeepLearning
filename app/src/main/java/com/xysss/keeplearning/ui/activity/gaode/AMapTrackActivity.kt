package com.xysss.keeplearning.ui.activity.gaode

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AlertDialog
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.ToastUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.databinding.ActivityAmapTrackBinding
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService.DataBinder
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService.RealLocationCallBack
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import getRouteWidth
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:12
 * 描述 : 描述
 */
class AMapTrackActivity : BaseActivity<AMapViewModel, ActivityAmapTrackBinding>() {

    //private lateinit var tt : TimeTask<TimeTask.Task>
    private lateinit var mapService: TrackCollectService
    private lateinit var mqttService: MQTTService

    private var isBeginning = false  //是否正在检测
    private var isStart = false  //是否开始检测
    private var yourChoice = 0

    private val mapConnection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as DataBinder
            mapService = mBinder.service
            mViewModel.putMapService(mapService)

            //mViewModel.setViewModelRealLocationListener()

            mapService.setRealLocationListener(object : RealLocationCallBack {
                override fun sendRealLocation(mlist: MutableList<LatLng>) {
                    drawMapLine(mlist)
                }
            })
        }

        //崩溃被杀掉的时候回调
        override fun onServiceDisconnected(name: ComponentName?) {
            mapService.stop()
        }
    }

    private val mqttConnection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as MQTTService.MyBinder
            mqttService = mBinder.service
            mViewModel.putMqttService(mqttService)

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

    @SuppressLint("CheckResult", "UseCompatLoadingForDrawables")
    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.initBack(getString(R.string.map_track_activity_title)) {
            ToastUtils.showShort("请点击结束按钮")
            //finish()
        }
        isPollingModel=true
        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mViewBinding.mMapView.onCreate(savedInstanceState)
        mViewBinding.mMapView.map.uiSettings.isZoomControlsEnabled = false

        RxPermissions(this).request(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe(Consumer<Boolean?> { aBoolean ->
            if (!aBoolean) {
                ToastUtils.showShort("没有相关权限")
            }
        })

//        tt = TimeTask(this, "abc", object : TimeTask.Task {
//            override fun exeTask() {
//                "exeTask>>>>>>>>>>>>".logE(LogFlag)
//            }
//            // 从API 19开始，最小执行时间是5S，如果小于5S，则设置无效，还是按照5S周期执行
//            override fun period(): Long {
//                return 10000L
//            }
//        })

        startTrackCollectService()
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.mRealTimeList.observe(this) {
            scope.launch(Dispatchers.IO) {
                //drawMapLine(it)
                val id = Thread.currentThread().id
                "此时运行在2${if (isMainThread()) "主线程" else "子线程"}中   线程号：$id".logE("LogFlag")
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.btnSurVey, mViewBinding.btnShow) {
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
            }
        }
    }

    private fun surVeyClick() {
        if (isBeginning){
            setEndState()
        }else {
            setStartState()
            mapService.start()
        }
    }

    private fun setEndState(){
        mViewBinding.btnSurVey.text="开始"
        isBeginning = false
        isStart = true
        trackEndTime = System.currentTimeMillis()
    }

    private fun setStartState(){
        mViewBinding.btnSurVey.text="结束"
        isBeginning = true
        isStart = true
        trackBeginTime = System.currentTimeMillis()
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
        yourChoice = -1
        val singleChoiceDialog = AlertDialog.Builder(this)
        singleChoiceDialog.setTitle("请选择")
        // 第二个参数是默认选项，此处设置
        singleChoiceDialog.setSingleChoiceItems(items, lastPpmValue) { _, which ->
            yourChoice = which
        }
        singleChoiceDialog.setPositiveButton("确定") { _, _ ->
            if (yourChoice != -1) {
                ToastUtils.showShort("你选择了" + items[yourChoice])
                when (yourChoice) {
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

    private fun onStopClick() {
        mapService.stop()
        finish()
    }

    //绑定服务
    private fun startTrackCollectService() {
        val intentService = Intent(appContext, TrackCollectService::class.java)
        bindService(intentService, mapConnection, BIND_AUTO_CREATE)

        val intentMqttService = Intent(appContext, MQTTService::class.java)
        bindService(intentMqttService, mqttConnection, BIND_AUTO_CREATE)
    }

    @SuppressLint("SimpleDateFormat")
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
        if (isStart) {
            if (isBeginning) {
                val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
                val markerBegin: Marker = mViewBinding.mMapView.map.addMarker(
                    MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker")
                )
            } else {
                val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
                val markerEnd: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))
                onStopClick()
            }
            isStart = false
        }
        //mMap.mapType=AMap.MAP_TYPE_NORMAL  //白昼地图（即普通地图）
        for (i in list.indices) {
            mBuilder.include(list[i])
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mViewBinding.mMapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        isPollingModel=false
        setEndState()
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mViewBinding.mMapView.onDestroy()
        //tt.onClose()
        unbindService(mapConnection)
        unbindService(mqttConnection)
        "AmapTrackActivity onDestory".logE(LogFlag)
    }

    override fun onResume() {
        super.onResume()
        // 在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mViewBinding.mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mViewBinding.mMapView.onPause()
    }

    override fun onBackPressed() {
        ToastUtils.showShort("请点击结束按钮")
        //super.onBackPressed()
    }
}
