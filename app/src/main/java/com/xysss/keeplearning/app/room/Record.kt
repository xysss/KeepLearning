package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-10 10:21
 * 描述 : 描述
 */

@Entity
data class Record(
    val timestamp: String,
    val reserv: String,
    val ppm: String,
    val cf: String,
    val voc_index_record: Int,  //物质库条目索引
    val alarm: String,  //报警状态
    val thresh_hi: String,  //报警阈值
    val thresh_lo: String,
    val thresh_twa: String,
    val thresh_stel: String,
    val userId: String,  //用户ID
    val placeId: String  //地点ID
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}