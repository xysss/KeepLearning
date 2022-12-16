package com.xysss.keeplearning.app.location

import android.content.Intent
import android.os.Bundle
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.app.ext.RECEIVER_ACTION
import com.xysss.keeplearning.app.ext.materialInfo
import com.xysss.keeplearning.app.util.NetUtil
import com.xysss.keeplearning.ui.activity.gaode.bean.LocationInfo
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:49
 * 描述 : 描述
 */

class LocationService : NotifyService() {
    /**
     * 处理息屏关掉wifi的delegate类
     */
    private val mWifiAutoCloseDelegate: IWifiAutoCloseDelegate = WifiAutoCloseDelegate()
    private var mLocationClient: AMapLocationClient? = null
    private var locationCount = 0

    /**
     * 记录是否需要对息屏关掉wifi的情况进行处理
     */
    private var mIsWifiCloseable = false


    private var locationListener: AMapLocationListener = object : AMapLocationListener {
        override fun onLocationChanged(aMapLocation: AMapLocation) {
            //发送结果的通知
            sendLocationBroadcast(aMapLocation)
            if (!mIsWifiCloseable) {
                return
            }
            if (aMapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
                mWifiAutoCloseDelegate.onLocateSuccess(
                    applicationContext, PowerManagerUtil.instance.isScreenOn(applicationContext), NetUtil.instance.isMobileAva(applicationContext)
                )
            } else {
                mWifiAutoCloseDelegate.onLocateFail(
                    applicationContext,
                    aMapLocation.errorCode,
                    PowerManagerUtil.instance.isScreenOn(
                        applicationContext
                    ),
                    NetUtil.instance.isWifiCon(applicationContext)
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        applyNotiKeepMech() //开启利用notification提高进程优先级的机制
        if (mWifiAutoCloseDelegate.isUseful(applicationContext)) {
            mIsWifiCloseable = true
            mWifiAutoCloseDelegate.initOnServiceStarted(applicationContext)
        }
        startLocation()
        return START_STICKY
    }

    override fun onDestroy() {
        unApplyNotiKeepMech()
        stopLocation()
        super.onDestroy()
    }

    private fun sendLocationBroadcast(aMapLocation: AMapLocation) {
        //记录信息并发送广播
        locationCount++
        val callBackTime = System.currentTimeMillis()
        val sb = StringBuffer()
        sb.append("""定位完成 第${locationCount}次""".trimIndent())
        //sb.append("""回调时间: ${LocationUtil.formatUTC(callBackTime, null).toString()}     """.trimIndent())
        //sb.append(LocationUtil.getLocationStr(aMapLocation))
        "定位完成 $sb".logE(LogFlag)
        val mIntent = Intent(RECEIVER_ACTION)
        val mBundle = Bundle()
        mBundle.putDouble("latitude",aMapLocation.latitude)
        mBundle.putDouble("longitude",aMapLocation.longitude)
        mIntent.putExtra("result", mBundle)

        //发送广播
        sendBroadcast(mIntent)
    }

    /**
     * 启动定位
     */
    fun startLocation() {
        //stopLocation()
        ToastUtils.showShort("开始采集")
        AMapLocationClient.updatePrivacyAgree(appContext, true)
        AMapLocationClient.updatePrivacyShow(appContext, true, true)
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

        "startLocation start...".logE(LogFlag)
        // 设置定位监听
        mLocationClient?.setLocationListener { aMapLocation ->
            if (aMapLocation != null && aMapLocation.errorCode == 0) {
                //发送结果的通知
                "发送结果的通知...".logE(LogFlag)

                sendLocationBroadcast(aMapLocation)
//                if (!mIsWifiCloseable) {
//                    return
//                }
                if (aMapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
                    mWifiAutoCloseDelegate.onLocateSuccess(
                        applicationContext,
                        PowerManagerUtil.instance.isScreenOn(applicationContext),
                        NetUtil.instance.isMobileAva(applicationContext)
                    )
                } else {
                    mWifiAutoCloseDelegate.onLocateFail(
                        applicationContext,
                        aMapLocation.errorCode,
                        PowerManagerUtil.instance.isScreenOn(applicationContext),
                        NetUtil.instance.isWifiCon(applicationContext)
                    )
                }
            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                "location Error, ErrCode: ${aMapLocation.errorCode}  errInfo:${aMapLocation.errorInfo}".logE(LogFlag)
            }
        }

    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        if (mLocationClient!=null)
            mLocationClient?.stopLocation()
    }
}