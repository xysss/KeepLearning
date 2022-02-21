package com.xysss.keeplearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.response.DeviceInfo
import com.xysss.mvvmhelper.base.BaseViewModel

/**
 * 作者 : xys
 * 时间 : 2022-02-21 16:27
 * 描述 : 描述
 */
class SettingViewModel : BaseViewModel(){
    val deviceData: LiveData<DeviceInfo> get() = _deviceData

    private val _deviceData= MutableLiveData<DeviceInfo>()

    fun getDeviceInfo(){
        _deviceData.value= DeviceInfo(
            mmkv.getString(ValueKey.deviceHardwareVersion,""),
            mmkv.getString(ValueKey.deviceSoftwareVersion,""),
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
            mmkv.getString(ValueKey.deviceId,"")
        )
    }


}