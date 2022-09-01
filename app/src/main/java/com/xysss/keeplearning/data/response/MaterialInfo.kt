package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 作者 : xys
 * 时间 : 2022-01-21 14:59
 * 描述 : 描述
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class MaterialInfo(
    var concentrationNum:String,
    var concentrationState:String,
    var materialLibraryIndex:Int,
    var concentrationUnit:String,
    var cfNum:String,
    var materialName:String) : Parcelable