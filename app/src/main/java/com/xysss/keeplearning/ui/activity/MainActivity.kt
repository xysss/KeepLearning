package com.xysss.keeplearning.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //测试捕获异常
        CrashReport.testJavaCrash();
    }
}