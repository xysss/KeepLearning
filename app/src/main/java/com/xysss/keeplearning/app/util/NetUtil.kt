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
}