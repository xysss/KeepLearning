package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:33
 * 描述 : 描述
 */
@Entity
data class Alarm(
    val timestamp: String,
    val state: String,  //报警状态，非0为报警
    val type: String,  //报警类型
    val value: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}