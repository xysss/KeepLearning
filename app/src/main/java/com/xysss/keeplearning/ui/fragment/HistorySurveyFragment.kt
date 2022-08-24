package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.databinding.FragmentHistorySurveyBinding
import com.xysss.keeplearning.ui.adapter.HistorySurveyAdapter
import com.xysss.keeplearning.viewmodel.HistorySurveyViewModel
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.util.decoration.DividerOrientation
import initFloatBtn

/**
 * 作者 : xys
 * 时间 : 2022-08-24 13:50
 * 描述 : 描述
 */
class HistorySurveyFragment : BaseFragment<HistorySurveyViewModel, FragmentHistorySurveyBinding>(){

    private  val testAdapter: HistorySurveyAdapter by lazy { HistorySurveyAdapter(arrayListOf()) }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBinding.listSmartRefresh.refresh {
            //下拉刷新
            mViewModel.getSurveyList(true)
        }.loadMore {
            //上拉加载
            mViewModel.getSurveyList(false)
        }
        //初始化recyclerview
        mViewBinding.listRecyclerView.run {
            grid(1)
            divider {
                setColor(getColorExt(R.color.color_8FF))
                setDivider(10.dp)
                includeVisible = true
                orientation = DividerOrientation.GRID
            }
            adapter = testAdapter
            mViewBinding.listRecyclerView.initFloatBtn(mViewBinding.floatbtn)
        }

        mViewModel.getSurveyList(true)
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.surveyListData.observe(this){
            //it.logE("LogFlag")
            //请求到列表数据
            if (it.datas.size==0){
                if (it.isRefresh()){
                    showEmptyUi()
                }else{
                    it.over=true
                    showSuccessUi()
                    testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
                }
            } else{
                showSuccessUi()
                testAdapter.loadListSuccess(it,mViewBinding.listSmartRefresh)
            }
        }
    }

    override fun onLoadRetry() {
        showLoadingUi()
        mViewModel.getSurveyList(true)
    }
}