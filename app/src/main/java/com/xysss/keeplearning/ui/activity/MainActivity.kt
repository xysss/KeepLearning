package com.xysss.keeplearning.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.job
import com.xysss.keeplearning.databinding.ActivityMainBinding
import com.xysss.keeplearning.ui.adapter.MainAdapter
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.net.manager.NetState
import initColorMap
import java.util.*

class MainActivity : BaseActivity<TestViewModel, ActivityMainBinding>() {
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

//        val intent = getAutostartSettingIntent()
//        startActivity(intent)

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
        super.onNetworkStateChanged(netState)
        if (netState.isSuccess) {
            ToastUtils.showShort("终于有网了!")
        } else {
            ToastUtils.showShort("网络无连接!")

        }
    }

    override fun showToolBar() = false

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
