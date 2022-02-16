package com.xysss.keeplearning.data.response

import android.annotation.SuppressLint
import android.os.Parcelable
import com.xysss.mvvmhelper.entity.BasePage
import kotlinx.android.parcel.Parcelize

/**
 * 作者 : xys
 * 时间 : 2022-01-24 16:18
 * 描述 : 描述
 */


data class DataHistoryResponse<T>(
    var datas: ArrayList<T>,
    var offset: Boolean,
    var over: Boolean,
) : BasePage<T>() {
    override fun getPageData() = datas
    override fun isRefresh() = offset
    override fun isEmpty() = datas.isEmpty()
    override fun hasMore() = !over
}