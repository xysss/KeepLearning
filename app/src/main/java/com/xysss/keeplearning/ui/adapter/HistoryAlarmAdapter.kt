package com.xysss.keeplearning.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Record

/**
 * 作者 : xys
 * 时间 : 2022-02-16 14:13
 * 描述 : 描述
 */

class HistoryAlarmAdapter(data: ArrayList<Any>) :
    BaseQuickAdapter<Any, BaseViewHolder>(R.layout.history_item_alarm, data), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemAlarm = item as Alarm
        holder.setText(R.id.alarm_id, itemAlarm.id.toString()+".")
        holder.setText(R.id.alarm_value, itemAlarm.value)
        holder.setText(R.id.alarm_time, itemAlarm.timestamp)
        holder.setText(R.id.alarm_type, itemAlarm.type)
        holder.setText(R.id.alarm_state, "报警")
        //holder.setText(R.id.alarm_state, itemAlarm.state)
    }
}