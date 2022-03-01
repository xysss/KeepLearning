package com.xysss.keeplearning.app.ext

import android.os.Looper
import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.data.annotation.ValueKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.concurrent.LinkedBlockingDeque

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
val scope = CoroutineScope(job)

const val mqttConnectSuccess="MqttConnectSuccess"
const val reqDeviceMsg="55000a0900000100"  //读取设备信息
const val reqRealTimeDataMsg="55000a0910000100"  //读取实时数据

const val recTopicDefault = "HT308PRD/VP200/S2C/" //接收主题
const val sendTopicDefault = "HT308PRD/VP200/C2S/" //发送主题

var recTopic = "HT308PRD/VP200/S2C/20210708/" //接收主题
var sendTopic = "HT308PRD/VP200/C2S/20210708/" //发送主题

const val recordHeadMsg="5500120901000900"  //读取数据记录
const val alarmHeadMsg="5500120901000901"  //读取报警记录
const val matterHeadMsg="55000D09210004"  //请求数据条目

var defaultIndex=0
var defaultName="异丁烯"

var recordIndex=1L
var recordReadNum=5L
var recordSum= 0

var alarmIndex=1L
var alarmReadNum=10L
var alarmSum= 0

val startIndexByteArray0100=ByteArray(4)
val readNumByteArray0100=ByteArray(4)
val matterIndexMsg=ByteArray(4)

//val recLinkedDeque=LinkedBlockingDeque<ByteArray>(1000)
val recLinkedDeque=LinkedBlockingDeque<Byte>(100000)
val sendLinkedDeque=LinkedBlockingDeque<String>(1000)

var isStopReqRealMsg=false

var isConnectMqtt=false

val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    MMKV.mmkvWithID(ValueKey.MMKV_APP_KEY)
}

fun isMainThread(): Boolean {
    return Looper.getMainLooper().thread.id == Thread.currentThread().id
}