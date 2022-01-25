package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.app.ApplicationErrorReport
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 作者 : xys
 * 时间 : 2022-01-21 11:41
 * 描述 : 描述
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class DeviceInfo(
    val hardWareVersion:String,
    val softWareVersion:String,
//    val batteryInfo:Int,
//    val liberaMemoro:Int,
//    val dataNumber:Int,
//    val warnDataNumber:Int,
//    val theRunningTime:Int,
//    val theWarnNumber: Int,
//    val runningAllTime:Int,
//    val concentrationMax:Int,
//    val concentrationMin:Int,
//    val twaNumber:Int,
//    val stelNumber:Int,
    var deviceId: String) : Parcelable