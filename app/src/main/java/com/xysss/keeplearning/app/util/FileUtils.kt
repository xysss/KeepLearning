package com.xysss.keeplearning.app.util

import android.content.Context
import android.os.Environment
import com.xysss.keeplearning.app.room.Alarm
import com.xysss.keeplearning.app.room.Record
import com.xysss.mvvmhelper.base.appContext
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-03-04 10:41
 * 描述 : 描述
 */
object FileUtils {

    val sdPath = appContext.getExternalFilesDir(null)?.path + "/vp200/"
    const val recordFileName="数据记录.txt"
    const val AlarmFileName="报警记录.txt"


    fun deleteSingleFile(filePathName:String):Boolean {
        var file =  File(filePathName)
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return if (file.exists() && file.isFile()) {
            file.delete()
        } else {
            false
        }
    }

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

    private fun hasSdcard(): Boolean {
        val status = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == status
    }

    fun saveRecord(record: Record){
//        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
//        var timeMoment=simpleDateFormat.format(System.currentTimeMillis()) + "\r\n"
//        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
//        val time = formatter.format(Date())
//        val fileName = "数据记录.txt"

        if (hasSdcard()) {
            val file = File(sdPath)
            if (!file.exists()) {
                file.mkdir()
            }
            var dataText="时间:${record.timestamp}   报警状态:${record.alarm}   cf数值:${record.cf}   数值:${record.ppm}\n"

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(sdPath + recordFileName, true)
                fos.write(dataText.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    fun saveAlarm(alarm :Alarm){
//        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
//        var timeMoment=simpleDateFormat.format(System.currentTimeMillis()) + "\r\n"
//        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
//        val time = formatter.format(Date())
//        val fileName = "数据记录.txt"

        if (hasSdcard()) {
            val file = File(sdPath)
            if (!file.exists()) {
                file.mkdir()
            }
            var dataText="时间:${alarm.timestamp}   报警状态:${alarm.state}   报警类型:${alarm.type}   数值:${alarm.value}\n"

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(sdPath + AlarmFileName, true)
                fos.write(dataText.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }


}
