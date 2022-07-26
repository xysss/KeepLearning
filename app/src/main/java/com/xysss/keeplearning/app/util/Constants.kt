package com.xysss.keeplearning.app.util

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:03
 * 描述 : 描述
 */
class Constants {

    companion object{
        val TAG = "BaiduTraceSDK_V3"

        val REQUEST_CODE = 1

        val RESULT_CODE = 1

        val DEFAULT_RADIUS_THRESHOLD = 0

        val PAGE_SIZE = 5000

        /**
         * 默认采集周期
         */
        val DEFAULT_GATHER_INTERVAL = 5

        /**
         * 默认打包周期
         */
        val DEFAULT_PACK_INTERVAL = 15

        /**
         * 实时定位间隔(单位:秒)
         */
        val LOC_INTERVAL = 5

        /**
         * 最后一次定位信息
         */
        val LAST_LOCATION = "last_location"
    }
}