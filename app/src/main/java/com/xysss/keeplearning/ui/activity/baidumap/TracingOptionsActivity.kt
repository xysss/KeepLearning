package com.xysss.keeplearning.ui.activity.baidumap

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.baidu.trace.model.LocationMode
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.util.Constants

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:44
 * 描述 : 描述
 */
class TracingOptionsActivity : BaseActivity(){

    // 返回结果
    private var result: Intent? = null

    private var gatherIntervalText: EditText? = null
    private var packIntervalText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.tracing_options_title)
        setOptionsButtonInVisible()
        init()
    }

    private fun init() {
        gatherIntervalText = findViewById(R.id.gather_interval) as EditText
        packIntervalText = findViewById(R.id.pack_interval) as EditText
        gatherIntervalText!!.onFocusChangeListener =
            OnFocusChangeListener { view, hasFocus ->
                val textView = view as EditText
                val hintStr = textView.hint.toString()
                if (hasFocus) {
                    textView.hint = ""
                } else {
                    textView.hint = hintStr
                }
            }
        packIntervalText!!.onFocusChangeListener =
            OnFocusChangeListener { view, hasFocus ->
                val textView = view as EditText
                val hintStr = textView.hint.toString()
                if (hasFocus) {
                    textView.hint = ""
                } else {
                    textView.hint = hintStr
                }
            }
    }

    fun onCancel(v: View?) {
        super.onBackPressed()
    }

    fun onFinish(v: View?) {
        result = Intent()
        val locationModeGroup = findViewById(R.id.location_mode) as RadioGroup
        val locationModeRadio = findViewById(locationModeGroup.checkedRadioButtonId) as RadioButton
        var locationMode = LocationMode.High_Accuracy //定位模式
        when (locationModeRadio.id) {
            R.id.device_sensors -> locationMode = LocationMode.Device_Sensors
            R.id.battery_saving -> locationMode = LocationMode.Battery_Saving
            R.id.high_accuracy -> locationMode = LocationMode.High_Accuracy
            else -> {}
        }
        result!!.putExtra("locationMode", locationMode.name)
        val gatherIntervalText = findViewById(R.id.gather_interval) as EditText
        val packIntervalText = findViewById(R.id.pack_interval) as EditText
        val gatherIntervalStr = gatherIntervalText.text.toString()
        val packIntervalStr = packIntervalText.text.toString()
        if (!TextUtils.isEmpty(gatherIntervalStr)) { //采集频率
            try {
                result!!.putExtra("gatherInterval", gatherIntervalStr.toInt())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (!TextUtils.isEmpty(packIntervalStr)) { //打包频率
            try {
                result!!.putExtra("packInterval", packIntervalStr.toInt())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        setResult(Constants.RESULT_CODE, result)
        super.onBackPressed()
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_tracing_options
    }

}