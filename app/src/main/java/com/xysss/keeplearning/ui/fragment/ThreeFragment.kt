package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.appVersionCode
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.databinding.FragmentThreeBinding
import com.xysss.keeplearning.viewmodel.SettingViewModel
import com.xysss.mvvmhelper.base.appContext

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class ThreeFragment : BaseFragment<SettingViewModel, FragmentThreeBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_report)
        mViewBinding.customToolbar.setBackgroundResource(R.color.color_492)
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }
        //BleHelper.sendBlueToothMsg(reqDeviceMsg)
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.deviceData.observe(this){
            //it.logE("LogFlag")
            mViewBinding.settingTextViewValue1.text = it.deviceHardwareVersion
            mViewBinding.settingTextViewValue2.text = it.deviceSoftwareVersion
            mViewBinding.settingTextViewValueApp3.text = appVersionCode
            mViewBinding.settingTextViewValue3.text = it.deviceBattery.toString()
            mViewBinding.settingTextViewValue4.text = it.deviceFreeMemory.toString()
            mViewBinding.settingTextViewValue5.text = it.deviceRecordSum.toString()
            mViewBinding.settingTextViewValue6.text = it.deviceAlarmSum.toString()
            mViewBinding.settingTextViewValue7.text = ByteUtils.secondToTimes(it.deviceCurrentRunningTime)
            mViewBinding.settingTextViewValue8.text = it.deviceCurrentAlarmNumber.toString()
            mViewBinding.settingTextViewValue9.text = ByteUtils.secondToTimes(it.deviceCumulativeRunningTime)
            mViewBinding.settingTextViewValue10.text = it.deviceDensityMax
            mViewBinding.settingTextViewValue11.text = it.deviceDensityMin
            mViewBinding.settingTextViewValue12.text = it.deviceTwaNumber
            mViewBinding.settingTextViewValue13.text = it.deviceStelNumber
            mViewBinding.settingTextViewValue14.text = it.deviceId

        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getDeviceInfo()
    }
}