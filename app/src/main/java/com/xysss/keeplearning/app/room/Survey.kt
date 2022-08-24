package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-08-24 14:38
 * 描述 : 描述
 */
@Entity
data class Survey(
    val beginTime: Long,
    val endTime: Long,
    val time: String,
    val concentrationValue: String,
    val ppm: String,
    val cf: String,
    val longitudeLatitude: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}