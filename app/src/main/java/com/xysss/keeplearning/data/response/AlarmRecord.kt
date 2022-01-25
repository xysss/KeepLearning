package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 作者 : xys
 * 时间 : 2022-01-24 16:25
 * 描述 : 描述
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class AlarmRecord(
    val timestamp:String,
    val alarm:Int,  //报警状态，非0为报警
    val type:Int,  //报警类型
    val value: Int  //报警数值
): Parcelable