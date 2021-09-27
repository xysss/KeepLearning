package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.data.model.bean.UserInfo
import com.xysss.keeplearning.data.response.ApiPagerResponse
import rxhttp.IAwait

/**
 * 作者　: xys
 * 时间　: 2021/09/27
 * 描述　: 数据仓库
 */
object UserRepository {

    /**
     * 登录
     */
    fun login(userName: String, password: String): IAwait<UserInfo> {
        return RxHttp.postForm(NetUrl.LOGIN)
            .add("username", userName)
            .add("password", password)
            .toResponse()
    }

    /**
     * 获取列表信息
     */
    fun getList(pageIndex: Int): IAwait<ApiPagerResponse<Any>> {
        return RxHttp.get(NetUrl.HOME_LIST, pageIndex)
            .toResponse()
    }

}

