package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.response.DeviceInfo
import com.xysss.keeplearning.databinding.FragmentThreeBinding
import com.xysss.keeplearning.viewmodel.TestViewModel
import com.xysss.mvvmhelper.ext.logE

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class ThreeFragment : BaseFragment<TestViewModel, FragmentThreeBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_report)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorPrimary_20)

        val deviceInfo= DeviceInfo(
        mmkv.getString(ValueKey.deviceHardwareVersion,"0"),
        mmkv.getString(ValueKey.deviceSoftwareVersion,"0"),
        mmkv.getInt(ValueKey.deviceBattery,0),
        mmkv.getInt(ValueKey.deviceFreeMemory,0),
        mmkv.getInt(ValueKey.deviceRecordSum,0),
        mmkv.getInt(ValueKey.deviceAlarmSum,0),
        mmkv.getInt(ValueKey.deviceCurrentRunningTime,0),
        mmkv.getInt(ValueKey.deviceCurrentAlarmNumber,0),
        mmkv.getInt(ValueKey.deviceCumulativeRunningTime,0),
        mmkv.getFloat(ValueKey.deviceDensityMax,0f),
        mmkv.getFloat(ValueKey.deviceDensityMin,0f),
        mmkv.getFloat(ValueKey.deviceTwaNumber,0f),
        mmkv.getFloat(ValueKey.deviceSteLNumber,0f),
        mmkv.getString(ValueKey.deviceId,"0")
        )

        deviceInfo.logE("xysLog")
        mViewBinding.testInput1.text = deviceInfo.toString()

    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
    }
}