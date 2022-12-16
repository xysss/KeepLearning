package com.xysss.keeplearning.app.location

import android.content.Context
import com.amap.api.location.AMapLocation

/**
 * 作者 : xys
 * 时间 : 2022-12-16 14:50
 * 描述 : 在定位失败的情况下，用于判断当前定位错误是否是由于息屏导致的网络关闭引起的。
 * 判断逻辑仅限于处理设备仅有wifi信号的情况下
 */

open class LocationStatusManager {
    companion object {
        val instance: LocationStatusManager
            get() = Holder.instance

        /**
         * 过期时间为10分钟
         */
        private const val MINIMAL_EXPIRE_TIME = (30 * 60 * 1000).toLong()
        private val PREFER_NAME = LocationStatusManager::class.java.simpleName
        private const val DEF_PRIOR_TIME_VAL: Long = -1
    }

    /**
     * 上一次的定位是否成功
     */
    private var mPriorSuccLocated = false

    /**
     * 屏幕亮时可以定位
     */
    private var mPirorLocatableOnScreen = false

    internal object Holder {
        var instance = LocationStatusManager()
    }

    /**
     * 由于仅仅处理只有wifi连接的情况下，如果用户手机网络可连接，那么忽略。
     * 定位成功时，重置为定位成功的状态
     *
     * @param isScreenOn 当前屏幕是否为点亮状态
     * @param isMobileable 是否有手机信号
     */
    fun onLocationSuccess(context: Context, isScreenOn: Boolean, isMobileable: Boolean) {
        if (isMobileable) {
            return
        }
        mPriorSuccLocated = true
        if (isScreenOn) {
            mPirorLocatableOnScreen = true
            saveStateInner(context, true)
        }
    }

    /**
     * reset到默认状态
     *
     * @param context
     */
    fun resetToInit(context: Context) {
        mPirorLocatableOnScreen = false
        mPriorSuccLocated = false
        saveStateInner(context, false)
    }

    /**
     * 由preference初始化。特别是在定位服务重启的时候会进行初始化
     */
    fun initStateFromPreference(context: Context) {
        if (!isLocableOnScreenOn(context)) {
            return
        }
        mPriorSuccLocated = true
        mPirorLocatableOnScreen = true
    }

    /**
     * 判断是否由屏幕关闭导致的定位失败。
     * 只有在 网络可访问&&errorCode==4&&（priorLocated&&locatableOnScreen) && !isScreenOn 才认为是有息屏引起的定位失败
     * 如果判断条件较为严格，请按需要适当修改
     * @param errorCode  定位错误码, 0=成功， 4=因为网络原因造成的失败
     * @param isScreenOn 当前屏幕是否为点亮状态
     */
    fun isFailOnScreenOff(errorCode: Int, isScreenOn: Boolean, isWifiable: Boolean): Boolean {
        return !isWifiable && errorCode == AMapLocation.ERROR_CODE_FAILURE_CONNECTION && mPriorSuccLocated && mPirorLocatableOnScreen && !isScreenOn
    }

    /**
     * 是否存在屏幕亮而且可以定位的情况的key
     */
    private val IS_LOCABLE_KEY = "is_locable_key"

    /**
     * IS_LOCABLE_KEY 的过期时间
     */
    private val LOCALBLE_KEY_EXPIRE_TIME_KEY = "localble_key_expire_time_key"

    /**
     * 如果isLocable，则存入正确的过期时间，否则存默认值
     *
     * @param context
     * @param isLocable
     */
    fun saveStateInner(context: Context, isLocable: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(IS_LOCABLE_KEY, isLocable)
        editor.putLong(
            LOCALBLE_KEY_EXPIRE_TIME_KEY,
            if (isLocable) System.currentTimeMillis() else DEF_PRIOR_TIME_VAL
        )
        editor.commit()
    }

    /**
     * 从preference读取，判断是否存在网络状况ok，而且亮屏情况下，可以定位的情况
     */
    fun isLocableOnScreenOn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE)
        val res = sharedPreferences.getBoolean(IS_LOCABLE_KEY, false)
        val priorTime = sharedPreferences.getLong(LOCALBLE_KEY_EXPIRE_TIME_KEY, DEF_PRIOR_TIME_VAL)
        if (System.currentTimeMillis() - priorTime > MINIMAL_EXPIRE_TIME) {
            saveStateInner(context, false)
            return false
        }
        return res
    }
}
