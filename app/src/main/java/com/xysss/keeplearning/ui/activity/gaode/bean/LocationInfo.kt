package com.xysss.keeplearning.ui.activity.gaode.bean

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:25
 * 描述 : 描述
 */
data class LocationInfo(
    var lat: Double,
    var lon: Double,
    var time: Long,
    var concentrationValue: Float,
    var ppm: Int,
    var cf: Float
)
