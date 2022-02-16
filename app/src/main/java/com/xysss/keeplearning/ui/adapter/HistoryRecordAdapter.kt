package com.xysss.keeplearning.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.room.Record

/**
 * 作者 : xys
 * 时间 : 2022-02-10 17:47
 * 描述 : 描述
 */


class HistoryRecordAdapter(data: ArrayList<Any>) : BaseQuickAdapter<Any, BaseViewHolder>(R.layout.history_item_record,data),
    LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemRecord=item as Record
        holder.setText(R.id.item_todo_title, itemRecord.name)
        holder.setText(R.id.item_todo_id,itemRecord.id.toString())
        holder.setText(R.id.item_todo_date, itemRecord.timestamp)
        holder.setText(R.id.cfNum, itemRecord.cf)
        holder.setText(R.id.alarm, itemRecord.alarm)
        holder.setText(R.id.user_id, itemRecord.user_id)
        holder.setText(R.id.place_id, itemRecord.place_id)
    }
}