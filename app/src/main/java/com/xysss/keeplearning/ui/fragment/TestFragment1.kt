package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.ActivityListBinding
import com.xysss.keeplearning.ui.adapter.TestAdapter
import com.xysss.keeplearning.viewmodel.ListViewModel
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.util.decoration.DividerOrientation

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class TestFragment1 : BaseFragment<ListViewModel, ActivityListBinding>() {

    private val testAdapter: TestAdapter by lazy { TestAdapter(arrayListOf()) }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.listSmartRefresh.refresh {
            //刷新
            mViewModel.getList(true)
        }.loadMore {
            //加载更多
            mViewModel.getList(false)
        }
        //初始化 recycleView
        mViewBinding.listRecyclerView.grid(1).divider {
            orientation = DividerOrientation.GRID
            includeVisible = true
            setDivider(10,true)
            setColor(getColorExt(R.color.colorRed))
        }.adapter = testAdapter
    }

    /**
     * 懒加载 第一次获取视图的时候 触发
     */
    override fun lazyLoadData() {
        onLoadRetry()
    }

    /**
     * 请求成功
     */
    override fun onRequestSuccess() {
        mViewModel.listData.observe(this){
            //请求到列表数据
            testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
        }
    }

    /**
     * 请求失败
     * @param loadStatus LoadStatusEntity
     */
    override fun onRequestError(loadStatus: LoadStatusEntity) {
        when (loadStatus.requestCode) {
            NetUrl.HOME_LIST -> {
                //列表数据请求失败
                testAdapter.loadListError(loadStatus,mViewBinding.listSmartRefresh)
            }
        }
    }

    /**
     * 错误界面 空界面 点击重试
     */
    override fun onLoadRetry() {
        mViewModel.getList(isRefresh = true, loadingXml = true)
    }
}