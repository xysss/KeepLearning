package com.xysss.keeplearning.ui.activity.gaode

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.blankj.utilcode.util.ToastUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.ext.scope
import com.xysss.keeplearning.app.util.TimeTask
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.databinding.ActivityAmapTrackBinding
import com.xysss.keeplearning.ui.activity.gaode.collect.TripTrackCollection
import com.xysss.keeplearning.ui.activity.gaode.contract.ITripTrackCollection
import com.xysss.keeplearning.ui.activity.gaode.database.TripDBHelper
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:12
 * 描述 : 描述
 */
class AMapTrackActivity : BaseActivity<AMapViewModel, ActivityAmapTrackBinding>(),
    TripDBHelper.DrawMapCallBack, TripTrackCollection.RealLocationCallBack {

    //private lateinit var tt : TimeTask<TimeTask.Task>

    var mTrackCollection: ITripTrackCollection? = null
    private lateinit var mMap: AMap
    private var isFirst=false
    private var isEnd=false

    @SuppressLint("CheckResult")
    override fun initView(savedInstanceState: Bundle?) {
        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mViewBinding.mMapView.onCreate(savedInstanceState)
        mMap = mViewBinding.mMapView.map
        mMap.uiSettings.isZoomControlsEnabled = false
        startTrackCollectService()
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

        TripDBHelper.getInstance()?.setDrawMapCallBack(this)
        TripTrackCollection.getInstance()?.setRealLocationListener(this)
    }

    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.btnStart, mViewBinding.btnStop, mViewBinding.btnShow) {
            when (it.id) {
                R.id.btn_start -> {
                    onStartClick()
                    isFirst=true
                }
                R.id.btn_stop -> {
                    onStopClick()
                    isEnd=true
                }
                R.id.btn_show -> {
                    onShowClick()
                }
            }
        }
    }

    private fun onStartClick() {
        "thread: ${Thread.currentThread().id}".logE(LogFlag)
        // mTrackCollection = TripTrackCollection.getInstance(this);
        // mTrackCollection.start();
        mTrackCollection?.start()
    }

    private fun onStopClick() {
        mTrackCollection?.stop()
    }

    private fun onShowClick() {
        scope.launch(Dispatchers.IO) {
            val trackid = SimpleDateFormat("yyyy-MM-dd").format(Date())
            val track: MutableList<LatLng>? = TripDBHelper.getInstance()?.getTrack(trackid)
            showTrack(track)
        }
    }

    //启动轨迹信息收集服务
    private fun startTrackCollectService() {
        val intent = Intent(this, TrackCollectService::class.java)
        startService(intent)
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mTrackCollection = service as ITripTrackCollection
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }, Context.BIND_AUTO_CREATE)
    }

    private fun getRouteWidth(): Float {
        return 10f
    }

    private fun getDriveColor(): Int {

        val math = (Math.random() * 10).toInt()

        val locationRecNum=mmkv.getFloat(ValueKey.locationRecNum,0F)

        "轨迹过程中收到的数据： $locationRecNum".logE(LogFlag)
        val colorNum: Int = if (locationRecNum >0) {
            Color.parseColor("#2b9247")
        } else {
            Color.parseColor("#FF0000")
        }
        return colorNum
    }

    private fun drawMapLine(list: MutableList<LatLng>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        val mBuilder = LatLngBounds.Builder()
        val polylineOptions =
            PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
                .color(getDriveColor())
                .width(getRouteWidth())
                .addAll(list)
        //mMap.clear()
        mMap.addPolyline(polylineOptions)

//        if (isFirst){
//            val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
//            val markerBegin: Marker = mMap.addMarker(
//                MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker")
//            )
//        }
//        if (isEnd){
//            val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
//            val markerEnd: Marker =
//                mMap.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))
//        }

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
        mMap.animateCamera(cameraUpdate)
    }

    //轨迹展示
    private fun showTrack(list: MutableList<LatLng>?) {
        if (list == null || list.isEmpty()) {
            return
        }

        val mBuilder = LatLngBounds.Builder()
        val polylineOptions =
            PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
                .color(getDriveColor())
                .width(getRouteWidth())
                .addAll(list)
        mMap.clear()
        mMap.addPolyline(polylineOptions)

        val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
        val markerBegin: Marker = mMap.addMarker(MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker"))

        val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
        val markerEnd: Marker = mMap.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))

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
        mMap.animateCamera(cameraUpdate)
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

    override fun sendRealLocation(mlist: MutableList<LatLng>) {
        drawMapLine(mlist)
    }

    override fun realData(isRec: Boolean) {
        onShowClick()
    }
}
