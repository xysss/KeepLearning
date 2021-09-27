package com.xysss.keeplearning.data.response


/**
 * 时间　: 2021/09/27
 * 作者　: xys
 * 描述　: 玩Android 服务器返回的数据基类
 */
data class ApiResponse<T>(
    var data: T,
    var errorCode: Int = -1,
    var errorMsg: String = ""
)