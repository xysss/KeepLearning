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
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.app.room.Record
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.Android10DownloadFactory
import com.xysss.keeplearning.app.util.UriUtils
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.delay
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
    private val send00Msg="55000a09000001000023"  //读取设备信息
    private val send10Msg="55000a09100001000023"  //读取实时数据
    private val send0100Msg="550012090100090001000000050000000023"  //读取数据记录
    private val send0101Msg="550012090100090101000000050000000023"  //读取报警记录
    private val send21Msg="55000D09210004000000000023"  //读取物质信息

    private val dataRecordDao = AppDatabase.getDatabase().dataRecordDao()

    private val b0100Msg="5500120901000900"  //读取数据记录
    private val startIndexByteArray0100=ByteArray(4)
    private val readNumByteArray0100=ByteArray(4)
    private var startIndex=1L
    private var readNum=10L
    private var recordSumNum= 0


    //回调
    val bleCallBack = BleCallback()

    fun setCallBack(){
        //注册回调
        bleCallBack.setUiCallback(this)
    }

    fun connectBlueTooth(device: BluetoothDevice?){
        mService.connectMqtt(appContext)
        mService.connectBlueTooth(device,bleCallBack)
    }
    fun putService(service:MQTTService){
        mService=service
        mService.setMqttListener(this)
    }

    fun sendBlueToothMsg(msg:String){
        mService.sendBlueToothMsg(msg)
    }
    override fun state(state: String?) {
        if (state.equals("蓝牙连接完成")){
//            viewModelScope.launch {
//                while(true) {
//                    delay(1000)
//                    sendBlueToothMsg(send10Msg)
//                }
//            }
        }
        if (state.equals("HistoryOver")){
            recordSumNum= mmkv.getInt(ValueKey.recordSumNum,0)
            if (startIndex<recordSumNum-readNum){
                Thread.sleep(500)
                val sendBytes=startIndexByteArray0100.writeInt32LE(startIndex) + readNumByteArray0100.writeInt32LE(readNum)
                val command=b0100Msg+sendBytes.toHexString(false)+"0023".trim()
                sendBlueToothMsg(command)
                startIndex += readNum
            }
            else{
                if (startIndex<recordSumNum){
                    readNum=recordSumNum-startIndex
                    Thread.sleep(500)
                    val sendBytes=startIndexByteArray0100.writeInt32LE(startIndex) + readNumByteArray0100.writeInt32LE(readNum)
                    val command=b0100Msg+sendBytes.toHexString(false)+"0023".trim()
                    sendBlueToothMsg(command)
                }
                startIndex = recordSumNum.toLong()
            }
        }
        state.logE("xysLog")
    }

    override fun mqttUIShow(state: String?) {
        //_bleDate.postValue(state)
    }

    override fun realData(data: String?) {
        _bleDate.postValue(data)
    }

    override fun mqttSendMsg(bytes: ByteArray) {

        mService.publish(publishTopic, bytes)
    }

    override fun historyData(dateRecordArrayList: ArrayList<Record>) {
        for (Record in dateRecordArrayList)
            dataRecordDao.insertRecord(Record)
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
