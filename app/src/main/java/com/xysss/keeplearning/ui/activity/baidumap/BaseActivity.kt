package com.xysss.keeplearning.ui.activity.baidumap

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xysss.keeplearning.R

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:25
 * 描述 : 描述
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentViewId())
    }

    /**
     * 获取布局文件ID
     */
    protected abstract fun getContentViewId(): Int

    /**
     * 设置Activity标题
     */
    override fun setTitle(resId: Int) {
        val layout = findViewById<View>(R.id.layout_top) as LinearLayout
        val textView = layout.findViewById<View>(R.id.tv_activity_title) as TextView
        textView.setText(resId)
    }

    /**
     * 设置点击监听器
     */
    fun setOnClickListener(listener: View.OnClickListener?) {
        val layout = findViewById<View>(R.id.layout_top) as LinearLayout
        val optionsButton = layout.findViewById<View>(R.id.btn_activity_options)
        optionsButton.setOnClickListener(listener)
    }

    /**
     * 不显示设置按钮
     */
    fun setOptionsButtonInVisible() {
        val layout = findViewById<View>(R.id.layout_top) as LinearLayout
        val optionsButton = layout.findViewById<View>(R.id.btn_activity_options)
        optionsButton.visibility = View.INVISIBLE
    }

    /**
     * 回退事件
     */
    fun onBack(v: View?) {
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}