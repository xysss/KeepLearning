package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:bysd-2
 * Time:2021/10/1818:15
 */
/**
 * 搜索热词
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class SearchResponse(var id: Int,
                          var link: String,
                          var name: String,
                          var order: Int,
                          var visible: Int) : Parcelable
