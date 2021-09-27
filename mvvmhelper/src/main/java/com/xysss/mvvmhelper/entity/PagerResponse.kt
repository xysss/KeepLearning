package com.xysss.mvvmhelper.entity

/**
 * Author:bysd-2
 * Time:2021/9/2717:13
 */
/**
 * 分页帮助类
 */
abstract class  BasePage<T> {
    /**
     * 列表数据
     * @return ArrayList<T>
     */
    abstract fun getPageData():ArrayList<T>

    /**
     * 是否是第一页数据
     */
    abstract fun isRefresh(): Boolean
    /**
     * 数据是否为空
     */
    abstract fun isEmpty(): Boolean
    /**
     * 是否还有更多数据
     */
    abstract fun hasMore(): Boolean

}