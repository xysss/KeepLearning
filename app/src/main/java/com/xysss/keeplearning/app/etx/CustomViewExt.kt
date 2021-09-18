package com.xysss.keeplearning.app.etx

import androidx.appcompat.widget.Toolbar
import com.xysss.jetpackmvvm.base.appContext
import com.xysss.keeplearning.app.util.SettingUtil

/**
 * Author:bysd-2
 * Time:2021/9/1717:42
 */

/**
 * 初始化普通的toolbar 只设置标题
 */
fun Toolbar.init(titleStr: String = ""): Toolbar {
    setBackgroundColor(SettingUtil.getColor(appContext))
    title = titleStr
    return this
}