package com.xysss.keeplearning.app.ext

import android.os.Looper
import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.response.MaterialInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.HashMap
import java.util.concurrent.LinkedBlockingQueue

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
val dataSurveyDao = AppDatabase.getDatabase().dataSurveyDao()

val job = Job()
val scope = CoroutineScope(job)
var isRecOK = true

var LogFlag = "xysss"
const val recordFileName = "recordFileName"
const val alarmFileName = "alarmFileName"

const val reqDeviceMsg = "55000a0900000100"  //读取设备信息
const val reqRealTimeDataMsg = "55000a0910000100"  //读取实时数据

const val recTopicDefault = "HT308PRD/VP200/S2C/" //接收主题
const val sendTopicDefault = "HT308PRD/VP200/C2S/" //发送主题

var recTopic = "HT308PRD/VP200/S2C/20210708/" //接收主题
var sendTopic = "HT308PRD/VP200/C2S/20210708/" //发送主题

const val recordHeadMsg = "5500120901000900"  //读取数据记录
const val alarmHeadMsg = "5500120901000901"  //读取报警记录
const val matterHeadMsg = "55000D09210004"  //请求数据条目

const val sUUID = "0003cdd0-0000-1000-8000-00805f9b0131"
const val wUUID = "0003cdd2-0000-1000-8000-00805f9b0131"
const val rUUID = "0003cdd1-0000-1000-8000-00805f9b0131"
const val dUUID = "00002902-0000-1000-8000-00805f9b34fb"

var recordIndex = 1L
var recordReadNum = 5L
var recordSum = 0

var alarmIndex = 1L
var alarmReadNum = 5L
var alarmSum = 0

val startIndexByteArray0100 = ByteArray(4)
val readNumByteArray0100 = ByteArray(4)
val matterIndexMsg = ByteArray(4)

//val recLinkedDeque=LinkedBlockingDeque<ByteArray>(1000000)
val recLinkedDeque = LinkedBlockingQueue<Byte>()  //默认情况下，该阻塞队列的大小为Integer.MAX_VALUE，由于这个数值特别大
val sendLinkedDeque = LinkedBlockingQueue<String>(1000)

var isBleReady=false

var isRealTimeModel = false

var isConnectMqtt = false

var isPollingModel = false  //是否巡航模式

val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    MMKV.mmkvWithID(ValueKey.MMKV_APP_KEY)
}

fun isMainThread(): Boolean {
    return Looper.getMainLooper().thread.id == Thread.currentThread().id
}

const val delim = "￥"
const val cutOff = ","

//巡测实时数据
var trackBeginTime = 0L
var trackEndTime = 0L
var trackTime = 0L

//设备实时数据
lateinit var materialInfo : MaterialInfo

const val intentFlag ="beginTime"

var colorHashMap = HashMap<Int, Int>()


