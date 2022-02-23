package com.xysss.keeplearning.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.swallowsonny.convertextlibrary.toHexString
import com.swallowsonny.convertextlibrary.writeInt32LE
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Matter
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.Android10DownloadFactory
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.UriUtils
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp
import java.io.File

class BlueToothViewModel : BaseViewModel(), BleCallback.UiCallback, MQTTService.MqttMsgCall {
    val bleDate: LiveData<String?> get() = _bleDate

    private val publishTopic = "HT308PRD/VP200/C2S/20210708/" //发送主题
    private val _bleDate=MutableLiveData<String?>()
    @SuppressLint("StaticFieldLeak")
    private lateinit var mService: MQTTService

    private val recordHeadMsg="5500120901000900"  //读取数据记录
    private val alarmHeadMsg="5500120901000901"  //读取报警记录
    private val matterHeadMsg="55000D09210004"  //请求数据条目

    private val startIndexByteArray0100=ByteArray(4)
    private val readNumByteArray0100=ByteArray(4)
    private val matterIndexMsg=ByteArray(4)

    private var recordIndex=1L
    private var recordReadNum=10L
    private var recordSum= 0

    private var alarmIndex=1L
    private var alarmReadNum=10L
    private var alarmSum= 0

    //蓝牙回调
    val bleCallBack = BleCallback()

    fun setCallBack(){
        //注册回调
        bleCallBack.setUiCallback(this)
    }
    fun putService(service:MQTTService){
        mService=service
    }
    fun setMqttConnect(){
        mService.connectMqtt(appContext)
        mService.setMqttListener(this)
        // TODO: 2022/2/18  开始循环去发送实时命令
        BleHelper.sendBlueToothMsg(reqRealTimeDataMsg)
    }

    fun connectBlueTooth(device: BluetoothDevice?){
        BleHelper.connectBlueTooth(device,bleCallBack)
    }

    fun sendRecordMsg(){
        recordIndex=1L
        recordReadNum=10L
        val sendBytes=startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
        val command=recordHeadMsg+sendBytes.toHexString(false)+"0023".trim()
        BleHelper.sendBlueToothMsg(command)
        recordIndex += recordReadNum
    }
    fun sendAlarmMsg(){
        alarmIndex=1L
        alarmReadNum=10L
        val sendBytes=startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
        val command=alarmHeadMsg+sendBytes.toHexString(false)+"0023".trim()
        BleHelper.sendBlueToothMsg(command)
        alarmIndex += alarmReadNum
    }

    override fun reqMatter(index: Int) {
        val sendBytes=matterIndexMsg.writeInt32LE(index.toLong())
        val command=matterHeadMsg+sendBytes.toHexString(false)+"0023".trim()
        BleHelper.sendBlueToothMsg(command)
    }

    override fun saveMatter(matter: Matter) {
        if (Repository.forgetMatterIsExist(matter.voc_index_matter)==0)  //不存在
            Repository.insertMatter(matter)
    }

    override fun state(state: String?) {
        if (state==BluetoothConnected){
            BleHelper.sendBlueToothMsg(reqDeviceMsg)  //请求设备信息
        }
//        if (state.equals("DeviceInfoRsp")){
//            viewModelScope.launch {
//                while(true) {
//                    delay(1000)
//                    sendBlueToothMsg(reqRealDataMsg)
//                }
//            }
//        }
    }

    override fun mqttUIShow(state: String?) {
        if (state==MqttConnectSuccess){
            BleHelper.sendBlueToothMsg(reqRealTimeDataMsg)
        }
    }

    override fun realData(materialInfo: MaterialInfo) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val matterList= ArrayList<Matter>()
//            val matter1=Matter(1,"异丁烯","0")
//            val matter2=Matter(2,"甲丁烯","1")
//            val matter3=Matter(3,"乙丁烯","2")
//            matterList.add(matter1)
//            matterList.add(matter2)
//            matterList.add(matter3)
//            Repository.insertMatterList(matterList)
//        }
        _bleDate.postValue(materialInfo.toString())
    }

    override fun mqttSendMsg(bytes: ByteArray) {
        mService.publish(publishTopic, bytes)
    }

    override fun recordData(recordArrayList: ArrayList<Record>) {
        if (recordArrayList.size!=0){
            for (record in recordArrayList){
                if (Repository.forgetRecordIsExist(record.timestamp)==0){
                    Repository.insertRecord(record)
                }
            }
            //Repository.insertRecordList(recordArrayList)
            //recordArrayList.logE("xysLog")
            recordSum= mmkv.getInt(ValueKey.deviceRecordSum,0)
            if (recordIndex<recordSum-recordReadNum){
                "recordIndex: $recordIndex".logE("xysLog")
                val sendBytes=startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
                val command=recordHeadMsg+sendBytes.toHexString(false)+"0023".trim()
                BleHelper.sendBlueToothMsg(command)
                recordIndex += recordReadNum
            }
            else{
                if (recordIndex<recordSum){
                    recordReadNum=recordSum-recordIndex
                    val sendBytes=startIndexByteArray0100.writeInt32LE(recordIndex) + readNumByteArray0100.writeInt32LE(recordReadNum)
                    val command=recordHeadMsg+sendBytes.toHexString(false)+"0023".trim()
                    BleHelper.sendBlueToothMsg(command)
                }
                recordIndex = recordSum.toLong()
            }
        }
    }

    override fun alarmData(alarmArrayList: ArrayList<Alarm>) {
        if (alarmArrayList.size!=0){
            for (alarm in alarmArrayList){
                //不存在
                if (Repository.forgetAlarmIsExist(alarm.timestamp)==0){
                    Repository.insertAlarm(alarm)
                }
            }

            //Repository.insertAlarmList(alarmArrayList)
            //alarmArrayList.logE("xysLog")
            alarmSum= mmkv.getInt(ValueKey.deviceAlarmSum,0)
            if (alarmIndex<alarmSum-alarmReadNum){
                "alarmIndex: $alarmIndex:$alarmReadNum:$alarmSum".logE("xysLog")
                val sendBytes=startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                val command=alarmHeadMsg+sendBytes.toHexString(false)+"0023".trim()
                BleHelper.sendBlueToothMsg(command)
                alarmIndex += alarmReadNum
            }
            else{
                if (alarmIndex<alarmSum){
                    alarmReadNum=alarmSum-alarmIndex
                    val sendBytes=startIndexByteArray0100.writeInt32LE(alarmIndex) + readNumByteArray0100.writeInt32LE(alarmReadNum)
                    val command=alarmHeadMsg+sendBytes.toHexString(false)+"0023".trim()
                    BleHelper.sendBlueToothMsg(command)
                    "alarmIndex: $alarmIndex:$alarmReadNum:$alarmSum".logE("xysLog")
                }
                alarmIndex = alarmSum.toLong()
            }
        }
    }












    /**
     * 下载
     * @param downLoadData Function1<ProgressT<String>, Unit>
     * @param downLoadSuccess Function1<String, Unit>
     * @param downLoadError Function1<Throwable, Unit>
     */

    fun downLoad(downLoadData: (Progress) -> Unit = {}, downLoadSuccess: (String) -> Unit, downLoadError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            if (checkedAndroidQ()) {
                //android 10 以上
                val factory = Android10DownloadFactory(appContext, "${System.currentTimeMillis()}.apk")
                RxHttp.get(NetUrl.DOWNLOAD_URL)
                    .toFlow(factory) {
                        downLoadData.invoke(it)
                    }.catch {
                        //异常回调
                        downLoadError(it)
                    }.collect {
                        //成功回调
                        downLoadSuccess.invoke(UriUtils.getFileAbsolutePath(appContext,it)?:"")
                    }
            } else {
                //android 10以下
                val localPath = appContext.externalCacheDir!!.absolutePath + "/${System.currentTimeMillis()}.apk"
                RxHttp.get(NetUrl.DOWNLOAD_URL)
                    .toFlow(localPath) {
                        downLoadData.invoke(it)
                    }.catch {
                        //异常回调
                        downLoadError(it)
                    }.collect {
                        //成功回调
                        downLoadSuccess.invoke(it)
                    }
            }
        }
    }

    /**
     * android 10 及以上文件上传 ，兼容Android 10以下
     * 注意：这里并非通过 [Await] 实现的， 而是通过 [Flow] 监听的进度，因为在监听上传进度这块，Flow性能更优，且更简单
     * 如不需要监听进度，toFlow 方法不要传进度回调即可
     * @param uploadData Function1<Progress, Unit>
     * @param uploadSuccess Function1<String, Unit>
     * @param uploadError Function1<Throwable, Unit>
     */
    fun upload(filePath: String, uploadData: (Progress) -> Unit = {}, uploadSuccess: (String) -> Unit, uploadError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            if (checkedAndroidQ() && filePath.startsWith("content:")) {
                //android 10 以上
                RxHttp.postForm(NetUrl.UPLOAD_URL)
                    .addPart(appContext, "apkFile", Uri.parse(filePath))
                    .toFlow<String> {
                        //上传进度回调,0-100，仅在进度有更新时才会回调
                        uploadData.invoke(it)
                    }.catch {
                        //异常回调
                        uploadError.invoke(it)
                    }.collect {
                        //成功回调
                        uploadSuccess.invoke(it)
                    }
            } else {
                // android 10以下
                val file = File(filePath)
                if(!file.exists()){
                    uploadError.invoke(Exception("文件不存在"))
                    return@launch
                }
                RxHttp.postForm(NetUrl.UPLOAD_URL)
                    .addFile("apkFile", file)
                    .toFlow<String> {
                        //上传进度回调,0-100，仅在进度有更新时才会回调
                        uploadData.invoke(it)
                    }.catch {
                        //异常回调
                        uploadError.invoke(it)
                    }.collect {
                        //成功回调
                        uploadSuccess.invoke(it)
                    }
            }
        }
    }

    private fun checkedAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
