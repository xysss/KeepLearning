package com.xysss.mvvmhelper.net

/**
 * Author:bysd-2
 * Time:2021/9/2717:23
 */
data class LoadingDialogEntity(
    @LoadingType var loadingType: Int = LoadingType.LOADING_NULL,
    var loadingMessage: String = "",
    var isShow: Boolean = false,
    var requestCode: String = "mmp"
)