package com.xysss.keeplearning.app.util

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import com.xysss.keeplearning.R
import com.xysss.mvvmhelper.base.appContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:08
 * 描述 : 描述
 */
class DateDialog(var activity :Activity, var mcallback: Callback) : Dialog(appContext), OnDateChangedListener, OnTimeChangedListener{

    private var calendar: Calendar? = null
    private var simpleDateFormat: SimpleDateFormat? = null
    private var callback: Callback? = null
    private var datePicker: DatePicker? = null
    private var timePicker: TimePicker? = null
    private var dateTime: String? = null

    private var year = 0
    private var month = 0
    private var day = 0

    private var hour = 0
    private var minute = 0

    init {
        //super(activity, android.R.style.Theme_Holo_Light_Dialog)
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm")
        dateTime = simpleDateFormat!!.format(System.currentTimeMillis())
        this.setTitle(dateTime)
        this.callback = mcallback
        year = calendar!!.get(Calendar.YEAR)
        month = calendar!!.get(Calendar.MONTH)
        day = calendar!!.get(Calendar.DAY_OF_MONTH)
        hour = calendar!!.get(Calendar.HOUR_OF_DAY)
        minute = calendar!!.get(Calendar.MINUTE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_date)
        datePicker = findViewById<View>(R.id.date_picker) as DatePicker
        timePicker = findViewById<View>(R.id.time_picker) as TimePicker
        ViewUtil.resizePicker(datePicker)
        ViewUtil.resizePicker(timePicker)
        val cancelBtn = findViewById<View>(R.id.btn_cancel) as Button
        val sureBtn = findViewById<View>(R.id.btn_sure) as Button
        cancelBtn.setOnClickListener { dismiss() }
        sureBtn.setOnClickListener {
            if (null != callback) {
                val timeStamp = calendar!!.time.time / 1000
                callback!!.onDateCallback(timeStamp)
            }
            dismiss()
        }
        datePicker!!.init(year, month, day, this)
        timePicker!!.setOnTimeChangedListener(this)
        timePicker!!.setIs24HourView(true)
    }

    override fun onDateChanged(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
        updateDate()
    }

    override fun onTimeChanged(timePicker: TimePicker?, hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
        updateDate()
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    private fun updateDate() {
        calendar!![year, month, day, hour] = minute
        dateTime = simpleDateFormat!!.format(calendar!!.time)
        this.setTitle(dateTime)
    }

    interface Callback {
        fun onDateCallback(timeStamp: Long)
    }

}