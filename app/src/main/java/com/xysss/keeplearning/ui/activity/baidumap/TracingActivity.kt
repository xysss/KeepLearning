package com.xysss.keeplearning.ui.activity.baidumap

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.*
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.model.LatLng
import com.baidu.trace.api.entity.OnEntityListener
import com.baidu.trace.api.track.LatestPointResponse
import com.baidu.trace.api.track.OnTrackListener
import com.baidu.trace.model.*
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.App
import com.xysss.keeplearning.app.util.*
import com.xysss.keeplearning.ui.activity.baidumap.model.CurrentLocation
import com.xysss.mvvmhelper.base.appContext

/**
 * 作者 : xys
 * 时间 : 2022-07-26 17:28
 * 描述 : 描述
 */
class TracingActivity :BaseActivity(), View.OnClickListener, SensorEventListener{



    private var viewUtil: ViewUtil? = null

    private var traceBtn: Button? = null

    private var gatherBtn: Button? = null

    private var powerManager: PowerManager? = null

    private var wakeLock: WakeLock? = null

    private var trackReceiver: TrackReceiver? = null

    private var mSensorManager: SensorManager? = null

    private var lastX = 0.0
    private var mCurrentDirection = 0F

    /**
     * 地图工具
     */
    private var mapUtil: MapUtil? = null

    /**
     * 轨迹服务监听器
     */
    private var traceListener: OnTraceListener? = null

    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private var trackListener: OnTrackListener? = null

    /**
     * Entity监听器(用于接收实时定位回调)
     */
    private var entityListener: OnEntityListener? = null

    /**
     * 实时定位任务
     */
    private val realTimeHandler: RealTimeHandler? = RealTimeHandler()

    private var realTimeLocRunnable: RealTimeLocRunnable? = null

    /**
     * 打包周期
     */
    var packInterval: Int = Constants.DEFAULT_PACK_INTERVAL

    /**
     * 轨迹点集合
     */
    private var trackPoints: MutableList<LatLng>? = null

    private var firstLocate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.tracing_title)
        setOptionsText()
        setOnClickListener(this)
        init()
    }

    fun setOptionsText() {
        val layout = findViewById<LinearLayout>(R.id.layout_top)
        val textView = layout.findViewById<View>(R.id.tv_options) as TextView
        textView.text = "轨迹追踪设置"
    }

    private fun init() {
        initListener()
        viewUtil = ViewUtil()
        mapUtil = MapUtil.getInstance()
        mapUtil!!.init(findViewById<MapView>(R.id.tracing_mapView))
        mapUtil!!.setCenter(mCurrentDirection) //设置地图中心点
        powerManager = appContext.getSystemService(POWER_SERVICE) as PowerManager?
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager // 获取传感器管理服务
        traceBtn = findViewById<Button>(R.id.btn_trace)
        gatherBtn = findViewById<Button>(R.id.btn_gather)
        traceBtn!!.setOnClickListener(this)
        gatherBtn!!.setOnClickListener(this)
        setTraceBtnStyle()
        setGatherBtnStyle()
        trackPoints = ArrayList()
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        //每次方向改变，重新给地图设置定位数据，用上一次的经纬度
        val x = sensorEvent.values[SensorManager.DATA_X].toDouble()
        if (Math.abs(x - lastX) > 1.0) { // 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
            mCurrentDirection = x.toFloat()
            if (!CommonUtil.isZeroPoint(CurrentLocation.latitude, CurrentLocation.longitude)) {
                mapUtil?.updateMapLocation(
                    LatLng(
                        CurrentLocation.latitude,
                        CurrentLocation.longitude
                    ), mCurrentDirection.toFloat()
                )
            }
        }
        lastX = x
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {}

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_activity_options -> ViewUtil.startActivityForResult(
                this,
                TracingOptionsActivity::class.java, Constants.REQUEST_CODE
            )
            R.id.btn_trace -> if (App.isTraceStarted) {
                App.mClient?.stopTrace(App.mTrace, traceListener) //停止服务
            } else {
                App.mClient?.startTrace(App.mTrace, traceListener) //开始服务
            }
            R.id.btn_gather -> if (App.isGatherStarted) {
                App.mClient?.stopGather(traceListener)
            } else {
                App.mClient?.setInterval(Constants.DEFAULT_GATHER_INTERVAL, packInterval)
                App.mClient?.startGather(traceListener) //开启采集
            }
            else -> {}
        }
    }

    /**
     * 设置服务按钮样式
     */
    private fun setTraceBtnStyle() {
        val isTraceStarted: Boolean = App.trackConf?.getBoolean("is_trace_started", false) == true
        if (isTraceStarted) {
            traceBtn?.setText(R.string.stop_trace)
            traceBtn?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.bg_btn_sure, null
                )
            } else {
                traceBtn!!.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.mipmap.bg_btn_sure, null
                    )
                )
            }
        } else {
            traceBtn?.setText(R.string.start_trace)
            traceBtn?.setTextColor(ResourcesCompat.getColor(resources, R.color.layout_title, null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.bg_btn_cancel, null
                )
            } else {
                traceBtn!!.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.mipmap.bg_btn_cancel, null
                    )
                )
            }
        }
    }

    /**
     * 设置采集按钮样式
     */
    private fun setGatherBtnStyle() {
        val isGatherStarted: Boolean = App.trackConf?.getBoolean("is_gather_started", false) == true
        if (isGatherStarted) {
            gatherBtn?.setText(R.string.stop_gather)
            gatherBtn?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.bg_btn_sure, null
                )
            } else {
                gatherBtn!!.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.mipmap.bg_btn_sure, null
                    )
                )
            }
        } else {
            gatherBtn?.setText(R.string.start_gather)
            gatherBtn?.setTextColor(ResourcesCompat.getColor(resources, R.color.layout_title, null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.bg_btn_cancel, null
                )
            } else {
                gatherBtn!!.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.mipmap.bg_btn_cancel, null
                    )
                )
            }
        }
    }

    /**
     * 实时定位任务
     */
    inner class RealTimeLocRunnable(interval: Int) : Runnable {
        private var interval = 0
        override fun run() {
            App.getCurrentLocation(entityListener, trackListener)
            realTimeHandler?.postDelayed(this, (interval * 1000).toLong())
        }

        init {
            this.interval = interval
        }
    }

    fun startRealTimeLoc(interval: Int) {
        realTimeLocRunnable = RealTimeLocRunnable(interval)
        realTimeHandler!!.post(realTimeLocRunnable!!)
    }

    fun stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable!!)
        }
        App.mClient?.stopRealTimeLoc()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null == data) {
            return
        }
        if (data.hasExtra("locationMode")) {
            val locationMode = LocationMode.valueOf(
                data.getStringExtra("locationMode")!!
            )
            App.mClient?.setLocationMode(locationMode) //定位模式
        }
        App.mTrace?.setNeedObjectStorage(false)
        if (data.hasExtra("gatherInterval") && data.hasExtra("packInterval")) {
            val gatherInterval =
                data.getIntExtra("gatherInterval", Constants.DEFAULT_GATHER_INTERVAL)
            val packInterval = data.getIntExtra("packInterval", Constants.DEFAULT_PACK_INTERVAL)
            this@TracingActivity.packInterval = packInterval
            App.mClient?.setInterval(gatherInterval, packInterval) //设置频率
        }
    }

    private fun initListener() {
        trackListener = object : OnTrackListener() {
            override fun onLatestPointCallback(response: LatestPointResponse) {
                //经过服务端纠偏后的最新的一个位置点，回调
                try {
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        return
                    }
                    val point = response.latestPoint
                    if (null == point || CommonUtil.isZeroPoint(
                            point.location.getLatitude(), point.location
                                .getLongitude()
                        )
                    ) {
                        return
                    }
                    val currentLatLng: LatLng = MapUtil.convertTrace2Map(point.location)
                        ?: return
                    if (firstLocate) {
                        firstLocate = false
                        Toast.makeText(this@TracingActivity, "起点获取中，请稍后...", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    //当前经纬度
                    CurrentLocation.locTime = point.locTime
                    CurrentLocation.latitude = currentLatLng.latitude
                    CurrentLocation.longitude = currentLatLng.longitude
                    if (trackPoints == null) {
                        return
                    }
                    trackPoints!!.add(currentLatLng)
                    mapUtil?.drawHistoryTrack(trackPoints, false, mCurrentDirection) //时时动态的画出运动轨迹
                } catch (x: Exception) {
                }
            }
        }
        entityListener = object : OnEntityListener() {
            override fun onReceiveLocation(location: TraceLocation) {
                //本地LBSTraceClient客户端获取的位置
                try {
                    if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(
                            location.latitude,
                            location.longitude
                        )
                    ) {
                        return
                    }
                    val currentLatLng: LatLng = mapUtil?.convertTraceLocation2Map(location)
                        ?: return
                    CurrentLocation.locTime = CommonUtil.toTimeStamp(location.time)
                    CurrentLocation.latitude = currentLatLng.latitude
                    CurrentLocation.longitude = currentLatLng.longitude
                    if (null != mapUtil) {
                        mapUtil?.updateMapLocation(currentLatLng, mCurrentDirection) //显示当前位置
                        mapUtil?.animateMapStatus(currentLatLng) //缩放
                    }
                } catch (x: Exception) {
                }
            }
        }
        traceListener = object : OnTraceListener {
            override fun onBindServiceCallback(errorNo: Int, message: String) {
                viewUtil?.showToast(
                    this@TracingActivity,
                    String.format(
                        "onBindServiceCallback, errorNo:%d, message:%s ",
                        errorNo,
                        message
                    )
                )
            }

            override fun onStartTraceCallback(errorNo: Int, message: String) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    App.isTraceStarted = true
                    val editor: SharedPreferences.Editor? = App.trackConf?.edit()
                    editor?.putBoolean("is_trace_started", true)
                    editor?.apply()
                    setTraceBtnStyle()
                    registerReceiver()
                }
                viewUtil?.showToast(
                    this@TracingActivity,
                    String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message)
                )
            }

            override fun onStopTraceCallback(errorNo: Int, message: String) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    App.isTraceStarted = false
                    App.isGatherStarted = false
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    val editor: SharedPreferences.Editor? = App.trackConf?.edit()
                    editor?.remove("is_trace_started")
                    editor?.remove("is_gather_started")
                    editor?.apply()
                    setTraceBtnStyle()
                    setGatherBtnStyle()
                    unregisterPowerReceiver()
                    firstLocate = true
                }
                viewUtil?.showToast(
                    this@TracingActivity,
                    String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message)
                )
            }

            override fun onStartGatherCallback(errorNo: Int, message: String) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    App.isGatherStarted = true
                    val editor: SharedPreferences.Editor? = App.trackConf?.edit()
                    editor?.putBoolean("is_gather_started", true)
                    editor?.apply()
                    setGatherBtnStyle()
                    stopRealTimeLoc()
                    startRealTimeLoc(packInterval)
                }
                viewUtil?.showToast(
                    this@TracingActivity,
                    String.format(
                        "onStartGatherCallback, errorNo:%d, message:%s ",
                        errorNo,
                        message
                    )
                )
            }

            override fun onStopGatherCallback(errorNo: Int, message: String) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    App.isGatherStarted = false
                    val editor: SharedPreferences.Editor? = App.trackConf?.edit()
                    editor?.remove("is_gather_started")
                    editor?.apply()
                    setGatherBtnStyle()
                    firstLocate = true
                    stopRealTimeLoc()
                    startRealTimeLoc(Constants.LOC_INTERVAL)
                    if (trackPoints!!.size >= 1) {
                        try {
                            mapUtil?.drawEndPoint(trackPoints!![trackPoints!!.size - 1])
                        } catch (e: Exception) {
                        }
                    }
                }
                viewUtil?.showToast(
                    this@TracingActivity,
                    String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message)
                )
            }

            override fun onPushCallback(messageType: Byte, pushMessage: PushMessage) {}
        }
    }

    class RealTimeHandler : Handler() {
    }

    /**
     * 注册广播（电源锁、GPS状态）
     */
    @SuppressLint("InvalidWakeLockTag")
    private fun registerReceiver() {
        if (App.isRegisterReceiver) {
            return
        }
        if (null == wakeLock) {
            wakeLock = powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload")
        }
        if (null == trackReceiver) {
            trackReceiver = wakeLock?.let { TrackReceiver(it) }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(StatusCodes.GPS_STATUS_ACTION)
        registerReceiver(trackReceiver, filter)
        App.isRegisterReceiver = true
    }

    private fun unregisterPowerReceiver() {
        if (!App.isRegisterReceiver) {
            return
        }
        if (null != trackReceiver) {
            unregisterReceiver(trackReceiver)
        }
        App.isRegisterReceiver = false
    }

    override fun onStart() {
        super.onStart()
        if (App.trackConf?.contains("is_trace_started") == true
            && App.trackConf?.contains("is_gather_started")!!
            && App.trackConf?.getBoolean("is_trace_started", false)!!
            && App.trackConf?.getBoolean("is_gather_started", false)!!
        ) {
            startRealTimeLoc(packInterval)
        } else {
            startRealTimeLoc(Constants.LOC_INTERVAL)
        }
    }

    override fun onResume() {
        super.onResume()
        mapUtil?.onResume()
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )

        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName: String = appContext.packageName
            val isIgnoring = powerManager!!.isIgnoringBatteryOptimizations(packageName)
            if (!isIgnoring) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                try {
                    startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapUtil?.onPause()
    }

    override fun onStop() {
        super.onStop()
        stopRealTimeLoc()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRealTimeLoc()
        trackPoints!!.clear()
        trackPoints = null
        mapUtil?.clear()
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_tracing
    }

}