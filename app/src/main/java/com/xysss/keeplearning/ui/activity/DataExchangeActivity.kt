package com.xysss.keeplearning.ui.activity

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.bluetooth.BleCallback
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils.getBCCResult
import com.xysss.keeplearning.databinding.ActivityDataExchangeBinding
import com.xysss.keeplearning.viewmodel.BlueToothViewModel
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.ext.logD
import com.xysss.mvvmhelper.ext.logE

class DataExchangeActivity : BaseActivity<BlueToothViewModel, ActivityDataExchangeBinding>(),
    BleCallback.UiCallback {

    //Gatt
    private lateinit var gatt: BluetoothGatt
    //Ble回调
    private val bleCallback = BleCallback()
    //状态缓存
    private var stringBuffer = StringBuffer()

    override fun initView(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            title = "Data Exchange"
            setDisplayHomeAsUpEnabled(true)
        }
        val device = intent.getParcelableExtra<BluetoothDevice>("device")
        //gatt连接 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        //第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = device!!.connectGatt(this, false, bleCallback,TRANSPORT_LE)
        }
        else{
            gatt = device!!.connectGatt(this, false, bleCallback)
        }
        //发送指令
        mViewBinding.btnSendCommand.setOnClickListener {
            var command = mViewBinding.etCommand.text.toString().trim()
            if (command.isEmpty()) {
                ToastUtils.showShort("请输入指令")
                return@setOnClickListener
            }
            command += getBCCResult(command)
            //发送指令
            BleHelper.sendCommand(gatt, command,true)
        }
    }

    //页面返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home)  { onBackPressed();true } else false

    override fun state(state: String) {
        TODO("Not yet implemented")
    }

}