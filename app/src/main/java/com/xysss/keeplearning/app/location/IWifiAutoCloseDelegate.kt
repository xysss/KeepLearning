package com.xysss.keeplearning.app.location

import android.content.Context

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:48
 * 描述 : 代理类，用于处理息屏造成wifi被关掉时再重新点亮屏幕的逻辑
 */

open interface IWifiAutoCloseDelegate {
    /**
     * 判断在该机型下此逻辑是否有效。目前已知的系统是小米系统存在(用户自助设置的)息屏断掉wifi的功能。
     *
     * @param context
     * @return
     */
    fun isUseful(context: Context): Boolean

    /**
     * 点亮屏幕的服务有可能被重启。此处进行初始化
     *
     * @param context
     * @return
     */
    fun initOnServiceStarted(context: Context)

    /**
     * 定位成功时，如果移动网络无法访问，而且屏幕是点亮状态，则对状态进行保存
     */
    fun onLocateSuccess(context: Context, isScreenOn: Boolean, isMobileable: Boolean)

    /**
     * 对定位失败情况的处理
     */
    fun onLocateFail(context: Context, errorCode: Int, isScreenOn: Boolean, isWifiable: Boolean)
}
