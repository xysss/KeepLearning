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
    var beginTime: Long,
    var endTime: Long,
    var time: String,
    var concentrationValue: String,
    var ppm: String,
    var cf: String,
    var longitudeLatitude: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}