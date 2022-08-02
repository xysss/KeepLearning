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
import android.util.Log
import android.widget.Toast
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.PolylineOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.databinding.ActivityAmapTrackBinding
import com.xysss.keeplearning.ui.activity.gaode.contract.ITripTrackCollection
import com.xysss.keeplearning.ui.activity.gaode.database.TripDBHelper
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat
import io.reactivex.functions.Consumer
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:12
 * 描述 : 描述
 */
class AMapTrackActivity : BaseActivity<AMapViewModel, ActivityAmapTrackBinding>(){

    lateinit var mTrackCollection: ITripTrackCollection
    private lateinit var mMap: AMap
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
                Toast.makeText(this, "没有相关权限", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.btnStart,mViewBinding.btnStop,mViewBinding.btnShow) {
            when (it.id) {
                R.id.btn_start -> {
                    onStartClick()
                }
                R.id.btn_stop -> {
                    onStopClick()
                }
                R.id.btn_show -> {
                    onShowClick()
                }
            }
        }
    }

    private fun onStartClick() {
        Log.v("MYTAG", "thread:" + Thread.currentThread().id).logE()
        // mTrackCollection = TripTrackCollection.getInstance(this);
        // mTrackCollection.start();
        mTrackCollection.start()
    }

    private fun onStopClick() {
        mTrackCollection.stop()
    }

    private fun onShowClick() {
        val trackid = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val track: MutableList<LatLng?>? = TripDBHelper.getInstance()?.getTrack(trackid)
        showTrack(track)
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

    override fun onDestroy() {
        super.onDestroy()
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mViewBinding.mMapView.onDestroy()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mViewBinding.mMapView.onSaveInstanceState(outState)
    }

    private fun getRouteWidth(): Float {
        return 24f
    }

    private fun getDriveColor(): Int {
        return Color.parseColor("#D81B60")
    }


    //轨迹展示
    private fun showTrack(list: MutableList<LatLng?>?) {
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
        for (i in list.indices) {
            mBuilder.include(list[i])
        }
        Handler().postDelayed({
            val cameraUpdate: CameraUpdate
            // 判断,区域点计算出来,的两个点相同,这样地图视角发生改变,SDK5.0.0会出现异常白屏(定位到海上了)
            val northeast = mBuilder.build().northeast
            cameraUpdate = if (northeast != null && northeast == mBuilder.build().southwest) {
                CameraUpdateFactory.newLatLng(mBuilder.build().southwest)
            } else {
                CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 20)
            }
            mMap.animateCamera(cameraUpdate)
        }, 500)
    }

}
