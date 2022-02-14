package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.initBack
import com.xysss.keeplearning.databinding.FragmentThreeBinding
import com.xysss.keeplearning.ui.adapter.HistoryAdapter
import com.xysss.keeplearning.ui.adapter.TestAdapter
import com.xysss.keeplearning.viewmodel.HistoryViewModel
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.util.decoration.DividerOrientation
import jni.JniKit

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class ThreeFragment : BaseFragment<HistoryViewModel, FragmentThreeBinding>() {

    private  val testAdapter: HistoryAdapter by lazy { HistoryAdapter(arrayListOf()) }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_report)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorPrimary_20)

        mViewBinding.listSmartRefresh.refresh {
            //下拉刷新
            mViewModel.getRecordList(true)
        }.loadMore {
            //上拉加载
            mViewModel.getRecordList(false)
        }
        //初始化recyclerview
        mViewBinding.listRecyclerView.run {
            grid(1)
            divider {
                setColor(getColorExt(R.color.colorWhite))
                setDivider(10.dp)
                includeVisible = true
                orientation = DividerOrientation.GRID
            }
            adapter = testAdapter
        }
        //发起请求
        //onLoadRetry()

        mViewModel.recordListData.observe(this){
            it.logE("xysLog")
            //请求到列表数据
            if (it.datas.size==0)
                it.over=true
            testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
        }

    }

    /**
     * 请求成功
     */
//    override fun onRequestSuccess() {
//        mViewModel.recordListData.observe(this){
//            //请求到列表数据
//            testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
//        }
//    }

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
        mViewModel.getRecordList(isRefresh = true, loadingXml = true)
    }

//    override fun onResume() {
//        super.onResume()
//        immersionBar {
//            titleBar(mViewBinding.customToolbar)
//        }
//        //JNI TestDemo
//        val integers = intArrayOf(1, 2, 3, 4)
//        JniKit.setIntArray(integers).toString()
//        JniKit.setString("abc").toString()
//        JniKit.findClass()
//        mViewBinding.testInput1.text = JniKit.stringFromJNI()
//    }
}