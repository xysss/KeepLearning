package com.xysss.keeplearning.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
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
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.keeplearning.ui.activity.gaode.bean.LocationInfo
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class OneFragmentViewModel : BaseViewModel(), BleCallback.UiCallback {
    val bleDate: LiveData<MaterialInfo> get() = _bleDate
    val bleState: LiveData<String> get() = _bleState
    val progressNum: LiveData<Int> get() = _progressNum
    val numShow: LiveData<String> get() = _numShow
    val dialogStatus: LiveData<Boolean> get() = _dialogStatus

    var latLngResultList= MutableLiveData<ArrayList<LatLng>>()

    private var _bleDate=MutableLiveData<MaterialInfo>()
    private var _bleState=MutableLiveData<String>()
    private var _progressNum=MutableLiveData<Int>()
    private var _numShow=MutableLiveData<String>()
    private var _dialogStatus=MutableLiveData<Boolean>()

    @SuppressLint("StaticFieldLeak")
    private lateinit var mService: MQTTService

    //蓝牙回调
    private val bleCallBack = BleCallback()

    private var mLocationClient: AMapLocationClient? = null
    private var mAMapLocationListener: AMapLocationListener? = null
    private var mLocations = Vector<LocationInfo>()
    private var mDataBaseThread: ScheduledExecutorService? = null  // 入库线程
    var mVectorThread: ExecutorService? = null   // 入缓存线程
    private var isshowerror = true
    private var latLngList= ArrayList<LatLng>()
    private var testFlag:Double= 0.0
    //数据库相关
    private var mTimeStringBuffer = StringBuffer()
    private var mConcentrationValueStringBuffer = StringBuffer()
    private var mPpmStringBuffer = StringBuffer()
    private var mCfStringBuffer = StringBuffer()
    private var mLongitudeLatitudeStringBuffer = StringBuffer()
    private var mAlarmStatusBuffer = StringBuffer()
    private var mIndexBuffer = StringBuffer()
    private var mNameBuffer = StringBuffer()
    private var isStored=0
    private lateinit var newSurvey: Survey

    private lateinit var realLocationCallBack: RealLocationCallBack

    fun setRealLocationListener(mListener: RealLocationCallBack) {
        this.realLocationCallBack = mListener
    }


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

    // 开启定位服务
    fun startLocation() {
        "startLocation start...".logE(LogFlag)
        ToastUtils.showShort("开始采集")
        // 初始定位服务
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(appContext)
        }
        // 初始化定位参数
        val mLocationOption = AMapLocationClientOption()
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.interval = 2000
        // 设置定位参数
        mLocationClient?.setLocationOption(mLocationOption)
        mLocationOption.isOnceLocation = false // 是否定位一次
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
        mLocationClient?.startLocation()

        // 设置定位监听
        mLocationClient?.setLocationListener { amapLocation ->
            if (amapLocation != null && amapLocation.errorCode == 0) {
                // 定位成功回调信息，设置相关消息
                // amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见定位类型表
                // amapLocation.getLatitude();// 获取纬度
                // amapLocation.getLongitude();// 获取经度
                // amapLocation.getAccuracy();// 获取精度信息
                mAMapLocationListener?.onLocationChanged(amapLocation)  // 显示系统小蓝点
                if (mVectorThread == null) {
                    mVectorThread = Executors.newSingleThreadExecutor()
                }

                //测试模拟数据
                //testFlag += 0.00001

                // 避免阻塞UI主线程，开启一个单独线程来存入内存
                mVectorThread?.execute {
                    val trackTime=System.currentTimeMillis()/1000
                    val longitude = BigDecimal(amapLocation.longitude + testFlag).setScale(5, RoundingMode.FLOOR)
                    val latitude = BigDecimal(amapLocation.latitude + testFlag).setScale(5, RoundingMode.FLOOR)
                    "lat: +$latitude lon: $longitude".logE(LogFlag)
                    val unit = when(materialInfo.concentrationUnit){
                        "ppm" -> 0
                        "ppb" -> 1
                        "mg/m3" -> 2
                        else -> 0
                    }
                    mLocations.add(
                        LocationInfo(
                        trackTime,
                        materialInfo.concentrationNum.toFloat(),
                        materialInfo.concentrationState,
                        materialInfo.materialLibraryIndex,
                        unit,
                        materialInfo.cfNum.toFloat(),
                        materialInfo.materialName,
                        latitude.toDouble(),
                        longitude.toDouble())
                    )
                    latLngList.add(LatLng(amapLocation.latitude+testFlag,amapLocation.longitude+testFlag))
                    if(latLngList.size >1){
                        val sendRealSurveyBytes = getRealSurveyDataBytes(latitude.toDouble(),longitude.toDouble())
                        latLngResultList.postValue(latLngList)
                        mService.publish(sendRealSurveyBytes)
                        //realLocationCallBack.sendRealLocation(latLngList,bytes)
                        val temp = LatLng(latLngList[1].latitude,latLngList[1].longitude)
                        latLngList.clear()
                        latLngList.add(temp)
                    }
                }
            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                "location Error, ErrCode: ${amapLocation.errorCode}  errInfo:${amapLocation.errorInfo}".logE(
                    LogFlag
                )
                if (isshowerror) {
                    isshowerror = false
                    ToastUtils.showShort(amapLocation.errorInfo)
                }
            }
        }
    }

    // 开启数据入库线程，五秒中入一次库
    @SuppressLint("SimpleDateFormat")
    fun startCollect() {
        if (mDataBaseThread == null) {
            mDataBaseThread = Executors.newSingleThreadScheduledExecutor()
        }
        mDataBaseThread?.scheduleWithFixedDelay({ // 取出缓存数据
            saveData()
        }, (1000 * 20).toLong(), (1000 * 20).toLong(), TimeUnit.MILLISECONDS)
    }

    private fun saveData(){
        for (i in mLocations.indices) {
            val locationInfo: LocationInfo = mLocations[i]
            mTimeStringBuffer.append(locationInfo.time).append(delim)
            mConcentrationValueStringBuffer.append(locationInfo.concentrationValue).append(delim)
            mPpmStringBuffer.append(locationInfo.ppm).append(delim)
            mCfStringBuffer.append(locationInfo.cf).append(delim)
            mLongitudeLatitudeStringBuffer.append(locationInfo.lat).append(cutOff).append(locationInfo.lon).append(delim)
            mAlarmStatusBuffer.append(locationInfo.alarmStatus).append(delim)
            mIndexBuffer.append(locationInfo.index).append(delim)
            mNameBuffer.append(locationInfo.name).append(delim)
        }
        // 取完之后清空数据
        mLocations.clear()

        val track = Survey(
            trackBeginTime,
            trackEndTime,
            mTimeStringBuffer.toString().trim(),
            mConcentrationValueStringBuffer.toString().trim(),
            mAlarmStatusBuffer.toString().trim(),
            mIndexBuffer.toString().trim(),
            mPpmStringBuffer.toString().trim(),
            mCfStringBuffer.toString().trim(),
            mNameBuffer.toString().trim(),
            mLongitudeLatitudeStringBuffer.toString().trim())
        clearBuffer()
        addTrack(track)
    }

    private fun getRealSurveyDataBytes(mLatitude:Double,mLongitude: Double) : ByteArray{
        val realSurveyHeadByte = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            0x41.toByte(),
            0x09.toByte(),
            0x91.toByte(),
            0x00.toByte(),
            0x38.toByte()
        )
        val concentrationNumBytes = ByteArray(4)
        concentrationNumBytes.writeFloatLE(materialInfo.concentrationNum.toFloat())
        val concentrationStateBytes = ByteArray(4)
        concentrationStateBytes.writeInt32LE(materialInfo.concentrationState.toLong())
        val materialLibraryIndexBytes = ByteArray(4)
        materialLibraryIndexBytes.writeInt32LE(materialInfo.materialLibraryIndex.toLong())
        val state = when(materialInfo.concentrationUnit){
            "ppm" -> 0
            "ppb" -> 1
            "mg/m3" -> 2
            else -> 0
        }
        val materialPpmBytes = ByteArray(1)
        materialPpmBytes.writeInt8(state)
        val retainBytes = ByteArray(3)
        val materialCfBytes = ByteArray(4)
        materialCfBytes.writeFloatLE(materialInfo.cfNum.toFloat())
        val materialNameBytes = ByteArray(20)
        val mNameBytes = materialInfo.materialName.toByteArray()
        for (i in materialNameBytes.indices){
            if (i<mNameBytes.size){
                materialNameBytes[i]=mNameBytes[i]
            }else{
                materialNameBytes[i]=0
            }
        }
        val mLongitudeBytes = ByteArray(8)
        mLongitudeBytes.writeFloatLE(mLongitude.toFloat())
        val mLatitudeBytes = ByteArray(8)
        mLatitudeBytes.writeFloatLE(mLatitude.toFloat())
        val beforeCrcCheckBytes = realSurveyHeadByte + concentrationNumBytes + concentrationStateBytes + materialLibraryIndexBytes + materialPpmBytes +
                retainBytes + materialCfBytes + materialNameBytes + mLongitudeBytes + mLatitudeBytes
        val resultBytes = beforeCrcCheckBytes+ Crc8.cal_crc8_t(beforeCrcCheckBytes,beforeCrcCheckBytes.size) + ByteUtils.FRAME_END
        return BleHelper.transSendCoding(resultBytes)
    }



    private fun clearBuffer(){
        if (!TextUtils.isEmpty(mTimeStringBuffer.toString())) {
            mTimeStringBuffer.delete(0, mTimeStringBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mConcentrationValueStringBuffer.toString())) {
            mConcentrationValueStringBuffer.delete(0, mConcentrationValueStringBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mPpmStringBuffer.toString())) {
            mPpmStringBuffer.delete(0, mPpmStringBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mCfStringBuffer.toString())) {
            mCfStringBuffer.delete(0, mCfStringBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mLongitudeLatitudeStringBuffer.toString())) {
            mLongitudeLatitudeStringBuffer.delete(0, mLongitudeLatitudeStringBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mAlarmStatusBuffer.toString())) {
            mAlarmStatusBuffer.delete(0, mAlarmStatusBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mIndexBuffer.toString())) {
            mIndexBuffer.delete(0, mIndexBuffer.toString().length)
        }
        if (!TextUtils.isEmpty(mNameBuffer.toString())) {
            mNameBuffer.delete(0, mNameBuffer.toString().length)
        }
    }

    private fun addTrack(track: Survey) {
        if (TextUtils.isEmpty(track.longitudeLatitude)) {
            return
        }
        try {
            // 查找库里面有没有之前存储过当前的数据
            if (track.beginTime!=0L) {
                isStored= Repository.forgetSurveyIsExist(track.beginTime)
            }
            // 如果之前存储过
            if (isStored==1){
                val surveySlq = Repository.getSurveyByBeginTime(track.beginTime)
                //time
                if (!TextUtils.isEmpty(surveySlq.time)) {
                    mTimeStringBuffer.append( surveySlq.time)
                }
                if (!TextUtils.isEmpty(track.time)) {
                    mTimeStringBuffer.append(track.time)
                }
                //convalue
                if (!TextUtils.isEmpty(surveySlq.concentrationValue)) {
                    mConcentrationValueStringBuffer.append(surveySlq.concentrationValue)
                }
                if (!TextUtils.isEmpty(track.concentrationValue)) {
                    mConcentrationValueStringBuffer.append(track.concentrationValue)
                }
                //ppmValue
                if (!TextUtils.isEmpty(surveySlq.ppm)) {
                    mPpmStringBuffer.append(surveySlq.ppm)
                }
                if (!TextUtils.isEmpty(track.ppm)) {
                    mPpmStringBuffer.append(track.ppm)
                }
                //cfValue
                if (!TextUtils.isEmpty(surveySlq.cf)) {
                    mCfStringBuffer.append(surveySlq.cf)
                }
                if (!TextUtils.isEmpty(track.cf)) {
                    mCfStringBuffer.append(track.cf)
                }
                if (!TextUtils.isEmpty(surveySlq.longitudeLatitude)) {
                    mLongitudeLatitudeStringBuffer.append(surveySlq.longitudeLatitude)
                }
                if (!TextUtils.isEmpty(track.longitudeLatitude)) {
                    mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude)
                    "latlngs  data buffer new: ${mLongitudeLatitudeStringBuffer.length}".logE(LogFlag)
                }
                //alarmStatus
                if (!TextUtils.isEmpty(surveySlq.alarmStatus)) {
                    mAlarmStatusBuffer.append(surveySlq.alarmStatus)
                }
                if (!TextUtils.isEmpty(track.alarmStatus)) {
                    mAlarmStatusBuffer.append(track.alarmStatus)
                }
                //index
                if (!TextUtils.isEmpty(surveySlq.index)) {
                    mIndexBuffer.append(surveySlq.index)
                }
                if (!TextUtils.isEmpty(track.index)) {
                    mIndexBuffer.append(track.index)
                }
                //name
                if (!TextUtils.isEmpty(surveySlq.name)) {
                    mNameBuffer.append(surveySlq.name)
                }
                if (!TextUtils.isEmpty(track.name)) {
                    mNameBuffer.append(track.name)
                }

                newSurvey.apply {
                    beginTime = track.beginTime
                    endTime=track.endTime
                    newSurvey.time=mTimeStringBuffer.toString().trim()
                    newSurvey.concentrationValue=mConcentrationValueStringBuffer.toString().trim()
                    newSurvey.alarmStatus=mAlarmStatusBuffer.toString().trim()
                    newSurvey.index=mIndexBuffer.toString().trim()
                    newSurvey.ppm=mPpmStringBuffer.toString().trim()
                    newSurvey.cf=mCfStringBuffer.toString().trim()
                    newSurvey.name=mNameBuffer.toString().trim()
                    newSurvey.longitudeLatitude=mLongitudeLatitudeStringBuffer.toString().trim()
                }
                Repository.updateSurvey(newSurvey)
                "巡测：Repository updateSurvey".logE(LogFlag)
            }else{
                newSurvey = Survey(
                    track.beginTime,
                    track.endTime,
                    mTimeStringBuffer.append(track.time).toString().trim(),
                    mConcentrationValueStringBuffer.append(track.concentrationValue).toString().trim(),
                    mAlarmStatusBuffer.append(track.alarmStatus).toString().trim(),
                    mIndexBuffer.append(track.index).toString().trim(),
                    mPpmStringBuffer.append(track.ppm).toString().trim(),
                    mCfStringBuffer.append(track.cf).toString().trim(),
                    mNameBuffer.append(track.name).toString().trim(),
                    mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude).toString().trim()
                )
                newSurvey.id=Repository.insertSurvey(newSurvey)
                "巡测：Repository insertSurvey".logE(LogFlag)
            }
        } catch (e: Exception) {
            "addTrack error:$e".logE(LogFlag)
            e.printStackTrace()
        } finally {
            clearBuffer()
        }
    }

    fun stopLocation() {
        ToastUtils.showShort("停止采集")
        if (mLocationClient != null) {
            mLocationClient?.stopLocation()
            mLocationClient = null
        }
        // 关闭Vector线程
        if (mVectorThread != null) {
            mVectorThread?.shutdownNow()
            mVectorThread = null
        }
        // 关闭SaveDabase线程
        if (mDataBaseThread != null) {
            mDataBaseThread?.shutdownNow()
            mDataBaseThread = null
        }
        // 定期任务关闭后，需要把最后的数据同步到数据库
        scope.launch(Dispatchers.IO) {
            saveData()
            "巡测：Repository stop ".logE(LogFlag)
        }
    }

    fun getDriveColor(): Int {
        val ppmValue = mmkv.getInt(ValueKey.ppmValue, 0)
        val colorNum: Int
        var y: Int=0
        if (materialInfo.concentrationNum.toFloat() <= ppmValue) {
            y = (materialInfo.concentrationNum.toFloat() * 255 / ppmValue).toInt()
            colorNum = colorHashMap[y] ?: 0
//            colorNum=Color.parseColor(toHexEncoding(colorHashMap[y] ?: 0))
        } else {
            colorNum = colorHashMap[255] ?: 0
            y=255
            //colorNum= ContextCompat.getColor(appContext, R.color.red)
        }
        ("巡测实时数据： ${materialInfo.concentrationNum}   颜色y:$y ").logE(LogFlag)
        return colorNum
    }


    interface RealLocationCallBack {
        fun sendRealLocation(mList: MutableList<LatLng>,bytes: ByteArray)
    }

}
