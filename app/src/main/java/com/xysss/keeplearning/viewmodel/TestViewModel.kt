package com.xysss.keeplearning.viewmodel

import android.net.Uri
import androidx.lifecycle.rxLifeScope
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.util.Android10DownloadFactory
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.awaitResult
import rxhttp.toDownload
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp

/**
 * Author:bysd-2
 * Time:2021/9/2811:12
 */

class TestViewModel : BaseViewModel() {

    /**
     * 下载
     * @param downLoadData Function1<ProgressT<String>, Unit>
     * @param downLoadSuccess Function1<String, Unit>
     * @param downLoadError Function1<Throwable, Unit>
     */
    fun downLoad(downLoadData:((Progress) -> Unit) = {}, downLoadSuccess:(String)->Unit, downLoadError:(Throwable)->Unit = {}){
        rxLifeScope.launch{
            val factory = Android10DownloadFactory(appContext, "${System.currentTimeMillis()}.apk")
            RxHttp.get(NetUrl.DOWNLOAD_URL)
                .toDownload(factory, true) {
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    //下载中回调
                    downLoadData.invoke(it)
                }.awaitResult {
                    downLoadSuccess.invoke(it.toString())
                }.onFailure {
                    //异常回调
                    downLoadError.invoke(it)
                }
        }
    }

    /**
     * android 10 及以上文件上传 ，兼容Android 10以下
     * 注意：这里并非通过 [Await] 实现的， 而是通过 [Flow] 监听的进度，因为在监听上传进度这块，Flow性能更优，且更简单
     * 如不需要监听进度，toFlow 方法不要传进度回调即可
     * @param uploadData Function1<Progress, Unit>
     * @param uploadSuccess Function1<String, Unit>
     * @param uploadError Function1<Throwable, Unit>
     */
    fun upload(filePath:String, uploadData:(Progress)->Unit = {}, uploadSuccess:(String)->Unit, uploadError:(Throwable)->Unit = {}) {
        rxLifeScope.launch {
            RxHttp.postForm(NetUrl.UPLOAD_URL)
                .addPart(appContext, "apkFile", Uri.parse(filePath))
                .toFlow<String> {
                    //上传进度回调,0-100，仅在进度有更新时才会回调
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize  //当前已上传的字节大小
                    val totalSize = it.totalSize      //要上传的总字节大小
                    //上传进度回调
                    uploadData.invoke(it)
                }.catch {
                    //失败回调
                    uploadError.invoke(it)
                }.collect{
                    uploadSuccess.invoke(it)
                }
        }
    }
}