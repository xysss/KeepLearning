package com.xysss.keeplearning.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.data.response.JoinResult

/**
 * 作者 : xys
 * 时间 : 2022-02-10 17:47
 * 描述 : 描述
 */


class HistoryRecordAdapter(data: ArrayList<Any>) : BaseQuickAdapter<Any, BaseViewHolder>(R.layout.history_item_record,data), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemData=item as JoinResult
        holder.setText(R.id.itemRecordName, itemData.matterName?:"异丁烯")
        holder.setText(R.id.itemRecordId,itemData.id.toString()+".")
        holder.setText(R.id.itemRecordDate, itemData.timestamp)
        holder.setText(R.id.cfNum, itemData.cf)
        if (itemData.alarm!="0"){
            holder.setText(R.id.alarm, "报警")
        }else{
            holder.setText(R.id.alarm, "正常")
        }
        holder.setText(R.id.user_id, itemData.userId)
        holder.setText(R.id.place_id, itemData.ppm)
    }
}