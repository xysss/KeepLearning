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


class HistoryRecordAdapter(data: ArrayList<Any>) : BaseQuickAdapter<Any, BaseViewHolder>(R.layout.history_item_record,data),
    LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemData=item as JoinResult
        holder.setText(R.id.item_todo_title, itemData.matterName)
        holder.setText(R.id.item_todo_id,itemData.id.toString())
        holder.setText(R.id.item_todo_date, itemData.timestamp)
        holder.setText(R.id.cfNum, itemData.cf)
        holder.setText(R.id.alarm, itemData.alarm)
        holder.setText(R.id.user_id, itemData.userId)
        holder.setText(R.id.place_id, itemData.placeId)
    }
}