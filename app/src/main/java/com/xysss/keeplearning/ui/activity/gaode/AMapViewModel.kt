package com.xysss.keeplearning.ui.activity.gaode

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amap.api.maps.model.LatLng
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:16
 * 描述 : 描述
 */
class AMapViewModel : BaseViewModel() {

    val mRealTimeList: LiveData<MutableList<LatLng>> get() = _mRealTimeList
    private val _mRealTimeList = MutableLiveData<MutableList<LatLng>>()


    @SuppressLint("StaticFieldLeak")
    private lateinit var mapService: TrackCollectService
    @SuppressLint("StaticFieldLeak")
    private lateinit var mqttService: MQTTService

    fun putMapService(service: TrackCollectService) {
        mapService = service
    }

    fun putMqttService(service: MQTTService) {
        mqttService = service
    }

//    fun setViewModelRealLocationListener() {
//        mapService.setRealLocationListener(this)
//    }

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

//    override fun sendRealLocation(mlist: MutableList<LatLng>,mByteArray: ByteArray) {
//        _mRealTimeList.postValue(mlist)
//    }

    private fun toHexEncoding(color: Int): String {
        val sb = StringBuffer()
        var R: String = Integer.toHexString(Color.red(color))
        var G = Integer.toHexString(Color.green(color))
        var B = Integer.toHexString(Color.blue(color))
        if (R.length == 1) {
            R = "0$R"
        }
        if (G.length == 1) {
            G = "0$G"
        }
        if (B.length == 1) {
            B = "0$B"
        }
        sb.append("#")
        sb.append(R.uppercase(Locale.getDefault()))
        sb.append(G.uppercase(Locale.getDefault()))
        sb.append(B.uppercase(Locale.getDefault()))
        return sb.toString()
    }
}