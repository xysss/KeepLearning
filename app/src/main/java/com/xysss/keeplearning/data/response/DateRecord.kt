package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 作者 : xys
 * 时间 : 2022-01-24 16:18
 * 描述 : 描述
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class DateRecord(
    val timestamp:String,
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
    val place_id:Int  //地点ID
):Parcelable