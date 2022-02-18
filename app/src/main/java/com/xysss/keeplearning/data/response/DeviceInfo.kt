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
    val deviceHardwareVersion:String?,
    val deviceSoftwareVersion:String?,
    val deviceBattery:Int,
    val deviceFreeMemory:Int,
    val deviceRecordSum:Int,
    val deviceAlarmSum:Int,
    val deviceCurrentRunningTime:Int,
    val deviceCurrentAlarmNumber: Int,
    val deviceCumulativeRunningTime:Int,
    val deviceDensityMax:Float,
    val deviceDensityMin:Float,
    val deviceTwaNumber:Float,
    val deviceStelNumber:Float,
    var deviceId: String?) : Parcelable