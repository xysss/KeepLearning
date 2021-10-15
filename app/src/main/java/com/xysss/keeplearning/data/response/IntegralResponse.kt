package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:bysd-2
 * Time:2021/10/1511:23
 */
/**
 * 积分
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class IntegralResponse(
    var coinCount: Int, //总积分
    var rank: Int, //当前排名
    var userId: Int,
    var username: String) : Parcelable
