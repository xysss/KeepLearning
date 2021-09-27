package com.xysss.mvvmhelper.ext

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xysss.mvvmhelper.util.decoration.DefaultDecoration

/**
 * Author:bysd-2
 * Time:2021/9/2717:18
 */


/**
 * 纵向recyclerview
 * @receiver RecyclerView
 * @param baseAdapter BaseQuickAdapter<*, *>
 * @return RecyclerView
 */
fun RecyclerView.vertical(): RecyclerView {
    layoutManager = LinearLayoutManager(this.context)
    setHasFixedSize(true)
    return this
}

/**
 * 横向 recyclerview
 * @receiver RecyclerView
 * @return RecyclerView
 */
fun RecyclerView.horizontal(): RecyclerView {
    layoutManager = LinearLayoutManager(this.context).apply {
        orientation = RecyclerView.HORIZONTAL
    }
    setHasFixedSize(true)
    return this
}

/**
 * grid recyclerview
 * @receiver RecyclerView
 * @return RecyclerView
 */
fun RecyclerView.grid(count: Int): RecyclerView {
    layoutManager = GridLayoutManager(this.context, count)
    setHasFixedSize(true)
    return this
}

/**
 * 配置万能分割线
 * @receiver RecyclerView
 * @param block [@kotlin.ExtensionFunctionType] Function1<DefaultDecoration, Unit>
 * @return RecyclerView
 */
fun RecyclerView.divider(block: DefaultDecoration.() -> Unit): RecyclerView {
    val itemDecoration = DefaultDecoration(context).apply(block)
    addItemDecoration(itemDecoration)
    return this
}

