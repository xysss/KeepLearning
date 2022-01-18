package com.xysss.keeplearning.app.api


import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.net.interception.LogInterceptor
import rxhttp.wrapper.cookie.CookieStore
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import rxhttp.wrapper.ssl.HttpsUtils
import java.io.File

/**
 * Author:bysd-2
 * Time:2021/9/2716:15
 */

object NetHttpClient {
    fun getDefaultOkHttpClient():  OkHttpClient.Builder {
        val sslParams = HttpsUtils.getSslSocketFactory()
        return OkHttpClient.Builder()
            //使用CookieStore对象磁盘缓存,自动管理cookie 玩安卓自动登录验证
            .cookieJar(CookieStore(File(appContext.externalCacheDir, "RxHttpCookie")))
            .connectTimeout(15, TimeUnit.SECONDS)//读取连接超时时间 15秒
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HeadInterceptor())//自定义头部参数拦截器
            .addInterceptor(LogInterceptor())//添加Log拦截器
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier { hostname, session -> true } //忽略host验证
    }
}