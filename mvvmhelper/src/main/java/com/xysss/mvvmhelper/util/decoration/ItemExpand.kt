package com.xysss.mvvmhelper.util.decoration

/**
 * Author:bysd-2
 * Time:2021/9/2717:26
 */
/**
 * 可展开/折叠的条目
 */
interface ItemExpand {

    // 同级别的分组的索引位置
    var itemGroupPosition: Int

    // 是否已展开
    var itemExpand: Boolean

    // 子列表
    var itemSublist: List<Any?>?
}