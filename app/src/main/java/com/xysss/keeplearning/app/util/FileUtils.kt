package com.xysss.keeplearning.app.util

import android.content.Context
import com.xysss.mvvmhelper.base.appContext
import java.io.*
import java.nio.charset.Charset

/**
 * 作者 : xys
 * 时间 : 2022-03-04 10:41
 * 描述 : 描述
 */
object FileUtils {

    fun saveString(str: String) {
        try {
            val fileOutput = appContext.openFileOutput("data", Context.MODE_PRIVATE)
            val bfs = BufferedWriter(OutputStreamWriter(fileOutput))
            bfs.use {
                it.write(str)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readString(): String {
        val sb = StringBuilder()
        try {
            val openFileInput = appContext.openFileInput("data")
            val bis = BufferedReader(InputStreamReader(openFileInput))
            bis.use {
                it.forEachLine {
                    sb.append(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    //覆盖写文件
    fun writeFile(text: String, destFile: String) {
        val f = File(destFile)
        if (!f.exists()) {
            f.createNewFile()
        }
        f.writeText(text, Charset.defaultCharset())
    }

    //末尾追加写文件
    fun appendFile(text: String, destFile: String) {
        val f = File(destFile)
        if (!f.exists()) {
            f.createNewFile()
        }
        f.appendText(text, Charset.defaultCharset())
    }

}