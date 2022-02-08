package com.xysss.keeplearning.viewmodel

import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.xysss.keeplearning.app.api.NetUrl
import com.xysss.keeplearning.app.util.Android10DownloadFactory
import com.xysss.keeplearning.app.util.UriUtils
import com.xysss.mvvmhelper.base.BaseViewModel
import com.xysss.mvvmhelper.base.appContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp
import java.io.File

class BlueToothViewModel : BaseViewModel()  {

    /**
     * 下载
     * @param downLoadData Function1<ProgressT<String>, Unit>
     * @param downLoadSuccess Function1<String, Unit>
     * @param downLoadError Function1<Throwable, Unit>
     */

    fun downLoad(downLoadData: (Progress) -> Unit = {}, downLoadSuccess: (String) -> Unit, downLoadError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            if (checkedAndroidQ()) {
                //android 10 以上
                val factory = Android10DownloadFactory(appContext, "${System.currentTimeMillis()}.apk")
                RxHttp.get(NetUrl.DOWNLOAD_URL)
                    .toFlow(factory) {
                        downLoadData.invoke(it)
                    }.catch {
                        //异常回调
                        downLoadError(it)
                    }.collect {
                        //成功回调
                        downLoadSuccess.invoke(UriUtils.getFileAbsolutePath(appContext,it)?:"")
                    }
            } else {
                //android 10以下
                val localPath = appContext.externalCacheDir!!.absolutePath + "/${System.currentTimeMillis()}.apk"
                RxHttp.get(NetUrl.DOWNLOAD_URL)
                    .toFlow(localPath) {
                        downLoadData.invoke(it)
                    }.catch {
                        //异常回调
                        downLoadError(it)
                    }.collect {
                        //成功回调
                        downLoadSuccess.invoke(it)
                    }
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
    fun upload(filePath: String, uploadData: (Progress) -> Unit = {}, uploadSuccess: (String) -> Unit, uploadError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            if (checkedAndroidQ() && filePath.startsWith("content:")) {
                //android 10 以上
                RxHttp.postForm(NetUrl.UPLOAD_URL)
                    .addPart(appContext, "apkFile", Uri.parse(filePath))
                    .toFlow<String> {
                        //上传进度回调,0-100，仅在进度有更新时才会回调
                        uploadData.invoke(it)
                    }.catch {
                        //异常回调
                        uploadError.invoke(it)
                    }.collect {
                        //成功回调
                        uploadSuccess.invoke(it)
                    }
            } else {
                // android 10以下
                val file = File(filePath)
                if(!file.exists()){
                    uploadError.invoke(Exception("文件不存在"))
                    return@launch
                }
                RxHttp.postForm(NetUrl.UPLOAD_URL)
                    .addFile("apkFile", file)
                    .toFlow<String> {
                        //上传进度回调,0-100，仅在进度有更新时才会回调
                        uploadData.invoke(it)
                    }.catch {
                        //异常回调
                        uploadError.invoke(it)
                    }.collect {
                        //成功回调
                        uploadSuccess.invoke(it)
                    }
            }

        }
    }
}

private fun checkedAndroidQ(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
