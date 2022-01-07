package com.xysss.keeplearning.data.repository

import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.network.ListDataUiState
import com.xysss.keeplearning.data.response.ApiPagerResponse
import com.xysss.keeplearning.data.response.UserInfo
import com.xysss.keeplearning.viewmodel.AriticleResponse
import com.xysss.keeplearning.viewmodel.ClassifyResponse
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toResponse

/**
 * 作者　: xys
 * 时间　: 2021/09/27
 * 描述　: 数据仓库
 */
object UserRepository {

    /**
     * 登录
     */
    fun login(userName: String, password: String): Await<UserInfo> {
        return RxHttp.postForm(NetUrl.LOGIN)
            .add("username", userName)
            .add("password", password)
            .toResponse()
    }

    /**
     * 获取列表信息
     */
    fun getList(pageIndex: Int): Await<ApiPagerResponse<Any>> {
        return RxHttp.get(NetUrl.HOME_LIST, pageIndex)
            .toResponse()
    }

    /**
     * 获取项目分类
     */
    fun getProjectTitleData(): Await<ArrayList<ClassifyResponse>> {
        return RxHttp.get(NetUrl.PROJECT_TITLE_LIST)
            .toResponse()
    }

    /**
     * 获取项目标题数据
     */
    fun getProjectData(pageIndex: Int): Await<ListDataUiState<AriticleResponse>> {
        return RxHttp.get(NetUrl.Project_Data, pageIndex)
            .toResponse()
    }

    /**
     * 获取项目分类
     */
    fun getPublicTitle(): Await<ArrayList<ClassifyResponse>> {
        return RxHttp.get(NetUrl.Public_Title)
            .toResponse()
    }

    /**
     * 获取项目标题数据
     */
    fun getPublicData(pageIndex: Int, id: Int): Await<ListDataUiState<AriticleResponse>> {
        return RxHttp.get(NetUrl.Public_Data, id, pageIndex)
            /*.add("page",pageIndex)
            .add("id",id)*/
            .toResponse()
    }

    fun setDateShow(): Await<Any> {
        return RxHttp.get("http://192.168.1.254/MateCam/PHOTO/snap.jpg")
            .toResponse()
    }
}

