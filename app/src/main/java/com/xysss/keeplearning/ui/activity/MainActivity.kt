package com.xysss.keeplearning.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.app.ext.appVersion
import com.xysss.keeplearning.app.ext.job
import com.xysss.keeplearning.app.ext.netConnectIsOK
import com.xysss.keeplearning.app.wheel.manager.ActivityManager
import com.xysss.keeplearning.app.wheel.other.DoubleClickHelper
import com.xysss.keeplearning.databinding.ActivityMainBinding
import com.xysss.keeplearning.ui.adapter.MainAdapter
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.base.BackPressedListener
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.toast
import com.xysss.mvvmhelper.net.manager.NetState
import com.xysss.mvvmhelper.net.manager.NetworkStateReceive
import initColorMap
import java.util.*

class MainActivity : BaseActivity<TestViewModel, ActivityMainBinding>() {
    private var netWorkReceiver: NetworkStateReceive? = null
    override fun initView(savedInstanceState: Bundle?) {
        //mToolbar.setCenterTitle(R.string.bottom_title_read)
        //进行竖向方向的滑动
        //mViewBinding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mViewBinding.mainViewPager.adapter = MainAdapter(this)
        mViewBinding.mainViewPager.offscreenPageLimit = mViewBinding.mainViewPager.adapter!!.itemCount
        mViewBinding.mainViewPager.isUserInputEnabled = false
        mViewBinding.mainNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationRead -> {
                    mViewBinding.mainViewPager.setCurrentItem(0, false)
                }
                R.id.navigationPaper -> {
                    mViewBinding.mainViewPager.setCurrentItem(1, false)
                }
                R.id.navigationReport -> {
                    mViewBinding.mainViewPager.setCurrentItem(2, false)
                }
//                R.id.navigationUser -> {
//                    mViewBinding.mainViewPager.setCurrentItem(3, false)
//                }
            }
            true
        }

        initColorMap()
        //动态注册网络状态监听广播
        netWorkReceiver = NetworkStateReceive()
        application.registerReceiver(
            netWorkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        val filter = IntentFilter()
        filter.apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        registerReceiver(netWorkReceiver, filter)

//        val intent = getAutostartSettingIntent()
//        startActivity(intent)

        try {
            val manager = this.packageManager
            val info = manager.getPackageInfo(this.packageName, 0)
            info.versionName
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                appVersion ="${info.versionName}.${info.longVersionCode}"
            }else{
                appVersion ="${info.versionName}.${info.versionCode}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取自启动管理页面的Intent
     *
     * @param context context
     * @return 返回自启动管理页面的Intent
     */
    private fun getAutostartSettingIntent(): Intent {
        var componentName: ComponentName? = null
        val brand = Build.MANUFACTURER
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        when (brand.lowercase(Locale.getDefault())) {
//                "samsung" -> componentName = ComponentName(
//                    "com.samsung.android.sm",
//                    "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"
//                )
//                "huawei" -> {
//                    Log.e("自启动管理 >>>>", "getAutostartSettingIntent: 华为")
//                    componentName = ComponentName(
//                        "com.huawei.systemmanager",
//                        "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
//                    )
//                }
//                "xiaomi" -> //                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
//                    componentName = ComponentName(
//                        "com.android.settings",
//                        "com.android.settings.BackgroundApplicationsManager"
//                    )
//                "vivo" -> //            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
//                    componentName = ComponentName(
//                        "com.iqoo.secure",
//                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
//                    )
//                "oppo" -> //            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
//                    componentName = ComponentName(
//                        "com.coloros.oppoguardelf",
//                        "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
//                    )
//                "yulong", "360" -> componentName = ComponentName(
//                    "com.yulong.android.coolsafe",
//                    "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
//                )
//                "meizu" -> componentName =
//                    ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
//                "oneplus" -> componentName = ComponentName(
//                    "com.oneplus.security",
//                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
//                )
//                "letv" -> {
//                    intent.action = "com.letv.android.permissionautoboot"
//                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
//                    intent.data = Uri.fromParts("package", appContext.packageName, null)
//                }
            else -> {
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", appContext.packageName, null)
            }
        }
        intent.component = componentName
        return intent
    }

    /**
     * 示例，在Activity/Fragment中如果想监听网络变化，可重写onNetworkStateChanged该方法
     */
    override fun onNetworkStateChanged(netState: NetState) {
        if (netState.isSuccess) {
            ToastUtils.showShort("网络连接成功!")
            "网络连接成功!".logE(LogFlag)
            netConnectIsOK=true

        } else {
            ToastUtils.showShort("网络无连接!")
            "网络无连接!".logE(LogFlag)
            netConnectIsOK=false
        }
    }
    /**
     * 拦截事件
     */
    private fun interceptBackPressed(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag("f${mViewBinding.mainViewPager.adapter?.getItemId(0)}")
        if (fragment is BackPressedListener) {
            if (fragment.handleBackPressed()) {
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        //判断fragment是否拦截
        if (!interceptBackPressed()) {
            if (!DoubleClickHelper.isOnDoubleClick()) {
                toast(R.string.home_exit_hint)
                return
            }
            // 移动到上一个任务栈，避免侧滑引起的不良反应
            moveTaskToBack(false)
            postDelayed({
                // 进行内存优化，销毁掉所有的界面
                ActivityManager.getInstance().finishAllActivities()
            }, 300)
        }
    }

    override fun showToolBar() = false

    override fun onDestroy() {
        job.cancel()
        application.unregisterReceiver(netWorkReceiver)
        super.onDestroy()
    }
}
