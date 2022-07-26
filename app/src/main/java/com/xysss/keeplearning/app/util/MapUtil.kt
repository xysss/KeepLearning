package com.xysss.keeplearning.app.util

import android.graphics.Color
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.utils.CoordinateConverter
import com.baidu.trace.model.CoordType
import com.baidu.trace.model.TraceLocation
import com.xysss.keeplearning.app.ext.bmEnd
import com.xysss.keeplearning.app.ext.bmStart
import com.xysss.keeplearning.ui.activity.baidumap.model.CurrentLocation

/**
 * 作者 : xys
 * 时间 : 2022-07-26 16:47
 * 描述 : 描述
 */
class MapUtil {

    companion object  {
        val instanse = MapUtil()

        fun getInstance(): MapUtil {
            return instanse
        }

        /**
         * 将轨迹坐标对象转换为地图坐标对象
         */
        fun convertTrace2Map(traceLatLng: com.baidu.trace.model.LatLng): LatLng? {
            return LatLng(traceLatLng.latitude, traceLatLng.longitude)
        }

    }


    private var mapStatus: MapStatus? = null

    private var mMoveMarker: Marker? = null

    var mapView: MapView? = null

    var baiduMap: BaiduMap? = null

    var lastPoint: LatLng? = null

    private var locData: MyLocationData? = null

    /**
     * 路线覆盖物
     */
    var polylineOverlay: Overlay? = null

    private var mCurrentZoom = 18.0f



    fun init(view: MapView?) {
        mapView = view
        baiduMap = mapView!!.map
        mapView!!.showZoomControls(false)
        with(baiduMap) {
            this?.setMyLocationEnabled(true)
            this?.setMyLocationConfigeration(
                MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.FOLLOWING,
                    true,
                    null
                )
            )
            this?.setOnMapStatusChangeListener(object : OnMapStatusChangeListener {
                //缩放比例变化监听
                override fun onMapStatusChangeStart(mapStatus: MapStatus) {}
                override fun onMapStatusChange(mapStatus: MapStatus) {
                    mCurrentZoom = mapStatus.zoom
                }

                override fun onMapStatusChangeFinish(mapStatus: MapStatus) {}
            })
        }
    }

    fun onPause() {
        if (null != mapView) {
            mapView!!.onPause()
        }
    }

    fun onResume() {
        if (null != mapView) {
            mapView!!.onResume()
        }
    }

    fun clear() {
        lastPoint = null
        if (null != mMoveMarker) {
            mMoveMarker!!.remove()
            mMoveMarker = null
        }
        if (null != polylineOverlay) {
            polylineOverlay!!.remove()
            polylineOverlay = null
        }
        if (null != baiduMap) {
            baiduMap!!.clear()
            baiduMap = null
        }
        mapStatus = null
        if (null != mapView) {
            mapView!!.onDestroy()
            mapView = null
        }
    }

    /**
     * 将轨迹实时定位点转换为地图坐标
     */
    fun convertTraceLocation2Map(location: TraceLocation?): LatLng? {
        if (null == location) {
            return null
        }
        val latitude = location.latitude
        val longitude = location.longitude
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            return null
        }
        var currentLatLng = LatLng(latitude, longitude)
        if (CoordType.wgs84 == location.coordType) {
            val sourceLatLng = currentLatLng
            val converter = CoordinateConverter()
            converter.from(CoordinateConverter.CoordType.GPS)
            converter.coord(sourceLatLng)
            currentLatLng = converter.convert()
        }
        return currentLatLng
    }


    /**
     * 设置地图中心：使用已有定位信息；
     */
    fun setCenter(direction: Float) {
        if (!CommonUtil.isZeroPoint(CurrentLocation.latitude, CurrentLocation.longitude)) {
            val currentLatLng = LatLng(CurrentLocation.latitude, CurrentLocation.longitude)
            updateMapLocation(currentLatLng, direction)
            animateMapStatus(currentLatLng)
            return
        }
    }

    fun updateMapLocation(currentPoint: LatLng?, direction: Float) {
        if (currentPoint == null) {
            return
        }
        locData = MyLocationData.Builder().accuracy(0f).direction(direction)
            .latitude(currentPoint.latitude).longitude(currentPoint.longitude).build()
        baiduMap!!.setMyLocationData(locData)
    }

    /**
     * 绘制历史轨迹
     */
    fun drawHistoryTrack(points: List<LatLng?>?, staticLine: Boolean, direction: Float) {
        // 绘制新覆盖物前，清空之前的覆盖物
        baiduMap!!.clear()
        if (points == null || points.size == 0) {
            if (null != polylineOverlay) {
                polylineOverlay!!.remove()
                polylineOverlay = null
            }
            return
        }
        if (points.size == 1) {
            val startOptions: OverlayOptions = MarkerOptions().position(points[0]).icon(bmStart)
                .zIndex(9).draggable(true)
            baiduMap!!.addOverlay(startOptions)
            updateMapLocation(points[0], direction)
            animateMapStatus(points[0])
            return
        }
        val startPoint = points[0]
        val endPoint = points[points.size - 1]

        // 添加起点图标
        val startOptions: OverlayOptions = MarkerOptions()
            .position(startPoint).icon(bmStart)
            .zIndex(9).draggable(true)

        // 添加路线（轨迹）
        val polylineOptions: OverlayOptions = PolylineOptions().width(10)
            .color(Color.BLUE).points(points)
        if (staticLine) {
            // 添加终点图标
            drawEndPoint(endPoint)
        }
        baiduMap!!.addOverlay(startOptions)
        polylineOverlay = baiduMap!!.addOverlay(polylineOptions)
        if (staticLine) {
            animateMapStatus(points)
        } else {
            updateMapLocation(points[points.size - 1], direction)
            animateMapStatus(points[points.size - 1])
        }
    }

    fun drawEndPoint(endPoint: LatLng?) {
        // 添加终点图标
        val endOptions: OverlayOptions = MarkerOptions().position(endPoint)
            .icon(bmEnd).zIndex(9).draggable(true)
        baiduMap!!.addOverlay(endOptions)
    }

    fun animateMapStatus(points: List<LatLng?>?) {
        if (null == points || points.isEmpty()) {
            return
        }
        val builder = LatLngBounds.Builder()
        for (point in points) {
            builder.include(point)
        }
        val msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build())
        baiduMap!!.animateMapStatus(msUpdate)
    }

    fun animateMapStatus(point: LatLng?) {
        val builder = MapStatus.Builder()
        mapStatus = builder.target(point).zoom(mCurrentZoom).build()
        baiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
    }
}