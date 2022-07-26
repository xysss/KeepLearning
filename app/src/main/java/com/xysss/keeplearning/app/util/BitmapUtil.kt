package com.xysss.keeplearning.app.util

import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.xysss.keeplearning.R

/**
 * 作者 : xys
 * 时间 : 2022-07-26 16:29
 * 描述 : 描述
 */
class BitmapUtil {
    companion object  {

        var bmArrowPoint: BitmapDescriptor? = null

        var bmStart: BitmapDescriptor? = null

        var bmEnd: BitmapDescriptor? = null

        /**
         * 创建bitmap，在MainActivity onCreate()中调用
         */
        fun init() {
            bmArrowPoint = BitmapDescriptorFactory.fromResource(R.mipmap.icon_point)
            bmStart = BitmapDescriptorFactory.fromResource(R.mipmap.icon_start)
            bmEnd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_end)
        }

        /**
         * 回收bitmap，在MainActivity onDestroy()中调用
         */
        fun clear() {
            bmArrowPoint!!.recycle()
            bmStart!!.recycle()
            bmEnd!!.recycle()
        }

    }
}