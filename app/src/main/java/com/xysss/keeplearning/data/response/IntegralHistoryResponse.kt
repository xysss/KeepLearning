package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:bysd-2
 * Time:2021/10/1818:17
 */
/**
 * 积分记录
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class IntegralHistoryResponse(
    var coinCount: Int,
    var date: Long,
    var desc: String,
    var id: Int,
    var type: Int,
    var reason: String,
    var userId: Int,
    var userName: String) : Parcelable


