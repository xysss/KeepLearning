package com.xysss.keeplearning.data.response

/**
 * Author:bysd-2
 * Time:2021/9/2811:05
 * 描述　: 玩Android 服务器返回的数据基类
 */
data class ApiResponse<T>(
    var data: T,
    var errorCode: Int = -1,
    var errorMsg: String = ""
)