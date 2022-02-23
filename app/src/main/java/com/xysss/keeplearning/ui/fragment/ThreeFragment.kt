package com.xysss.keeplearning.ui.fragment

import android.os.Bundle
import com.gyf.immersionbar.ktx.immersionBar
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseFragment
import com.xysss.keeplearning.app.ext.reqDeviceMsg
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils
import com.xysss.keeplearning.databinding.FragmentThreeBinding
import com.xysss.keeplearning.viewmodel.SettingViewModel
import com.xysss.mvvmhelper.ext.logE

/**
 * Author:bysd-2
 * Time:2021/9/2811:16
 */

class ThreeFragment : BaseFragment<SettingViewModel, FragmentThreeBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewBinding.customToolbar.setCenterTitle(R.string.bottom_title_report)
        mViewBinding.customToolbar.setBackgroundResource(R.color.colorPrimary_20)
        immersionBar {
            titleBar(mViewBinding.customToolbar)
        }

        //BleHelper.sendBlueToothMsg(reqDeviceMsg)

    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.deviceData.observe(this){
            //it.logE("xysLog")
            mViewBinding.settingTextViewValue1.text = it.deviceHardwareVersion
            mViewBinding.settingTextViewValue2.text = it.deviceSoftwareVersion
            mViewBinding.settingTextViewValue3.text = it.deviceBattery.toString()
            mViewBinding.settingTextViewValue4.text = it.deviceFreeMemory.toString()
            mViewBinding.settingTextViewValue5.text = it.deviceRecordSum.toString()
            mViewBinding.settingTextViewValue6.text = it.deviceAlarmSum.toString()
            mViewBinding.settingTextViewValue7.text = ByteUtils.cal(it.deviceCurrentRunningTime)
            mViewBinding.settingTextViewValue8.text = it.deviceCurrentAlarmNumber.toString()
            mViewBinding.settingTextViewValue9.text = ByteUtils.cal(it.deviceCumulativeRunningTime)
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