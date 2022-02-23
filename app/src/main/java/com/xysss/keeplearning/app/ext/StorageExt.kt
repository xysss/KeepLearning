package com.xysss.keeplearning.app.ext

import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.data.annotation.ValueKey
import kotlinx.coroutines.Job

/**
 * 作者　: xys
 * 时间　: 2021/09/27
 * 描述　:
 */

/**
 * 获取MMKV
 */

val dataRecordDao = AppDatabase.getDatabase().dataRecordDao()
val dataAlarmDao = AppDatabase.getDatabase().dataAlarmDao()
val dataMatterDao = AppDatabase.getDatabase().dataMatterDao()

val job= Job()

const val BluetoothConnected="BluetoothConnected"
const val MqttConnectSuccess="MqttConnectSuccess"
const val reqDeviceMsg="55000a09000001000023"  //读取设备信息
const val reqRealTimeDataMsg="55000a09100001000023"  //读取实时数据

val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    MMKV.mmkvWithID(ValueKey.MMKV_APP_KEY)
}