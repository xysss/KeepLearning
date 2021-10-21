package com.xysss.keeplearning.app.api

import rxhttp.wrapper.annotation.DefaultDomain

/**
* 作者　: xys
* 时间　: 2021/09/27
* 描述　:
*/
object NetUrl {

    // 服务器请求成功的 Code值
    const val SUCCESS_CODE = 0

    @DefaultDomain //设置为默认域名
    const val DEV_URL = "https://wanandroid.com/"

    //登录
    const val LOGIN = "user/login"
    //获取首页列表数据
    const val HOME_LIST = "article/list/%1\$d/json"
    //项目分类标题
    const val PROJECT_TITLE_LIST = "project/tree/json"

    const val UPLOAD_URL = "http://t.xinhuo.com/index.php/Api/Pic/uploadPic"

    const val DOWNLOAD_URL = "http://update.9158.com/miaolive/Miaolive.apk"

}