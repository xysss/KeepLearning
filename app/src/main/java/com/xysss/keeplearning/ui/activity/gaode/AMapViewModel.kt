package com.xysss.keeplearning.ui.activity.gaode

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amap.api.maps.model.*
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.app.ext.mmkv
import com.xysss.keeplearning.app.ext.scope
import com.xysss.keeplearning.app.service.MQTTService
import com.xysss.keeplearning.data.annotation.ValueKey
import com.xysss.keeplearning.ui.activity.gaode.database.TripDBHelper
import com.xysss.keeplearning.ui.activity.gaode.service.TrackCollectService
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:16
 * 描述 : 描述
 */
class AMapViewModel : BaseViewModel(), TrackCollectService.RealLocationCallBack {

    var colorHashMap = HashMap<Int,Int>()
    val track: LiveData<MutableList<LatLng>> get() = _track
    val mRealTimeList: LiveData<MutableList<LatLng>> get() = _mRealTimeList
    private val _track= MutableLiveData<MutableList<LatLng>>()
    private val _mRealTimeList= MutableLiveData<MutableList<LatLng>>()

    @SuppressLint("StaticFieldLeak")
    private lateinit var mService: TrackCollectService

    fun putService(service: TrackCollectService){
        mService=service
    }

    fun setRealLocationListener(){
        mService.setRealLocationListener(this)
    }

    fun getDriveColor(num:Int): Int {
        val locationRecNum = mmkv.getFloat(ValueKey.locationRecNum, 0F)
        val colorNum: Int
        if (locationRecNum<=num){
            val y=(locationRecNum*255/num).toInt()
            colorNum=colorHashMap[y] ?: 0
//            colorNum=Color.parseColor(toHexEncoding(colorHashMap[y] ?: 0))
            ("走航轨迹过程中收到的数据： $locationRecNum   颜色y:$y ").logE(LogFlag)
        }else{
            colorNum=colorHashMap[255] ?: 0
            //colorNum= ContextCompat.getColor(appContext, R.color.red)
        }
        return colorNum
    }

    fun getRouteWidth(): Float {
        return 20f
    }

    fun onShowClick() {
        scope.launch(Dispatchers.IO) {
            val trackId = SimpleDateFormat("yyyy-MM-dd").format(Date())
            _track.postValue(TripDBHelper.getInstance()?.getTrack(trackId))
        }
    }

    fun initColorMap(){
        if(colorHashMap.isEmpty()){
            for (i in 0..255){
                if (i<32){
                    colorHashMap[i] = Color.rgb(0, 0, 128+i*4)
                }
                if (i==32){
                    colorHashMap[i] = Color.rgb(0, 0, 255)
                }
                if (i in 33..95){
                    colorHashMap[i] = Color.rgb(0, (i-32)*4, 255)
                }
                if (i in 96..159){
                    colorHashMap[i] = Color.rgb(2+(4*(i-96)), 255, 254-4*(i-96))
                }
                if (i==160){
                    colorHashMap[i] = Color.rgb(255, 252, 0)
                }
                if (i in 161..223){
                    colorHashMap[i] = Color.rgb(255, 248-4*(i-161), 0)
                }
                if (i==224){
                    colorHashMap[i] = Color.rgb(252, 0, 0)
                }
                if (i in 225..255){
                    colorHashMap[i] = Color.rgb(248-4*(i-225), 0, 0)
                }
            }
        }
    }

    override fun sendRealLocation(mlist: MutableList<LatLng>) {
        _mRealTimeList.postValue(mlist)
    }

    private fun toHexEncoding(color :Int): String {
        val sb = StringBuffer()
        var R :String= Integer.toHexString(Color.red(color))
        var G = Integer.toHexString(Color.green(color))
        var B = Integer.toHexString(Color.blue(color))
        if (R.length == 1){
            R= "0$R"
        }
        if (G.length == 1){
            G= "0$G"
        }
        if (B.length == 1){
            B= "0$B"
        }
        sb.append("#")
        sb.append(R.uppercase(Locale.getDefault()))
        sb.append(G.uppercase(Locale.getDefault()))
        sb.append(B.uppercase(Locale.getDefault()))
        return sb.toString()
    }
}