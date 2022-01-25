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
data class RecordingData(
    val timestamp:String,
    val reserv:String,
    val ppm:Int,
    val cf:Int,
    val voc_index:Int,  //物质库条目索引
    val alarm:Int,  //报警状态
    val thresh_hi:Int,  //报警阈值
    val thresh_lo: Int,
    val thresh_twa:Int,
    val thresh_stel:Int,
    val user_id:Int,  //用户ID
    val place_id:Int,  //地点ID
    val stelNumber:Int
):Parcelable