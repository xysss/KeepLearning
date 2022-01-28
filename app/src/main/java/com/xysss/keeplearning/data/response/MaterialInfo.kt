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
    val concentrationNum:String,
    val concentrationState:String,
    val materialLibraryIndex:String,
    val concentrationUnit:String,
    val cfNum:String,
    val materialName:String) : Parcelable