package com.xysss.keeplearning.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.blankj.utilcode.util.ToastUtils
import com.swallowsonny.convertextlibrary.toHexString
import com.swallowsonny.convertextlibrary.writeFloatLE
import com.swallowsonny.convertextlibrary.writeInt32LE
import com.swallowsonny.convertextlibrary.writeInt8
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Survey
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.app.util.*
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.internal.toHexString
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp
import java.io.File
import java.util.ArrayList

class BlueToothViewModel : BaseViewModel(), BleCallback.UiCallback {
    val bleDate: LiveData<MaterialInfo> get() = _bleDate
    val bleState: LiveData<String> get() = _bleState
    val progressNum: LiveData<Int> get() = _progressNum
    val numShow: LiveData<String> get() = _numShow
    val dialogStatus: LiveData<Boolean> get() = _dialogStatus

    private var _bleDate=MutableLiveData<MaterialInfo>()
    private var _bleState=MutableLiveData<String>()
    private var _progressNum=MutableLiveData<Int>()
    private var _numShow=MutableLiveData<String>()
    private var _dialogStatus=MutableLiveData<Boolean>()

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

    fun connectBlueTooth(device: BluetoothDevice?){
        BleHelper.connectBlueTooth(device,bleCallBack)
    }

    override fun realData(materialInfo: MaterialInfo) {
        _bleDate.postValue(materialInfo)
        materialInfo.toString().logE("LogFlag")
    }

    override fun bleConnected(state:String) {
        _bleState.postValue(state)
    }

    override fun synProgress(progress: Int,numShow :String) {
        _progressNum.postValue(progress)
        _numShow.postValue(numShow)
    }

    override fun mqttSendMsg(bytes: ByteArray) {
        mService.publish(bytes)
    }

    fun sendSurveys(){
        scope.launch(Dispatchers.IO) {
            val surveyList = Repository.loadAllSurvey()
            if (surveyList.isNotEmpty()) {
                for (i in surveyList.indices){
                    "mqtt history: item :$i, 一共： ${surveyList.size}".logE(LogFlag)
                    subpackage(getHistorySurveyByte(surveyList[i]))

                    _progressNum.postValue(i* 100/(surveyList.size-1))
                    _numShow.postValue("${i+1}/${surveyList.size}")
                }
            } else {
                //_dialogStatus.postValue(false)
                //dismissProgressUI()
                ToastUtils.showShort("设备上未查询到数据")
            }
        }
    }

    private suspend fun subpackage(byteArray: ByteArray){
        if (byteArray.size>1024){
            val mList=ByteArray(1024)
            var j=0
            for (i in byteArray.indices){
                mList[j]=byteArray[i]
                j++
                if (i!=0 && i%1023==0){
                    mService.publish(mList)
                    "mqtt history 总长度: ${byteArray.size} 发送长度： $i : ${byteArray.toHexString()}".logE(LogFlag)
                    j=0
                    delay(200)
                }
            }
            if (mList.isNotEmpty()){
                val mLastList=ByteArray(j)
                System.arraycopy(mList,0,mLastList,0,mLastList.size)
                mService.publish(mLastList)
                "mqtt history last 总长度: ${byteArray.size} 发送长度： ${mLastList.size} : ${mLastList.toHexString()}".logE(LogFlag)
            }
            //_dialogStatus.postValue(false)
            //dismissProgressUI()
        }else{
            mService.publish(byteArray)
            "mqtt history last 总长度: ${byteArray.size} 发送长度： ${byteArray.size} : ${byteArray.toHexString()}".logE(LogFlag)
            //_dialogStatus.postValue(false)
            //dismissProgressUI()
        }
    }

    private fun getHistorySurveyByte(survey: Survey) : ByteArray{
        val sqlLatLngList = ArrayList<LatLng>()
        val sqlConValueList = if (survey.concentrationValue.trim().isNotEmpty()) survey.concentrationValue.split(delim).toList() as ArrayList<String> else ArrayList<String>()
        val sqlTimeList = if (survey.time.trim().isNotEmpty()) survey.time.split(delim).toList() as ArrayList<String> else ArrayList<String>()
        val sqlPpmList = if (survey.ppm.trim().isNotEmpty()) survey.ppm.split(delim).toList() as ArrayList<String> else ArrayList<String>()
        val sqlIndexList = if (survey.index.trim().isNotEmpty()) survey.index.split(delim).toList() as ArrayList<String> else ArrayList<String>()

        if (survey.longitudeLatitude.trim().isNotEmpty()) {
            val lonLats = survey.longitudeLatitude.trim().split(delim).toTypedArray()
            if (lonLats.isNotEmpty()) {
                for (i in lonLats.indices) {
                    val mLonLat = lonLats[i]
                    val split = mLonLat.split(cutOff).toTypedArray()
                    if (split.isNotEmpty()) {
                        try {
                            sqlLatLngList.add(LatLng(split[0].toDouble(), split[1].toDouble()))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        val bytDatsSize = (sqlLatLngList.size * 29)+ 12
        val bytSize = (sqlLatLngList.size * 29) + 21
        "mqtt history size: ${sqlLatLngList.size} ,$bytDatsSize,$bytSize".logE(LogFlag)
        val mHeadByte : ByteArray = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            bytSize.toByte(),
            0x09.toByte(),
            0x94.toByte(),
            0x00.toByte(),
            bytDatsSize.toByte()
        )
        val beginTimeBytes = ByteArray(4)
        beginTimeBytes.writeInt32LE(survey.beginTime)
        val endTimeBytes = ByteArray(4)
        endTimeBytes.writeInt32LE(survey.endTime)
        val listSizeBytes = ByteArray(4)
        listSizeBytes.writeInt32LE(sqlLatLngList.size.toLong())
        "mqtt history time: ${survey.endTime},16进制：${(survey.endTime).toHexString()}".logE(LogFlag)
        val itemByteArray = ByteArray(sqlLatLngList.size * 29 )
        var index:Int = 0
        for (i in sqlLatLngList.indices){
            val timeStamp = ByteArray(4)
            timeStamp.writeInt32LE(if(sqlTimeList[i].isNotEmpty()) sqlTimeList[i].toLong() else 0)
            val conBytes = ByteArray(4)
            conBytes.writeFloatLE(if(sqlConValueList[i].isNotEmpty()) sqlConValueList[i].toFloat() else 0F)

//            val stateBytes = ByteArray(4)
//            stateBytes.writeInt32LE(1234)

            val indexBytes = ByteArray(4)
            indexBytes.writeInt32LE(if(sqlIndexList[i].isNotEmpty()) sqlIndexList[i].toLong() else 0)
            val ppmBytes = ByteArray(1)
            ppmBytes.writeInt8(if(sqlPpmList[i].isNotEmpty()) sqlPpmList[i].toInt() else 0)

//            val nameBytes = ByteArray(20)
//            val nBytes ="异丁烯".toByteArray()
//            for (k in nameBytes.indices){
//                if (k<nBytes.size){
//                    nameBytes[k]=nBytes[k]
//                }else{
//                    nameBytes[k]=0
//                }
//            }
            val mLongitudeBytes = ByteArray(8)
            mLongitudeBytes.writeFloatLE(sqlLatLngList[i].longitude.toFloat())
            val mLatitudeBytes = ByteArray(8)
            mLatitudeBytes.writeFloatLE(sqlLatLngList[i].latitude.toFloat())
            val itemSurvey = timeStamp + conBytes + indexBytes + ppmBytes + mLongitudeBytes +  mLatitudeBytes
            System.arraycopy(itemSurvey,0,itemByteArray,index,itemSurvey.size)
            index +=itemSurvey.size
        }
        val beforeCrcCheckBytes = mHeadByte + beginTimeBytes + endTimeBytes + listSizeBytes + itemByteArray
        val resultBytes = beforeCrcCheckBytes+ Crc8.cal_crc8_t(beforeCrcCheckBytes,beforeCrcCheckBytes.size) + ByteUtils.FRAME_END
        return BleHelper.transSendCoding(resultBytes)
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
