package com.xysss.keeplearning.app.util

import android.content.Context
import android.os.Environment
import com.xysss.mvvmhelper.base.appContext
import java.io.*
import java.nio.charset.Charset
import java.text.SimpleDateFormat

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

    fun hasSdcard(): Boolean {
        val status = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == status
    }

    fun saveRecord(data: String, fileName: String): String? {
        val format = """
             ${
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis())
        }
             
             """.trimIndent()
        var sb = ""
        //DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //String time = formatter.format(new Date());
        //String fileName = "crash-" + time + ".txt";
        if (fileName == "自检记录") {
            sb = """
            $data
            
            """.trimIndent()
        } else if (fileName == "故障记录") {
            sb = """
            $format$data
            
            """.trimIndent()
        }
        if (hasSdcard()) {
            val path =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "EF1000" + File.separator
            val dir = File(path)
            if (!dir.exists()) dir.mkdirs()
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream("$path$fileName.txt", true)
                fos.write(sb.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return fileName
    }


}