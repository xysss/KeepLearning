package com.xysss.keeplearning.ui.activity.gaode.collect

import com.xysss.keeplearning.ui.activity.gaode.bean.Trip

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:29
 * 描述 : 描述
 */
class LocalTripManage {
    private val trips // 本地多个对应的Trip对象,初始化时读区目录建立。;
            : List<Trip>? = null
    // 返回一个Trip，并添加至trips属性

    // 返回一个Trip，并添加至trips属性
    fun createTrip(): Trip {
        return Trip()
    }

    // 上传对象至服务器。
    fun upload() {}

    // 删除,判断当前状态进行本地。
    fun remove(trip: Trip?) {}

    // 根据tripID从云端下载至本地。
    fun download(TripID: String?) {}
}
