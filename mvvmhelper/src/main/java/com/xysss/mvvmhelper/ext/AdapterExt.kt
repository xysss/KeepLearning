package com.xysss.mvvmhelper.ext

import com.chad.library.adapter.base.BaseQuickAdapter
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.xysss.mvvmhelper.entity.BasePage
import com.xysss.mvvmhelper.net.LoadStatusEntity

/**
 * Author:bysd-2
 * Time:2021/9/2717:13
 */

/**
 * 下拉刷新
 * @receiver SmartRefreshLayout
 * @param refreshAction Function0<Unit>
 * @return SmartRefreshLayout
 */
fun SmartRefreshLayout.refresh(refreshAction: () -> Unit = {}): SmartRefreshLayout {
    this.setOnRefreshListener {
        refreshAction.invoke()
    }
    return this
}

/**
 * 上拉加载
 * @receiver SmartRefreshLayout
 * @param loadMoreAction Function0<Unit>
 * @return SmartRefreshLayout
 */
fun SmartRefreshLayout.loadMore(loadMoreAction: () -> Unit = {}): SmartRefreshLayout {
    this.setOnLoadMoreListener {
        loadMoreAction.invoke()
    }
    return this
}

/**
 * 列表数据加载成功
 * @receiver BaseQuickAdapter<T,*>
 * @param baseListNetEntity BaseListNetEntity<T>
 */
fun <T> BaseQuickAdapter<T, *>.loadListSuccess(baseListNetEntity: BasePage<T>, smartRefreshLayout: SmartRefreshLayout) {
    //关闭头部刷新
    if (baseListNetEntity.isRefresh()) {
        //如果是第一页 那么设置最新数据替换
        this.setNewInstance(baseListNetEntity.getPageData())
        smartRefreshLayout.finishRefresh()
    } else {
        //不是第一页，累加数据
        this.addData(baseListNetEntity.getPageData())
        //刷新一下分割线
        this.recyclerView.post { this.recyclerView.invalidateItemDecorations() }
    }
    //如果还有下一页数据 那么设置 smartRefreshLayout 还可以加载更多数据
    if (baseListNetEntity.hasMore()) {
        smartRefreshLayout.finishLoadMore()
        smartRefreshLayout.setNoMoreData(false)
    } else {
        //如果没有更多数据了，设置 smartRefreshLayout 加载完毕 没有更多数据
        smartRefreshLayout.finishLoadMoreWithNoMoreData()
    }
}

/**
 * 列表数据请求失败
 * @receiver BaseQuickAdapter<*, *>
 * @param loadStatus LoadStatusEntity
 * @param status LoadService<*>
 * @param smartRefreshLayout SmartRefreshLayout
 */
fun BaseQuickAdapter<*, *>.loadListError(loadStatus: LoadStatusEntity, smartRefreshLayout: SmartRefreshLayout) {
    //关闭头部刷新
    if (loadStatus.isRefresh) {
        smartRefreshLayout.finishRefresh()
        //第一页，但是之前有数据，只给提示
        loadStatus.errorMessage.toast()
    } else {
        // 不是第一页请求，让recyclerview设置加载失败
        smartRefreshLayout.finishLoadMore(false)
        //给个错误提示
        loadStatus.errorMessage.toast()
    }
}