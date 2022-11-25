package com.xysss.keeplearning.app.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.telephony.TelephonyManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:02
 * 描述 : 描述
 */
class CommonUtil {

    companion object{

        fun getCurProcessName(context: Context): String? {
            val pid = Process.myPid()
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (appProcess in activityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    return appProcess.processName
                }
            }
            return ""
        }

        /**
         * 获取当前时间戳(单位：秒)
         *
         * @return
         */
        fun getCurrentTime(): Long {
            return System.currentTimeMillis() / 1000
        }

        /**
         * 校验double数值是否为0
         *
         * @param value
         *
         * @return
         */
        fun isEqualToZero(value: Double): Boolean {
            return if (Math.abs(value - 0.0) < 0.01) true else false
        }

        /**
         * 经纬度是否为(0,0)点
         *
         * @return
         */
        fun isZeroPoint(latitude: Double, longitude: Double): Boolean {
            return isEqualToZero(latitude) && isEqualToZero(longitude)
        }

        /**
         * 将字符串转为时间戳
         */
        fun toTimeStamp(time: String?): Long {
            val sdf = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.CHINA
            )
            val date: Date
            date = try {
                sdf.parse(time)
            } catch (e: ParseException) {
                e.printStackTrace()
                return 0
            }
            return date.time / 1000
        }

        /**
         * 获取设备IMEI码
         *
         * @param context
         *
         * @return
         */
        fun getImei(context: Context): String? {
            val imei: String? = try {
                (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
            } catch (e: Exception) {
                "myTrace"
            }
            return imei
        }



    }
}