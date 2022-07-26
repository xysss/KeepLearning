package com.xysss.keeplearning.ui.activity.baidumap

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.App
import com.xysss.keeplearning.app.ext.BitmapUtilInit
import com.xysss.keeplearning.app.ext.BitmapUtilclear
import com.xysss.keeplearning.app.util.BitmapUtil
import com.xysss.mvvmhelper.base.MvvmHelper.init
import com.xysss.mvvmhelper.util.XLog.init

/**
 * 作者 : xys
 * 时间 : 2022-07-26 16:22
 * 描述 : 描述
 */
class BaiduMapActivity : BaseActivity(){
    private var mReceiver: SDKReceiver? = null

    class SDKReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val s = intent.action
            when (s) {
                SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR -> {
                    ToastUtils.showShort("apikey验证失败，地图功能无法正常使用")
                }
                SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK -> {
                    ToastUtils.showShort("apikey验证成功")
                }
                SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR -> {
                    ToastUtils.showShort("网络错误")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // apikey的授权需要一定的时间，在授权成功之前地图相关操作会出现异常；apikey授权成功后会发送广播通知，我们这里注册 SDK 广播监听者
        val iFilter = IntentFilter()
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)
        mReceiver = SDKReceiver()
        registerReceiver(mReceiver, iFilter)
        init()
        BitmapUtil.init()
    }

    private fun init() {
        val trace = findViewById<View>(R.id.btn_trace) as Button
        val query = findViewById<View>(R.id.btn_query) as Button
        trace.setOnClickListener {
            val intent = Intent(this, TracingActivity::class.java)
            startActivity(intent)
        }
        query.setOnClickListener {
            val intent = Intent(this, TrackQueryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // 适配android M，检查权限
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toTypedArray(), 0)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_bai_du_map_trace
    }

    private fun isNeedRequestPermissions(permissions: MutableList<String>): Boolean {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION)
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE)
        return permissions.size > 0
    }

    private fun addPermission(permissionsList: MutableList<String>, permission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(permission)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        if (App.trackConf!!.contains("is_trace_started")
            && App.trackConf!!.getBoolean("is_trace_started", true)
        ) {
            // 退出app停止轨迹服务时，不再接收回调，将OnTraceListener置空
            App.mClient!!.setOnTraceListener(null)
            App.mClient!!.stopTrace(App.mTrace, null)
            App.mClient!!.clear()
        } else {
            App.mClient!!.clear()
        }
        App.isTraceStarted = false
        App.isGatherStarted = false
        val editor = App.trackConf!!.edit()
        editor.remove("is_trace_started")
        editor.remove("is_gather_started")
        editor.apply()
        BitmapUtil.clear()
    }

}