package com.xysss.keeplearning.ui.activity.baidumap.model

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:05
 * 描述 : 描述
 */
class CurrentLocation {

    companion object{
        /**
         * 定位时间（单位：秒）
         */
        var locTime: Long = 0

        /**
         * 纬度
         */
        var latitude = 0.0

        /**
         * 经度
         */
        var longitude = 0.0
    }
}