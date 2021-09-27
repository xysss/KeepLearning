package com.xysss.keeplearning.app.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Author:bysd-2
 * Time:2021/9/2716:16
 */

class HeadInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        //模拟了2个公共参数
        builder.addHeader("token", "token123456").build()
        builder.addHeader("device", "Android").build()
        return chain.proceed(builder.build())
    }

}