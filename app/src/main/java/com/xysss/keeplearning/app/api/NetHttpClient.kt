package com.xysss.keeplearning.app.api


import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.net.interception.LogInterceptor
import rxhttp.wrapper.cookie.CookieStore
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import java.io.File

/**
 * Author:bysd-2
 * Time:2021/9/2716:15
 */

object NetHttpClient {
    fun getDefaultOkHttpClient():  OkHttpClient.Builder {
        //在这里面可以写你想要的配置 太多了，我就简单的写了一点，具体可以看rxHttp的文档，有很多
        return OkHttpClient.Builder()
            //使用CookieStore对象磁盘缓存,自动管理cookie 玩安卓自动登录验证
            .cookieJar(CookieStore(File(appContext.externalCacheDir, "RxHttpCookie")))
            .connectTimeout(15, TimeUnit.SECONDS)//读取连接超时时间 15秒
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HeadInterceptor())//自定义头部参数拦截器
            .addInterceptor(LogInterceptor())//添加Log拦截器
    }
}