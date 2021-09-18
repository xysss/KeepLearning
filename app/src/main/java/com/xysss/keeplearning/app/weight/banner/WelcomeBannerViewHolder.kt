package com.xysss.keeplearning.app.weight.banner

import android.view.View
import android.widget.TextView
import com.xysss.keeplearning.R
import com.zhpan.bannerview.BaseViewHolder

/**
 * Author:bysd-2
 * Time:2021/9/1618:09
 */
class WelcomeBannerViewHolder(view: View) : BaseViewHolder<String>(view) {
    override fun bindData(data: String?, position: Int, pageSize: Int) {
        val textView = findView<TextView>(R.id.banner_text)
        textView.text = data
    }

}