package com.xysss.keeplearning.ui.activity.gaode.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.amap.api.maps.model.LatLng
import com.xysss.keeplearning.app.ble.BleCallback
import com.xysss.keeplearning.app.ext.LogFlag
import com.xysss.keeplearning.data.response.MaterialInfo
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:31
 * 描述 : 描述
 */
class TripDBHelper(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    private var mStringBuffer: StringBuffer?= null
    private var mContentValues: ContentValues?= null // 要插入的数据包
    private lateinit var drawMapCallBack: DrawMapCallBack

    fun setDrawMapCallBack(drawMapCallBack: DrawMapCallBack) {
        this.drawMapCallBack = drawMapCallBack
    }

    companion object {
        private var mTripDBHelper: TripDBHelper? = null
        private val mDBName = "yesway_track.db"
        private val VERSION = 1
        private val TABLAE_NAME = "track"

        fun getInstance(): TripDBHelper? {
            if (mTripDBHelper == null) {
                synchronized(TripDBHelper::class.java) {
                    mTripDBHelper = TripDBHelper(appContext, mDBName, VERSION)
                }
            }
            return mTripDBHelper
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table $TABLAE_NAME(trackid varchar(64),tracktime varchar(20),latlngs text)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    /**
     * 添加轨迹数据
     *
     * @param
     * @param trackid
     * @param tracktime
     * @param newLatLngs
     */
    @SuppressLint("Recycle")
    fun addTrack(trackid: String?, tracktime: String?, newLatLngs: String?) {
        "addTrack start...".logE(LogFlag)
        if (TextUtils.isEmpty(newLatLngs)) {
            "Vector nodata".logE(LogFlag)
            return
        }
        if (mStringBuffer == null) {
            mStringBuffer = StringBuffer()
        }
        var mDatabase: SQLiteDatabase? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前trackid的数据
            if (!TextUtils.isEmpty(trackid)) {
                cursor = mDatabase.rawQuery(
                    "select * from $TABLAE_NAME where trackid = ?",
                    arrayOf(trackid)
                )
            }
            // 如果之前存储过
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val latlngs = cursor.getString(cursor.getColumnIndex("latlngs"))
                if (!TextUtils.isEmpty(latlngs)) {
                    mStringBuffer?.append(latlngs)
                    "old data:$mStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(newLatLngs)) {
                    mStringBuffer?.append(newLatLngs)
                    "new data:$mStringBuffer".logE(LogFlag)
                }
                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }
                mContentValues?.apply {
                    clear()
                    put("trackid", trackid)
                    put("tracktime", tracktime)
                    put("latlngs", mStringBuffer.toString())
                }
                mContentValues?.put("latlngs", mStringBuffer.toString())
                mDatabase.update(TABLAE_NAME, mContentValues, "trackid = ?", arrayOf(trackid))
                "update data succ".logE(LogFlag)
            } else {
                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }
                mContentValues?.apply {
                    clear()
                    put("trackid", trackid)
                    put("tracktime", tracktime)
                    put("latlngs", mStringBuffer!!.append(newLatLngs).toString())
                }
                "init data:$mStringBuffer".logE(LogFlag)
                mDatabase.insert(TABLAE_NAME, null, mContentValues)
                "init data succ".logE(LogFlag)
            }
        } catch (e: Exception) {
            "addTrack error:$e".logE(LogFlag)
            e.printStackTrace()
        } finally {
            mDatabase?.close()
            if (!TextUtils.isEmpty(mStringBuffer.toString())) {
                mStringBuffer?.delete(0, mStringBuffer.toString().length)
            }
        }
        "addTrack end...".logE(LogFlag)
        //drawMapCallBack.realData(true)

    }

    @SuppressLint("Recycle")
    fun getTrack(trackid: String?): MutableList<LatLng>? {
        "getTrack start...".logE(LogFlag)
        var mDatabase: SQLiteDatabase? = null
        var listTrack: MutableList<LatLng>? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前trackid的数据
            if (!TextUtils.isEmpty(trackid)) {
                cursor = mDatabase.rawQuery(
                    "select * from $TABLAE_NAME where trackid = ?",
                    arrayOf(trackid)
                )
            }
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                "hava data...".logE(LogFlag)
                val latlngs = cursor.getString(cursor.getColumnIndex("latlngs"))
                if (!TextUtils.isEmpty(latlngs)) {
                    listTrack = ArrayList()
                    val delim = "￥"
                    val lonlats = latlngs.split(delim).toTypedArray()
                    if (lonlats.isNotEmpty()) {
                        for (i in lonlats.indices) {
                            val lonlat = lonlats[i]
                            val split = lonlat.split(",").toTypedArray()
                            if (split.isNotEmpty()) {
                                try {
                                    listTrack.add(LatLng(java.lang.Double.valueOf(split[0]), java.lang.Double.valueOf(split[1])))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mDatabase?.close()
        }
        return listTrack
    }

    interface DrawMapCallBack {
        fun realData(isRec :Boolean)
    }
}
