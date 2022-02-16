package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:41
 * 描述 : 描述
 */
@Entity
data class Matter(
    val timestamp: String,
    val reserv: String,
    val ppm: String,
    val cf: String,
    val voc_index: String,  //物质库条目索引
    val alarm: String,  //报警状态
    val thresh_hi: String,  //报警阈值
    val thresh_lo: String,
    val thresh_twa: String,
    val thresh_stel: String,
    val user_id: String,  //用户ID
    val place_id: String,  //地点ID
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}