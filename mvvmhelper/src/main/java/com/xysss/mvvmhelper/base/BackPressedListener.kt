package com.xysss.mvvmhelper.base

/**
 * 作者 : xys
 * 时间 : 2022-11-30 13:45
 * 描述 : 描述
 */
/**
 * 监听activity的onBackPress事件
 */
interface BackPressedListener {
    /**
     * @return true代表响应back键点击，false代表不响应
     */
    fun handleBackPressed(): Boolean
}