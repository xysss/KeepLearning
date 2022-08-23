package com.xysss.keeplearning.app.room

/**
 * 作者 : xys
 * 时间 : 2022-08-23 11:46
 * 描述 : 描述
 */
data class Track(
    val beginTime: Long,
    val endTime: Long,
    val time: String,
    val concentrationValue: String,
    val ppm: String,
    val cf: String,
    val longitudeLatitude: String,
)