package com.xysss.keeplearning.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.bugly.crashreport.CrashReport
import com.xysss.keeplearning.R

/**
 * Author:bysd-2
 * Time:2021/9/1515:50
 */
class WelcomeActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //测试捕获异常
        //CrashReport.testJavaCrash();
    }
}