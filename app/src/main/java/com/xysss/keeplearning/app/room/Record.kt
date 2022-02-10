package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-10 10:21
 * 描述 : 描述
 */

@Entity
data class Record(val timestamp:String,
                  val reserv:Int,
                  val ppm:String,
                  val cf:Float,
                  val voc_index:Int,  //物质库条目索引
                  val alarm:Int,  //报警状态
                  val thresh_hi:Float,  //报警阈值
                  val thresh_lo: Float,
                  val thresh_twa:Float,
                  val thresh_stel:Float,
                  val user_id:Int,  //用户ID
                  val place_id:Int,  //地点ID
                  val name:String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
}