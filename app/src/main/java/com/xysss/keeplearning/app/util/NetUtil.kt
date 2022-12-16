package com.xysss.keeplearning.app.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:12
 * 描述 : 描述
 */
class NetUtil {
    companion object{
        val instance: NetUtil
            get() = Holder.instance
        /**
         * 检测网络状态是否联通
         *
         * @return
         */
        fun isNetworkAvailable(context: Context): Boolean {
            try {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val info = cm.activeNetworkInfo
                if (null != info && info.isConnected && info.isAvailable) {
                    return true
                }
            } catch (e: Exception) {
                return false
            }
            return false
        }
    }

    private object Holder {
        var instance = NetUtil()
    }

    /**
     * 是否手机信号可连接
     * @param context
     * @return
     */
    fun isMobileAva(context: Context): Boolean {
        var hasMobileCon = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfos = cm.allNetworkInfo
        for (net in netInfos) {
            val type = net.typeName
            if (type.equals("MOBILE", ignoreCase = true)) {
                if (net.isConnected) {
                    hasMobileCon = true
                }
            }
        }
        return hasMobileCon
    }

    /**
     * 是否wifi可连接
     * @param context
     * @return
     */
    fun isWifiCon(context: Context): Boolean {
        var hasWifoCon = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfos = cm.allNetworkInfo
        for (net in netInfos) {
            val type = net.typeName
            if (type.equals("WIFI", ignoreCase = true)) {
                if (net.isConnected) {
                    hasWifoCon = true
                }
            }
        }
        return hasWifoCon
    }
}