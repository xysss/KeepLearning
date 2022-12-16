package com.xysss.keeplearning.app.location

import android.content.Context
import com.amap.api.location.AMapLocation
import java.util.regex.Pattern

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:52
 * 描述 : 描述
 */

class WifiAutoCloseDelegate : IWifiAutoCloseDelegate {
    /**
     * 请根据后台数据自行添加。此处只针对小米手机
     * @param context
     * @return
     */
    override fun isUseful(context: Context): Boolean {
        val manName: String = LocationUtil.getManufacture()
        val pattern = Pattern.compile("xiaomi", Pattern.CASE_INSENSITIVE)
        val m = pattern.matcher(manName)
        return m.find()
    }

    override fun initOnServiceStarted(context: Context) {
        LocationStatusManager.instance.initStateFromPreference(context)
    }

    override fun onLocateSuccess(context: Context, isScreenOn: Boolean, isMobileable: Boolean) {
        LocationStatusManager.instance.onLocationSuccess(context, isScreenOn, isMobileable)
    }

    override fun onLocateFail(
        context: Context,
        errorCode: Int,
        isScreenOn: Boolean,
        isWifiable: Boolean
    ) {

        //如果屏幕点亮情况下，因为断网失败，则表示不是屏幕点亮造成的断网失败，并修改参照值
        if (isScreenOn && errorCode == AMapLocation.ERROR_CODE_FAILURE_CONNECTION && !isWifiable) {
            LocationStatusManager.instance.resetToInit(context)
            return
        }
        if (!LocationStatusManager.instance.isFailOnScreenOff(errorCode, isScreenOn, isWifiable)
        ) {
            return
        }
        PowerManagerUtil.instance.wakeUpScreen(context)
    }
}
