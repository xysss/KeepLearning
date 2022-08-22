package com.xysss.keeplearning.ui.activity.gaode.collect

import android.annotation.SuppressLint
import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.ui.activity.gaode.bean.LocationInfo
import com.xysss.keeplearning.ui.activity.gaode.contract.ITripTrackCollection
import com.xysss.keeplearning.ui.activity.gaode.database.TripDBHelper
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:30
 * 描述 : 描述
 */
class TripTrackCollection : ITripTrackCollection{
    private var mContext: Context = appContext
    private var mlocationClient: AMapLocationClient? = null
    private var mAMapLocationListener: AMapLocationListener ? = null
    private var mLocations = Vector<LocationInfo>()
    private var mDataBaseThread: ScheduledExecutorService ? = null  // 入库线程
    var mVectorThread: ExecutorService? = null   // 入缓存线程
    private var isshowerror = true
    private var latLngList= ArrayList<LatLng>()
    private var testFlag:Double= 0.0


    private lateinit var realLocationCallBack: RealLocationCallBack

    fun setRealLocationListener(mListener: RealLocationCallBack) {
        this.realLocationCallBack = mListener
    }

    companion object{
        private var mTripTrackCollection: TripTrackCollection? = null
        fun getInstance(): TripTrackCollection? {
            if (mTripTrackCollection == null) {
                synchronized(TripTrackCollection::class.java) {
                    if (mTripTrackCollection == null) {
                        mTripTrackCollection = TripTrackCollection()
                    }
                }
            }
            return mTripTrackCollection
        }
    }

    // 开始采集数据
    override fun start() {
        startLocation()
        startCollect()
    }

    // 开启定位服务
    private fun startLocation() {
        "startLocation start...".logE(LogFlag)
        ToastUtils.showShort("开始采集")
        // 初始定位服务
        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(mContext)
        }
        // 初始化定位参数
        val mLocationOption = AMapLocationClientOption()
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.interval = 2000
        // 设置定位参数
        mlocationClient?.setLocationOption(mLocationOption)
        mLocationOption.isOnceLocation = false // 是否定位一次
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
        mlocationClient?.startLocation()

        // 设置定位监听
        mlocationClient?.setLocationListener { amapLocation ->
            if (amapLocation != null && amapLocation.errorCode == 0) {
                // 定位成功回调信息，设置相关消息
                // amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见定位类型表
                // amapLocation.getLatitude();// 获取纬度
                // amapLocation.getLongitude();// 获取经度
                // amapLocation.getAccuracy();// 获取精度信息
                mAMapLocationListener?.onLocationChanged(amapLocation)  // 显示系统小蓝点
                if (mVectorThread == null) {
                    mVectorThread = Executors.newSingleThreadExecutor()
                }

                "lat: +${ amapLocation.latitude+testFlag} lon: ${amapLocation.longitude+testFlag}".logE(LogFlag)

                testFlag += 0.00001

                // 避免阻塞UI主线程，开启一个单独线程来存入内存
                mVectorThread?.execute {
//                    val df = DecimalFormat("#.##")
//                    val latitudeAfter :Double= (amapLocation.latitude * 100.0).roundToInt() / 100.0
//                    val longitudeAfter :Double= (amapLocation.longitude * 100.0).roundToInt() / 100.0

                    mLocations.add(LocationInfo(amapLocation.latitude+testFlag, amapLocation.longitude+testFlag))

                    latLngList.add(LatLng(amapLocation.latitude+testFlag,amapLocation.longitude+testFlag))

                    if(latLngList.size >1){
                        realLocationCallBack.sendRealLocation(latLngList)
                        val temp = LatLng(latLngList[1].latitude,latLngList[1].longitude)
                        latLngList.clear()
                        latLngList.add(temp)
                    }
                }
            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                "location Error, ErrCode: ${amapLocation.errorCode}  errInfo:${amapLocation.errorInfo}".logE(LogFlag)
                if (isshowerror) {
                    isshowerror = false
                    ToastUtils.showShort(amapLocation.errorInfo)
                }
            }
        }
    }

    // 开启数据入库线程，五秒中入一次库
    @SuppressLint("SimpleDateFormat")
    private fun startCollect() {
        "startCollect start...".logE(LogFlag)
        if (mDataBaseThread == null) {
            mDataBaseThread = Executors.newSingleThreadScheduledExecutor()
        }
        mDataBaseThread?.scheduleWithFixedDelay({ // 取出缓存数据
            val stringBuffer = StringBuffer()
            for (i in mLocations.indices) {
                val locationInfo: LocationInfo = mLocations[i]
                stringBuffer.append(locationInfo.lat).append(",").append(locationInfo.lon)
                    .append("￥")
            }
            // 取完之后清空数据
            mLocations.clear()
            val trackid = SimpleDateFormat("yyyy-MM-dd").format(Date())
            TripDBHelper.getInstance()?.addTrack(trackid, trackid, stringBuffer.toString())
        }, (1000 * 20).toLong(), (1000 * 20).toLong(), TimeUnit.MILLISECONDS)
    }

    //停止采集
    @SuppressLint("SimpleDateFormat")
    override fun stop() {
        "stop start...".logE(LogFlag)
        ToastUtils.showShort("停止采集")
        if (mlocationClient != null) {
            mlocationClient?.stopLocation()
            mlocationClient = null
        }
        // 关闭Vector线程
        if (mVectorThread != null) {
            mVectorThread?.shutdownNow()
            mVectorThread = null
        }
        // 关闭SaveDabase线程
        if (mDataBaseThread != null) {
            mDataBaseThread?.shutdownNow()
            mDataBaseThread = null
        }
        // 定期任务关闭后，需要把最后的数据同步到数据库
        val stringBuffer = StringBuffer()
        for (i in mLocations.indices) {
            val locationInfo: LocationInfo = mLocations[i]
            stringBuffer.append(locationInfo.lat).append(",").append(locationInfo.lon)
                .append("￥")
        }
        // 取完之后清空数据
        mLocations.clear()
        val trackid = SimpleDateFormat("yyyy-MM-dd").format(Date())
        TripDBHelper.getInstance()?.addTrack(trackid, trackid, stringBuffer.toString())
    }

    override fun pause() {}

    override fun saveHoldStatus() {}


    override fun destory() {
        "destory start...".logE(LogFlag)
        stop()
    }

    interface RealLocationCallBack {
        fun sendRealLocation(mlist: MutableList<LatLng>)
    }
}
