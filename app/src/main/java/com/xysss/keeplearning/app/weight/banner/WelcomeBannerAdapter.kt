package com.xysss.keeplearning.app.weight.banner

import android.view.View
import com.xysss.keeplearning.R
import com.zhpan.bannerview.BaseBannerAdapter

/**
 * Author:bysd-2
 * Time:2021/9/1714:36
 */

class WelcomeBannerAdapter : BaseBannerAdapter<String, WelcomeBannerViewHolder>() {

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.banner_itemwelcome
    }

    override fun createViewHolder(itemView: View, viewType: Int): WelcomeBannerViewHolder {
        return WelcomeBannerViewHolder(itemView);
    }

    override fun onBind(
        holder: WelcomeBannerViewHolder?,
        data: String?,
        position: Int,
        pageSize: Int
    ) {
        holder?.bindData(data, position, pageSize);
    }
}
