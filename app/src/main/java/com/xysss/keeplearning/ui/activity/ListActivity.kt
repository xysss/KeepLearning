package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.ActivityListBinding
import com.xysss.keeplearning.ui.adapter.TestAdapter
import com.xysss.keeplearning.viewmodel.ListViewModel
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.util.decoration.DividerOrientation

/**
 * Author:bysd-2
 * Time:2021/9/2811:07
 */
class ListActivity: BaseActivity<ListViewModel, ActivityListBinding>() {

    private  val testAdapter: TestAdapter by lazy { TestAdapter(arrayListOf()) }

    override fun initView(savedInstanceState: Bundle?) {
        mToolbar.initBack("列表界面") {
            finish()
        }
        mViewBinding.listSmartRefresh.refresh {
            //下拉刷新
            mViewModel.getList(true)
        }.loadMore {
            //上拉加载
            mViewModel.getList(false)
        }
        //初始化recyclerview
        mViewBinding.listRecyclerView.run {
            grid(3)
            divider {
                setColor(getColorExt(R.color.colorWhite))
                setDivider(10.dp)
                includeVisible = true
                orientation = DividerOrientation.GRID
            }
            adapter = testAdapter
        }
        //发起请求
        onLoadRetry()
    }

    /**
     * 请求成功
     */
    override fun onRequestSuccess() {
        mViewModel.listData.observe(this, Observer {
            //请求到列表数据
            testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
        })
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