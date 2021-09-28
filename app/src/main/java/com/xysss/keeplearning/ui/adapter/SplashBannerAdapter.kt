package com.xysss.keeplearning.ui.adapter

import com.xysss.keeplearning.R
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder

/**
 * Author:bysd-2
 * Time:2021/9/2811:10
 */
class SplashBannerAdapter : BaseBannerAdapter<Int>(){
    override fun bindData(holder: BaseViewHolder<Int>?, data: Int?, position: Int, pageSize: Int) {
        holder?:return
        data?:return
        holder.setImageResource(R.id.banner_img,data)
    }

    override fun getLayoutId(viewType: Int) = R.layout.layout_splach_banner
}