package com.xysss.keeplearning.app.ext

import android.app.Dialog
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.xysss.keeplearning.R
import com.xysss.mvvmhelper.ext.hideOffKeyboard

/**
 * Author:bysd-2
 * Time:2021/9/2810:56
 */

/*****************************************loading框********************************************/
private var loadingDialog: Dialog? = null

/**
 * 打开等待框
 */
fun AppCompatActivity.showLoadingExt(message: String = "请求网络中...") {
    if (!this.isFinishing) {
        if (loadingDialog == null) {
            //弹出loading时 把当前界面的输入法关闭
            this.hideOffKeyboard()
            loadingDialog = Dialog(this, R.style.loadingDialogTheme).apply {
                setCancelable(true)
                setCanceledOnTouchOutside(false)
                setContentView(
                    LayoutInflater.from(this@showLoadingExt)
                        .inflate(R.layout.layout_loading_view, null).apply {
                            this.findViewById<TextView>(R.id.loading_tips).text = message
                        })
            }
            loadingDialog?.setOnDismissListener {
                dismissLoadingExt()
            }
        }
        loadingDialog?.show()
    }
}

/**
 * 打开等待框
 */
fun Fragment.showLoadingExt(message: String = "请求网络中...") {
    activity?.let {
        if (!it.isFinishing) {
            if (loadingDialog == null) {
                //弹出loading时 把当前界面的输入法关闭
                it.hideOffKeyboard()
                loadingDialog = Dialog(requireActivity(), R.style.loadingDialogTheme).apply {
                    setCancelable(true)
                    setCanceledOnTouchOutside(false)
                    setContentView(
                        LayoutInflater.from(it)
                            .inflate(R.layout.layout_loading_view, null).apply {
                                this.findViewById<TextView>(R.id.loading_tips).text = message
                            })
                }
                loadingDialog?.setOnDismissListener {
                    dismissLoadingExt()
                }
            }
            loadingDialog?.show()
        }
    }
}

/**
 * 关闭等待框
 */
fun AppCompatActivity.dismissLoadingExt() {
    loadingDialog?.dismiss()
    loadingDialog = null
}

/**
 * 关闭等待框
 */
fun Fragment.dismissLoadingExt() {
    loadingDialog?.dismiss()
    loadingDialog = null
}