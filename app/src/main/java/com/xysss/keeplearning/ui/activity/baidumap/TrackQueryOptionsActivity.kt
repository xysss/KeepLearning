package com.xysss.keeplearning.ui.activity.baidumap

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.util.CommonUtil
import com.xysss.keeplearning.app.util.Constants
import com.xysss.keeplearning.app.util.DateDialog
import java.text.SimpleDateFormat

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:45
 * 描述 : 描述
 */
class TrackQueryOptionsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener{

    private val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm")
    private var result: Intent? = null
    private var dateDialog: DateDialog? = null
    private var startTimeBtn: Button? = null
    private var endTimeBtn: Button? = null
    private var processedCBx: CheckBox? = null
    private var denoiseCBx: CheckBox? = null
    private var vacuateCBx: CheckBox? = null
    private var mapmatchCBx: CheckBox? = null
    private var radiusText: TextView? = null
    private var startTimeCallback: DateDialog.Callback? = null
    private var endTimeCallback: DateDialog.Callback? = null
    private var startTime: Long = CommonUtil.getCurrentTime()
    private var endTime: Long = CommonUtil.getCurrentTime()
    private var isProcessed = true
    private var isDenoise = false
    private var isVacuate = false
    private var isMapmatch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.track_query_options_title)
        setOptionsButtonInVisible()
        init()
    }

    private fun init() {
        result = Intent()
        startTimeBtn = findViewById<Button>(R.id.start_time)
        endTimeBtn = findViewById<Button>(R.id.end_time)
        processedCBx = findViewById<CheckBox>(R.id.processed)
        denoiseCBx = findViewById<CheckBox>(R.id.denoise)
        vacuateCBx = findViewById<CheckBox>(R.id.vacuate)
        mapmatchCBx = findViewById<CheckBox>(R.id.mapmatch)
        radiusText = findViewById<TextView>(R.id.radius_threshold)
        val startTimeBuilder = StringBuilder()
        startTimeBuilder.append(resources.getString(R.string.start_time))
        startTimeBuilder.append(simpleDateFormat.format(System.currentTimeMillis()))
        startTimeBtn!!.text = startTimeBuilder.toString()
        val endTimeBuilder = StringBuilder()
        endTimeBuilder.append(resources.getString(R.string.end_time))
        endTimeBuilder.append(simpleDateFormat.format(System.currentTimeMillis()))
        endTimeBtn!!.text = endTimeBuilder.toString()
        processedCBx!!.setOnCheckedChangeListener(this)
        denoiseCBx!!.setOnCheckedChangeListener(this)
        vacuateCBx!!.setOnCheckedChangeListener(this)
        mapmatchCBx!!.setOnCheckedChangeListener(this)
    }

    fun onStartTime(v: View?) {
        if (null == startTimeCallback) {
            startTimeCallback = object : DateDialog.Callback {
                override fun onDateCallback(timeStamp: Long) {
                    startTime = timeStamp
                    val startTimeBuilder = StringBuilder()
                    startTimeBuilder.append(resources.getString(R.string.start_time))
                    startTimeBuilder.append(simpleDateFormat.format(timeStamp * 1000))
                    startTimeBtn!!.text = startTimeBuilder.toString()
                }
            }
        }
        if (null == dateDialog) {
            dateDialog = DateDialog(this, startTimeCallback!!)
        } else {
            dateDialog!!.setCallback(startTimeCallback)
        }
        dateDialog!!.show()
    }

    fun onEndTime(v: View?) {
        if (null == endTimeCallback) {
            endTimeCallback = object : DateDialog.Callback {
                override fun onDateCallback(timeStamp: Long) {
                    endTime = timeStamp
                    val endTimeBuilder = StringBuilder()
                    endTimeBuilder.append(resources.getString(R.string.end_time))
                    endTimeBuilder.append(simpleDateFormat.format(timeStamp * 1000))
                    endTimeBtn!!.text = endTimeBuilder.toString()
                }
            }
        }
        if (null == dateDialog) {
            dateDialog = DateDialog(this, endTimeCallback!!)
        } else {
            dateDialog!!.setCallback(endTimeCallback)
        }
        dateDialog!!.show()
    }

    fun onCancel(v: View?) {
        super.onBackPressed()
    }

    fun onFinish(v: View?) {
        result!!.putExtra("startTime", startTime)
        result!!.putExtra("endTime", endTime)
        result!!.putExtra("processed", isProcessed)
        result!!.putExtra("denoise", isDenoise)
        result!!.putExtra("vacuate", isVacuate)
        result!!.putExtra("mapmatch", isMapmatch)
        val radiusStr = radiusText!!.text.toString()
        if (!TextUtils.isEmpty(radiusStr)) {
            try {
                result!!.putExtra("radius", radiusStr.toInt())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        setResult(Constants.RESULT_CODE, result)
        super.onBackPressed()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        when (compoundButton.id) {
            R.id.processed -> isProcessed = isChecked
            R.id.denoise -> isDenoise = isChecked
            R.id.vacuate -> isVacuate = isChecked
            R.id.mapmatch -> isMapmatch = isChecked
            else -> {}
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_trackquery_options
    }


}