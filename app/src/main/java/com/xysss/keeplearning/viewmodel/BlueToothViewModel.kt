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
import com.xysss.keeplearning.app.room.Matter
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
    val bleDate: LiveData<MaterialInfo> get() = _bleDate

    private val _bleDate=MutableLiveData<MaterialInfo>()
    @SuppressLint("StaticFieldLeak")
    private lateinit var mService: MQTTService

    //蓝牙回调
    private val bleCallBack = BleCallback()

    fun setCallBack(){
        //注册回调
        bleCallBack.setUiCallback(this)
    }

    fun putService(service:MQTTService){
        mService=service
    }

    fun setMqttConnect(){
        recTopic= mmkv.getString(ValueKey.recTopicValue, "").toString()
        sendTopic= mmkv.getString(ValueKey.sendTopicValue, "").toString()

        mService.connectMqtt(appContext)
        mService.setMqttListener(this)
        // TODO: 2022/2/18  开始循环去发送实时命令
        BleHelper.addSendLinkedDeque(reqRealTimeDataMsg)
    }

    fun connectBlueTooth(device: BluetoothDevice?){
        BleHelper.connectBlueTooth(device,bleCallBack)
    }

    override fun reqMatter(index: Int) {
        val sendBytes=matterIndexMsg.writeInt32LE(index.toLong())
        val command=matterHeadMsg+sendBytes.toHexString(false).trim()
        BleHelper.addSendLinkedDeque(command)
    }

    override fun saveMatter(matter: Matter) {
        if (Repository.forgetMatterIsExist(matter.voc_index_matter)==0)  //不存在
            Repository.insertMatter(matter)
    }

    override fun mqttUIShow(state: String?) {
        if (state==mqttConnectSuccess){
            mqttConnectSuccess.logE("xysLog")
            //BleHelper.sendBlueToothMsg(reqDeviceMsg)
        }
    }

    override fun realData(materialInfo: MaterialInfo) {
        _bleDate.postValue(materialInfo)
        materialInfo.toString().logE("xysLog")
    }

    override fun mqttSendMsg(bytes: ByteArray) {
        mService.publish(bytes)
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
