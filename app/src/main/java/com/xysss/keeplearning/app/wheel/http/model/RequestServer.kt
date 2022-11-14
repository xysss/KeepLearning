package com.xysss.keeplearning.app.wheel.http.model

import com.hjq.http.config.IRequestServer
import com.hjq.http.model.BodyType
import com.xysss.keeplearning.app.api.NetUrl

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/10/02
 *    desc   : 服务器配置
 */
class RequestServer : IRequestServer {

    override fun getHost(): String {
        return NetUrl.DEV_URL
    }

    override fun getPath(): String {
        return "api/"
    }

    override fun getType(): BodyType {
        // 以表单的形式提交参数
        return BodyType.FORM
    }
}