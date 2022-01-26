package com.xysss.keeplearning.ui.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.R
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.databinding.ActivityDataExchangeBinding
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.mvvmhelper.ext.logE
import com.xysss.mvvmhelper.ext.setOnclickNoRepeat


class DataExchangeActivity : BaseActivity<BlueToothViewModel, ActivityDataExchangeBinding>(),
    BleCallback.UiCallback{

    //Gatt
    private lateinit var gatt: BluetoothGatt

    //Ble回调
    private val bleCallback = BleCallback()

    //状态缓存
    private var stringBuffer = StringBuffer()

    private val send00Msg="55000a09000001000023"  //读取设备信息
    private val send10Msg="55000a09100001000023"  //读取实时数据
    private val send0100Msg="550012090100090001000000050000000023"  //读取数据记录
    private val send0101Msg="550012090100090101000000050000000023"  //读取报警记录

    override fun initView(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            title = "Data Exchange"
            setDisplayHomeAsUpEnabled(true)
        }
        val device = intent.getParcelableExtra<BluetoothDevice>("device")
        //gatt连接 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        //第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = device!!.connectGatt(this, false, bleCallback, TRANSPORT_LE)
        } else {
            gatt = device!!.connectGatt(this, false, bleCallback)
        }
        //Ble状态页面UI回调
        bleCallback.setUiCallback(this)
    }


    override fun onBindViewClick() {
        setOnclickNoRepeat(mViewBinding.button1, mViewBinding.button2, mViewBinding.button3,
            mViewBinding.button4, mViewBinding.button5,mViewBinding.button6, mViewBinding.button7,
            mViewBinding.button8,mViewBinding.button9, mViewBinding.button10,mViewBinding.btnSendCommand) {
            when (it.id) {
                R.id.btnSendCommand -> {
                    val command = mViewBinding.etCommand.text.toString().trim()
                    if (command.isEmpty()) {
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
                R.id.button1 -> {
                    mViewBinding.etCommand.setText(send00Msg)
                }
                R.id.button2 -> {
                    mViewBinding.etCommand.setText(send10Msg)
                }
                R.id.button3 -> {
                    mViewBinding.etCommand.setText(send0100Msg)
                }
                R.id.button4 -> {
                    mViewBinding.etCommand.setText(send0101Msg)
                }
                R.id.button5 -> {
                }
                R.id.button6 -> {
                }
                R.id.button7 -> {
                }
                R.id.button8 -> {
                }
                R.id.button9 -> {
                }
                R.id.button10 -> {
                }
            }
        }
    }

    //页面返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed();true
        } else false

    @SuppressLint("SetTextI18n")
    override fun state(state: String?)=runOnUiThread{
//        val id = Thread.currentThread().id
//        "state方法中的线程号：$id".logE("xysLog")
//        "state方回调运行在${if (isMainThread()) "主线程" else "子线程"}中".logE("xysLog")
        "解析完数据长度: ${state?.length}: $state".logE("xysLog")
        //mViewBinding.tvState.text = "收到转码后的数据长度: ${state?.length}: $state"
        stringBuffer.append("解析完数据长度: ${state?.length}: "+state).append("\n")
        mViewBinding.tvState.text = stringBuffer.toString()
        mViewBinding.scroll.apply { viewTreeObserver.addOnGlobalLayoutListener { post { fullScroll(View.FOCUS_DOWN) } } }
    }
}