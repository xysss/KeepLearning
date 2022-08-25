package com.xysss.keeplearning.ui.activity.gaode.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Survey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.ui.activity.gaode.bean.LocationInfo
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:32
 * 描述 : 描述
 */
class TrackCollectService : Service(){

    private val mBinder = DataBinder()

    private var mLocationClient: AMapLocationClient? = null
    private var mAMapLocationListener: AMapLocationListener? = null
    private var mLocations = Vector<LocationInfo>()
    private var mDataBaseThread: ScheduledExecutorService? = null  // 入库线程
    var mVectorThread: ExecutorService? = null   // 入缓存线程
    private var isshowerror = true
    private var latLngList= ArrayList<LatLng>()
    private var testFlag:Double= 0.0

    //数据库相关
    private var mTimeStringBuffer = StringBuffer()
    private var mConcentrationValueStringBuffer = StringBuffer()
    private var mPpmStringBuffer = StringBuffer()
    private var mCfStringBuffer = StringBuffer()
    private var mLongitudeLatitudeStringBuffer = StringBuffer()
    private var isStored=0

    private lateinit var newSurvey: Survey

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class DataBinder : Binder(){
        val service: TrackCollectService
            get() = this@TrackCollectService
    }


    private lateinit var realLocationCallBack: RealLocationCallBack

    fun setRealLocationListener(mListener: RealLocationCallBack) {
        this.realLocationCallBack = mListener
    }

    // 开始采集数据
    fun start() {
        startLocation()
        startCollect()
    }

    // 开启定位服务
    private fun startLocation() {
        "startLocation start...".logE(LogFlag)
        ToastUtils.showShort("开始采集")
        // 初始定位服务
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(appContext)
        }
        // 初始化定位参数
        val mLocationOption = AMapLocationClientOption()
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.interval = 2000
        // 设置定位参数
        mLocationClient?.setLocationOption(mLocationOption)
        mLocationOption.isOnceLocation = false // 是否定位一次
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
        mLocationClient?.startLocation()

        // 设置定位监听
        mLocationClient?.setLocationListener { amapLocation ->
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

                testFlag += 0.00001

                // 避免阻塞UI主线程，开启一个单独线程来存入内存
                mVectorThread?.execute {
                    val trackTime=System.currentTimeMillis()
                    val longitude = BigDecimal(amapLocation.longitude + testFlag).setScale(5, RoundingMode.FLOOR)
                    val latitude = BigDecimal(amapLocation.latitude + testFlag).setScale(5, RoundingMode.FLOOR)

                    "lat: +$latitude lon: $longitude".logE(LogFlag)

                    mLocations.add(LocationInfo(latitude.toDouble(),longitude.toDouble(),trackTime,concentrationValue,ppm,cf))
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
                "location Error, ErrCode: ${amapLocation.errorCode}  errInfo:${amapLocation.errorInfo}".logE(
                    LogFlag
                )
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
        if (mDataBaseThread == null) {
            mDataBaseThread = Executors.newSingleThreadScheduledExecutor()
        }
        mDataBaseThread?.scheduleWithFixedDelay({ // 取出缓存数据
            saveData()
        }, (1000 * 20).toLong(), (1000 * 20).toLong(), TimeUnit.MILLISECONDS)
    }

    fun saveData(){
        val timeStringBuffer = StringBuffer()
        val concentrationValueStringBuffer = StringBuffer()
        val ppmStringBuffer = StringBuffer()
        val cfStringBuffer = StringBuffer()
        val longitudeLatitudeStringBuffer = StringBuffer()

        for (i in mLocations.indices) {
            val locationInfo: LocationInfo = mLocations[i]
            timeStringBuffer.append(locationInfo.time).append(delim)
            concentrationValueStringBuffer.append(locationInfo.concentrationValue).append(delim)
            ppmStringBuffer.append(locationInfo.ppm).append(delim)
            cfStringBuffer.append(locationInfo.cf).append(delim)
            longitudeLatitudeStringBuffer.append(locationInfo.lat).append(cutOff).append(locationInfo.lon).append(delim)
        }
        // 取完之后清空数据
        mLocations.clear()

        val track = Survey(trackBeginTime,trackEndTime,timeStringBuffer.toString(),concentrationValueStringBuffer.toString(),ppmStringBuffer.toString(),
            cfStringBuffer.toString(),longitudeLatitudeStringBuffer.toString())

        addTrack(track)
    }

    //停止采集
    @SuppressLint("SimpleDateFormat")
    fun stop() {
        ToastUtils.showShort("停止采集")
        if (mLocationClient != null) {
            mLocationClient?.stopLocation()
            mLocationClient = null
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
        saveData()
    }

    interface RealLocationCallBack {
        fun sendRealLocation(mlist: MutableList<LatLng>)
    }

    private fun addTrack(track: Survey) {
        if (TextUtils.isEmpty(track.longitudeLatitude)) {
            return
        }
        try {
            // 查找库里面有没有之前存储过当前的数据
            if (track.beginTime!=0L) {
                isStored= Repository.forgetSurveyIsExist(track.beginTime)
            }
            // 如果之前存储过
            if (isStored==1){
                val surveySlq = Repository.getSurveyByBeginTime(track.beginTime)

                //time
                if (!TextUtils.isEmpty(surveySlq.time)) {
                    mTimeStringBuffer.append( surveySlq.time)
                    "mTime old data:$mTimeStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.time)) {
                    mTimeStringBuffer.append(track.time)
                    "mTime new data:$mTimeStringBuffer".logE(LogFlag)
                }

                //convalue
                if (!TextUtils.isEmpty(surveySlq.concentrationValue)) {
                    mConcentrationValueStringBuffer.append(surveySlq.concentrationValue)
                    "conValue old data:$mConcentrationValueStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.concentrationValue)) {
                    mConcentrationValueStringBuffer.append(track.concentrationValue)
                    "conValue new data:$mConcentrationValueStringBuffer".logE(LogFlag)
                }

                //ppmValue
                if (!TextUtils.isEmpty(surveySlq.ppm)) {
                    mPpmStringBuffer.append(surveySlq.ppm)
                    "ppmValue old data:$mPpmStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.ppm)) {
                    mPpmStringBuffer.append(track.ppm)
                    "ppmValue new data:$mPpmStringBuffer".logE(LogFlag)
                }

                //cfValue
                if (!TextUtils.isEmpty(surveySlq.cf)) {
                    mCfStringBuffer.append(surveySlq.cf)
                    "cfValue old data:$mCfStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.cf)) {
                    mCfStringBuffer.append(track.cf)
                    "cfValue new data:$mCfStringBuffer".logE(LogFlag)
                }

                //lnglats
                if (!TextUtils.isEmpty(surveySlq.longitudeLatitude)) {
                    mLongitudeLatitudeStringBuffer.append(surveySlq.longitudeLatitude)
                    "latlngs old data:$mLongitudeLatitudeStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.longitudeLatitude)) {
                    mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude)
                    "latlngs new data:$mLongitudeLatitudeStringBuffer".logE(LogFlag)
                }

                newSurvey = Survey(
                    track.beginTime,
                    track.endTime,
                    mTimeStringBuffer.toString(),
                    mConcentrationValueStringBuffer.toString(),
                    mPpmStringBuffer.toString(),
                    mCfStringBuffer.toString(),
                    mLongitudeLatitudeStringBuffer.toString()
                )
                Repository.updateSurvey(newSurvey)
            }else{
                newSurvey = Survey(
                    track.beginTime,
                    track.endTime,
                    mTimeStringBuffer.append(track.time).toString(),
                    mConcentrationValueStringBuffer.append(track.concentrationValue).toString(),
                    mPpmStringBuffer.append(track.ppm).toString(),
                    mCfStringBuffer.append(track.cf).toString(),
                    mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude).toString()
                )
                Repository.insertSurvey(newSurvey)
            }
        } catch (e: Exception) {
            "addTrack error:$e".logE(LogFlag)
            e.printStackTrace()
        } finally {
            if (!TextUtils.isEmpty(mTimeStringBuffer.toString())) {
                mTimeStringBuffer.delete(0, mTimeStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mConcentrationValueStringBuffer.toString())) {
                mConcentrationValueStringBuffer.delete(0, mConcentrationValueStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mPpmStringBuffer.toString())) {
                mPpmStringBuffer.delete(0, mPpmStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mCfStringBuffer.toString())) {
                mCfStringBuffer.delete(0, mCfStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mLongitudeLatitudeStringBuffer.toString())) {
                mLongitudeLatitudeStringBuffer.delete(0, mLongitudeLatitudeStringBuffer.toString().length)
            }
        }
    }

}
