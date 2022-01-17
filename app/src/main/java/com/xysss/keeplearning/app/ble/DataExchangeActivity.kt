package com.xysss.keeplearning.app.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.xysss.keeplearning.app.base.BaseActivity
import com.xysss.keeplearning.app.util.BleHelper
import com.xysss.keeplearning.app.util.ByteUtils.getBCCResult
import com.xysss.keeplearning.databinding.ActivityDataExchangeBinding
import com.xysss.mvvmhelper.base.BaseViewModel

class DataExchangeActivity : BaseActivity<BaseViewModel, ActivityDataExchangeBinding>(),BleCallback.UiCallback{

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
        //gatt连接
        gatt = device!!.connectGatt(this, false, bleCallback)
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
        //Ble状态页面UI回调
        bleCallback.setUiCallback(this)
        device.getUuids().toString()
    }

    //页面返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home)  { onBackPressed();true } else false

    override fun state(state: String) {
        stringBuffer.append(state).append("\n")
        mViewBinding.tvState.text = stringBuffer.toString()
        mViewBinding.scroll.apply { viewTreeObserver.addOnGlobalLayoutListener { post { fullScroll(View.FOCUS_DOWN) } } }
    }
}