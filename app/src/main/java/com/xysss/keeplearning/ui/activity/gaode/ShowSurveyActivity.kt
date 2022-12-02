package com.xysss.keeplearning.ui.activity.gaode

import android.annotation.SuppressLint
import android.os.Bundle
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.data.repository.Repository
import com.xysss.keeplearning.databinding.ActivityShowSurveyBinding
import com.xysss.mvvmhelper.ext.logE
import getRouteWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
 * 作者 : xys
 * 时间 : 2022-08-25 11:14
 * 描述 : 描述
 */

class ShowSurveyActivity : BaseActivity<ShowSurveyViewModel, ActivityShowSurveyBinding>() {

    private val sqlLatLngList = ArrayList<LatLng>()
    private var sqlConValueList = ArrayList<String>()
    private var ppmList = ArrayList<String>()
    private var nameList = ArrayList<String>()
    private var drawList= ArrayList<LatLng>()
    private var mBeginTime:Long = 0
    //巡航最大数值
    var surveyHistoryMaxConValue : Float = 0F
    //巡航历史数据
    var surveyHistoryConValue : Float = 0F

    override fun initView(savedInstanceState: Bundle?) {

        val bundle:Bundle?=intent.extras
        mBeginTime = bundle!!.getLong(intentFlag)

        mToolbar.initBack(getString(R.string.show_survey_activity_title)) {
            finish()
        }

        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mViewBinding.mMapView.onCreate(savedInstanceState)
        mViewBinding.mMapView.map.uiSettings.isZoomControlsEnabled = false

        scope.launch(Dispatchers.IO) {
            getTrack(mBeginTime)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTrack(beginTime: Long){
        if (beginTime!=0L) {
            val surveySlq = Repository.getSurveyByBeginTime(beginTime)
            val latlngs = surveySlq.longitudeLatitude.trim()
            val conValue = surveySlq.concentrationValue.trim()
            val ppm = surveySlq.ppm.trim()
            val name = surveySlq.name.trim()

            if (conValue.isNotEmpty()){
                sqlConValueList = conValue.split(delim).toList() as ArrayList<String>
            }
            if (ppm.isNotEmpty()){
                ppmList = ppm.split(delim).toList() as ArrayList<String>
            }
            if (ppm.isNotEmpty()){
                nameList = name.split(delim).toList() as ArrayList<String>
            }
            if (latlngs.isNotEmpty()) {
                val lonlats = latlngs.split(delim).toTypedArray()
                if (lonlats.isNotEmpty()) {
                    for (i in lonlats.indices) {
                        val lonlat = lonlats[i]
                        val split = lonlat.split(cutOff).toTypedArray()
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
        }

        surveyHistoryMaxConValue=0F
        for (i in 0 until sqlConValueList.size){
            if (sqlConValueList[i].isNotEmpty()) {
                if (sqlConValueList[i].toFloat()>surveyHistoryMaxConValue){
                    surveyHistoryMaxConValue=sqlConValueList[i].toFloat()
                }
            }
        }
        //surveyHistoryMaxConValue = Collections.max(sqlConValueList).toFloat()

        "巡测 MaxConValue: $surveyHistoryMaxConValue".logE(LogFlag)

        scope.launch(Dispatchers.Main) {
            val conUnit: String = when (ppmList[ppmList.size-2].toInt()) {
                0 -> "ppm"
                1 -> "ppb"
                2 -> "mg/m3"
                else -> ""
            }
            mViewBinding.surveyMaxValue.text = "${surveyHistoryMaxConValue.toInt()} $conUnit"
            mViewBinding.surveyAvgValue.text = "${(surveyHistoryMaxConValue/2).toInt()} $conUnit"
            mViewBinding.surveyMinValue.text = "0 $conUnit"
            mViewBinding.tvVocDes.text="${nameList[nameList.size-2]}浓度"

        }
        toDealData()
    }

    private fun toDealData(){
        for (i in 0 until sqlLatLngList.size){
            drawList.add(sqlLatLngList[i])
            if(sqlConValueList[i].isNotEmpty()){
                if (i==1){
                    if (surveyHistoryConValue<sqlConValueList[i].toFloat()){
                        surveyHistoryConValue = sqlConValueList[i].toFloat()
                    }
                }
                else{
                    surveyHistoryConValue = sqlConValueList[i].toFloat()
                }
            }
            if(drawList.size >1){
                when(i){
                    1->{
                        drawHistoryMapLine(drawList,1) //起点
                    }
                    sqlLatLngList.size-1->{
                        drawHistoryMapLine(drawList,2)  //终点
                    }
                    else->{
                        drawHistoryMapLine(drawList,0)  //中间点
                    }
                }

                val temp = LatLng(drawList[1].latitude,drawList[1].longitude)
                drawList.clear()
                drawList.add(temp)
            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun drawHistoryMapLine(list: MutableList<LatLng>?,flag:Int) {
        if (list == null || list.isEmpty()) {
            return
        }
        val mBuilder = LatLngBounds.Builder()
        val polylineOptions = PolylineOptions() //.setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
                .color(getHisToryDriveColor())
                .width(getRouteWidth())
                .addAll(list)
        //mMap.clear()
        mViewBinding.mMapView.map.addPolyline(polylineOptions)

        if (flag == 1) {
            val latLngBegin = LatLng(list[0].latitude, list[0].longitude)  //标记点
            val markerBegin: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngBegin).title("起点").snippet("DefaultMarker"))
            ("起点数据： ${latLngBegin.latitude} : ${latLngBegin.longitude}").logE(LogFlag)
        }
        else if (flag == 2) {
            val latLngEnd = LatLng(list[list.size - 1].latitude, list[list.size - 1].longitude)  //标记点
            val markerEnd: Marker = mViewBinding.mMapView.map.addMarker(MarkerOptions().position(latLngEnd).title("终点").snippet("DefaultMarker"))
            ("终点数据： ${latLngEnd.latitude} : ${latLngEnd.longitude}").logE(LogFlag)
        }

        //mMap.mapType=AMap.MAP_TYPE_NORMAL  //白昼地图（即普通地图）
        for (i in list.indices) {
            mBuilder.include(list[i])
        }
        val cameraUpdate: CameraUpdate
        // 判断,区域点计算出来,的两个点相同,这样地图视角发生改变,SDK5.0.0会出现异常白屏(定位到海上了)
        val northeast = mBuilder.build().northeast
        cameraUpdate = if (northeast != null && northeast == mBuilder.build().southwest) {
            CameraUpdateFactory.newLatLng(mBuilder.build().southwest)
        } else {
            CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 20)
        }
        mViewBinding.mMapView.map.animateCamera(cameraUpdate)
    }

    private fun getHisToryDriveColor(): Int {
        val colorNum: Int
        if (surveyHistoryConValue <= surveyHistoryMaxConValue) {
            val y = (surveyHistoryConValue * 255 / surveyHistoryMaxConValue).toInt()
            colorNum = colorHashMap[y] ?: 0
//            colorNum=Color.parseColor(toHexEncoding(colorHashMap[y] ?: 0))
            ("巡测历史数据： $surveyHistoryConValue   颜色y:$y ").logE(LogFlag)
        } else {
            colorNum = colorHashMap[255] ?: 0
            //colorNum= ContextCompat.getColor(appContext, R.color.red)
        }
        return colorNum
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mViewBinding.mMapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mViewBinding.mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        mViewBinding.mMapView.onResume()
        super.onResume()
        // 在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
    }

    override fun onPause() {
        // 在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mViewBinding.mMapView.onPause()
        super.onPause()
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}