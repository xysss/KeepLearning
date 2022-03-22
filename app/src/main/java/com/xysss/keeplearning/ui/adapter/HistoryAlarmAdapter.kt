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
        when (itemAlarm.type) {

        }
        holder.setText(R.id.itemAlarmId, itemAlarm.id.toString() + ".")
        holder.setText(R.id.itemAlarmValue, itemAlarm.value)
        holder.setText(R.id.itemAlarmTime, itemAlarm.timestamp)
        holder.setText(R.id.itemAlarmType, getType(itemAlarm.type))
        holder.setText(R.id.itemAlarmState, "报警")
        //holder.setText(R.id.alarm_state, itemAlarm.state)
    }

    private fun getType(type: String) = when (type) {
        "1" -> "TWA"
        "2" -> "STEL"
        "4" -> "HI"
        "8" -> "LO"
        "10" -> "FALL"
        else -> "未知"
    }
}