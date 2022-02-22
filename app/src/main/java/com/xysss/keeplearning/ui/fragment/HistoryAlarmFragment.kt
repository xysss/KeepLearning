package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentHistoryAlarmBinding
import com.xysss.keeplearning.ui.adapter.HistoryAlarmAdapter
import com.xysss.keeplearning.ui.adapter.HistoryRecordAdapter
import com.xysss.keeplearning.viewmodel.HistoryAlarmViewModel
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.util.decoration.DividerOrientation
import initFloatBtn

/**
 * 作者 : xys
 * 时间 : 2022-02-15 11:32
 * 描述 : 描述
 */
class HistoryAlarmFragment :BaseFragment<HistoryAlarmViewModel,FragmentHistoryAlarmBinding>(){

    private  val testAdapter: HistoryAlarmAdapter by lazy { HistoryAlarmAdapter(arrayListOf()) }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.listSmartRefresh.refresh {
            //下拉刷新
            mViewModel.getAlarmList(true)
        }.loadMore {
            //上拉加载
            mViewModel.getAlarmList(false)
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
            mViewBinding.listRecyclerView.initFloatBtn(mViewBinding.floatbtn)
        }

        mViewModel.getAlarmList(true)
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.alarmListData.observe(this){
            it.logE("xysLog")
            //请求到列表数据
            if (it.datas.size==0){
                if (it.isRefresh()){
                    showEmptyUi()
                }else{
                    it.over=true
                    showSuccessUi()
                    testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
                }
            }else{
                showSuccessUi()
                testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
            }
        }
    }

    override fun onLoadRetry() {
        showLoadingUi()
        mViewModel.getAlarmList(true)
    }

}