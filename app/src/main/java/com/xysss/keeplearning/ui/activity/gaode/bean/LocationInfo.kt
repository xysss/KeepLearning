package com.xysss.keeplearning.ui.activity.gaode.bean

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:25
 * 描述 : 描述
 */
data class LocationInfo(
    var time: Long,
    var concentrationValue: Float,
    var alarmStatus: String,
    var index : Int,
    var ppm: Int,
    var cf: Float,
    var name: String,
    var lat: Double,
    var lon: Double
)