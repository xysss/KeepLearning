package com.xysss.keeplearning.ui.activity.gaode.contract

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:30
 * 描述 : 描述
 */
interface ITripTrackCollection { // 开始收集
    fun start()

    // 停止收集
    fun stop()

    // 暂停收集
    fun pause()

    // 保存当前的状态，和当前活动的Trip对象id至本地文件
    fun saveHoldStatus()

    //销毁
    fun destory()
}
