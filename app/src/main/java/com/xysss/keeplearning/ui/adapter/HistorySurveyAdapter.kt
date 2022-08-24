package com.xysss.keeplearning.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.room.Survey
import com.xysss.keeplearning.app.util.ByteUtils

/**
 * 作者 : xys
 * 时间 : 2022-08-24 14:12
 * 描述 : 描述
 */
class HistorySurveyAdapter(data: ArrayList<Any>) : BaseQuickAdapter<Any, BaseViewHolder>(R.layout.history_item_survey,data), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemSurvey = item as Survey
        holder.setText(R.id.itemSurveyId, itemSurvey.id.toString()+". ")
        holder.setText(R.id.itemSurveyBeginTime, ByteUtils.getDateTime(itemSurvey.beginTime))
        holder.setText(R.id.itemSurveyEndTime, ByteUtils.getDateTime(itemSurvey.endTime))
        holder.setText(R.id.itemSurveyBeginTimeTitle, "开始时间:")
        holder.setText(R.id.itemSurveyEndTimeTitle, "结束时间:")
    }
}