package com.xysss.keeplearning.data.model.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:bysd-2
 * Time:2021/9/2315:57
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