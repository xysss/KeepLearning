package com.xysss.keeplearning.ui.activity.gaode

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.ToastUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.app.ext.isMainThread
import com.xysss.keeplearning.app.ext.scope
import com.xysss.keeplearning.databinding.ActivityAmapTrackBinding
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService.DataBinder
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService.RealLocationCallBack
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:12
 * 描述 : 描述
 */
class AMapTrackActivity : BaseActivity<AMapViewModel, ActivityAmapTrackBinding>(){

    //private lateinit var tt : TimeTask<TimeTask.Task>
    private lateinit var mService: TrackCollectService
    private var isBeginning=false
    private var isStart=false

    private lateinit var intentService :Intent

    private val connection = object : ServiceConnection {
        //与服务绑定成功的时候自动回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val mBinder = service as DataBinder
            mService = mBinder.service
            mViewModel.putService(mService)

            //mViewModel.setRealLocationListener()

            mService.setRealLocationListener(object : RealLocationCallBack {
                override fun sendRealLocation(mlist: MutableList<LatLng>) {
                    drawMapLine(mlist)
                }
            })
        }
        //崩溃被杀掉的时候回调
        override fun onServiceDisconnected(name: ComponentName?) {
            mService.stop()
        }
    }

    @SuppressLint("CheckResult")
    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.initBack(getString(R.string.bottom_title_navigation)) {
            finish()
        }
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

        mViewModel.initColorMap()
    }

    override fun initObserver() {
        super.initObserver()

        mViewModel.track.observe(this){
            showTrack(it)
        }
        mViewModel.mRealTimeList.observe(this){
//            Thread {
//                drawMapLine(it)
//            }
//            scope.launch(Dispatchers.IO) {
//                drawMapLine(it)
//                val id = Thread.currentThread().id
//            "此时运行在1${if (isMainThread()) "主线程" else "子线程"}中   线程号：$id".logE("LogFlag")
//            }
            scope.launch(Dispatchers.IO) {
                drawMapLine(it)
                val id = Thread.currentThread().id
                "此时运行在2${if (isMainThread()) "主线程" else "子线程"}中   线程号：$id".logE("LogFlag")
            }
        }
    }

    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.btnStart, mViewBinding.btnStop, mViewBinding.btnShow) {
            when (it.id) {
                R.id.btn_start -> {
                    isBeginning=true
                    isStart=true
                    onStartClick()
                }
                R.id.btn_stop -> {
                    isBeginning=false
                    isStart=true
                }
                R.id.btn_show -> {
                    mViewModel.onShowClick()
                }
            }
        }
    }

    private fun onStartClick() {
        "thread: ${Thread.currentThread().id}".logE(LogFlag)
        mService.start()
    }

    private fun onStopClick() {
        mService.stop()
    }

    //启动轨迹信息收集服务
    private fun startTrackCollectService() {
        intentService = Intent(this, TrackCollectService::class.java)
        bindService(intentService, connection, BIND_AUTO_CREATE)
    }

    private fun drawMapLine(list: MutableList<LatLng>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        val mBuilder = LatLngBounds.Builder()
        val polylineOptions =
            PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
                .color(mViewModel.getDriveColor(10))
                .width(mViewModel.getRouteWidth())
                .addAll(list)
        //mMap.clear()
        mViewBinding.mMapView.map.addPolyline(polylineOptions)
        if (isStart){
            if (isBeginning){
                val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
                val markerBegin: Marker = mViewBinding.mMapView.map.addMarker(
                    MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker")
                )
            } else{
                val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
                val markerEnd: Marker =
                    mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))

                onStopClick()
            }
            isStart=false
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

    //轨迹展示
    private fun showTrack(list: MutableList<LatLng>?) {
        if (list == null || list.isEmpty()) {
            return
        }

        val mBuilder = LatLngBounds.Builder()
        val polylineOptions =
            PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
                .color(mViewModel.getDriveColor(10))
                .width(mViewModel.getRouteWidth())
                .addAll(list)
        mViewBinding.mMapView.map.clear()
        mViewBinding.mMapView.map.addPolyline(polylineOptions)

        val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
        val markerBegin: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker"))

        val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
        val markerEnd: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))

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
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mViewBinding.mMapView.onDestroy()
        //tt.onClose()
        unbindService(connection)
        stopService(intentService)
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
        "按下返回按键".logE(LogFlag)
        super.onBackPressed()
    }
}
