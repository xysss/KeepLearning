package com.xysss.keeplearning.app.service

import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.ui.activity.MainActivity
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlin.concurrent.thread


class MQTTService : Service(){

    private val userName = "LTAIN7ty7dgzvLtS" //用户名
    private val password = "PvErZ94s5FMlnc67jqAIK29QJsg=" //密码
    private val receiveTopic = "HT308PRD/VP200/S2C/20210708/" //接收主题
    //private val sendTopic = "HT308PRD/VP200/C2S/20210708/" //发送主题
    private val qos = 0
    //private val serverURI = "tcp://iot.eclipse.org:1883"
    private val serverURI = "tcp://post-cn-mp90edmzr0e.mqtt.aliyuncs.com:1883"
    private val clientId = "GID_308PRD@@@20210708_4G"
    private lateinit var mqttClient: MqttAndroidClient
    private val mBinder = MyBinder()

    //Gatt
    private lateinit var gatt: BluetoothGatt

    //状态缓存
    private var stringBuffer = StringBuffer()

    private val send00Msg="55000a09000001000023"  //读取设备信息
    private val send10Msg="55000a09100001000023"  //读取实时数据
    private val send0100Msg="550012090100090001000000050000000023"  //读取数据记录
    private val send0101Msg="550012090100090101000000050000000023"  //读取报警记录
    private val send21Msg="55000D09210400000000000023"  //读取物质信息

    companion object {
        const val TAG = "AndroidMqttClient"
        /**
         * 开启服务
         */
//        fun startService() {
//            appContext.startService(Intent(appContext, MQTTService::class.java))
//        }
    }

    inner class MyBinder : Binder() {
        val service: MQTTService
            get() = this@MQTTService
    }

    //    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        connect(appContext)
//        return super.onStartCommand(intent, flags, startId)
//    }
    override fun onCreate() {
        super.onCreate()
        val manager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel("my_service","前台Service通知",NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val intent=Intent(this,MainActivity::class.java)
        val pi=PendingIntent.getActivity(this,0,intent,0)
        val notification=NotificationCompat.Builder(this,"my_service")
            .setContentTitle("This is content title")
            .setContentText("This is content text")
            .setSmallIcon(R.drawable.ic_demo)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.ic_launcher))
            .setContentIntent(pi)
            .build()
        startForeground(1,notification)
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun connectBlueTooth(device:BluetoothDevice?,bleCallback:BleCallback){
        //gatt连接 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        //第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //gatt = device?.connectGatt(this, false, bleCallback, BluetoothDevice.TRANSPORT_LE) ?: null
            gatt = device?.connectGatt(this, false, bleCallback, BluetoothDevice.TRANSPORT_LE) as BluetoothGatt
        } else {
            //gatt = device.connectGatt(this, false, bleCallback)
            gatt = device?.connectGatt(this, false, bleCallback) as BluetoothGatt
        }
    }
    fun sendBlueToothMsg(command: String){
        if (command.trim().isEmpty()) {
            ToastUtils.showShort("请输入指令")
        }
        //command += getBCCResult(command)
        //发送指令
        //BleHelper.sendCommand(gatt, command, true)

        //55000a09 01 0001 00 0023  //读取数据记录
//            val b0100Msg="5500120901000900"  //读取数据记录
//            val startIndexByteArray0100=ByteArray(4)
//            val readNumByteArray0100=ByteArray(4)
//            val startIndex=1L
//            val readNum=5L
//            val sendByteArry= reversSendCode(startIndexByteArray0100.writeInt32LE(startIndex) + readNumByteArray0100.writeInt32LE(readNum))
//            val command=b0100Msg+sendByteArry?.toHexString(false)+"0023".trim()
//            "sendMsg:$command".logE("xysLog")
        //command="123456789012345678901234567890"

        val stringBuffer = StringBuffer()
        if (command.length>40){
            for (i in command.indices){
                stringBuffer.append(command[i])
                if (stringBuffer.length==40){
                    BleHelper.sendCommand(gatt, stringBuffer.toString(), true)
                    "stringBuffer:$stringBuffer".logE("xysLog")
                    stringBuffer.delete( 0, stringBuffer.length)
                    Thread.sleep(200)
                }
            }
            BleHelper.sendCommand(gatt, stringBuffer.toString(), true)
            "stringBuffer:$stringBuffer".logE("xysLog")
        }else{
            BleHelper.sendCommand(gatt, command, true)
            command.logE("xysLog")
        }
    }

    //连接
    fun connectMqtt(context: Context) {
        thread {
            val mqttPersist = MemoryPersistence()
            mqttClient = MqttAndroidClient(context, serverURI, clientId, mqttPersist)
            //订阅主题的回调
            mqttClient.setCallback(object : MqttCallback {
                //messageArrived：收到 broker 新消息
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    "Receive message: ${message.toString()} from topic: $topic".logE(TAG)

                    response("message arrived")
                }

                //connectionLost：与 broker 连接丢失
                override fun connectionLost(cause: Throwable?) {
                    "Connection lost ${cause.toString()}".logE(TAG)
                }

                //deliveryComplete：消息到 broker 传递完成
                override fun deliveryComplete(token: IMqttDeliveryToken?) {

                }
            })
            //MqttConnectOptions 用于配置连接设置，包含用户名密码，超时配置等，具体可以查看其方法。
            val options = MqttConnectOptions()
            options.isCleanSession = true //设置是否清除缓存
            options.connectionTimeout = 10 //设置超时时间，单位：秒
            options.keepAliveInterval = 60 //设置心跳包发送间隔，单位：秒
            options.userName = userName //设置用户名
            options.password = password.toCharArray() //设置密码
            try {
                mqttClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionTokexn: IMqttToken?) {
                        "MQTT Connection success".logE(TAG)
                        //去订阅主题
                        subscribe(receiveTopic, qos)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        "MQTT Connection failure".logE(TAG)
                        // 连接失败，重连
                        //connect(appContext)
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    //订阅 topic
    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    "Subscribed to $topic".logE(TAG)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    "Failed to subscribe $topic".logE(TAG)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //取消订阅 topic
    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    "Unsubscribed to $topic".logE(TAG)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    "Failed to unsubscribe $topic".logE(TAG)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //发布消息
    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    "$msg published to $topic".logE(TAG)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    "Failed to publish $msg to $topic".logE(TAG)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //断开MQTT连接
    private fun mpttDisconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    "Disconnected".logE(TAG)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    "Failed to disconnect".logE(TAG)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    /**
     * 判断网络是否连接
     */
    val isConnectIsNomarl: Boolean
    get() {
            val connectivityManager =
                this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                "当前网络名称：$name".logE("xysLog")
                true
            } else {
                "没有可用网络".logE("xysLog")
                /*没有可用网络的时候，延迟3秒再尝试重连*/
                Handler().postDelayed(
                    Runnable { connectMqtt(appContext) },
                    3000
                )
                false
            }
        }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    fun response(message: String) {
        val topic = receiveTopic
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttClient.publish(
                topic, message.toByteArray(),
                qos, retained
            )
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    override fun unbindService(conn: ServiceConnection) {
        mpttDisconnect()
        gatt.disconnect()
        gatt.close()
        super.unbindService(conn)
    }
}
