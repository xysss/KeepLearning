package com.xysss.keeplearning.app.ext

import android.app.Dialog
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.xysss.keeplearning.R
import com.xysss.mvvmhelper.ext.hideOffKeyboard
import com.xysss.mvvmhelper.ext.textString

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

fun showPwd(view: EditText, boolean: Boolean) {
    if (boolean) {
        view.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        view.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    view.setSelection(view.textString().length)
}


fun EditText.afterTextChanged(action: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s.toString())
        }
    })
}

fun setOnClick(view: View, clickListener: () -> Unit) {
    val mHits = LongArray(2)
    view.setOnClickListener {
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] < SystemClock.uptimeMillis() - 500) {
            clickListener.invoke()
        }
    }
}