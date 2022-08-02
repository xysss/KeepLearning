package com.xysss.keeplearning.ui.activity.gaode.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import android.util.Log
import com.amap.api.maps.model.LatLng
import com.xysss.mvvmhelper.base.appContext

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:31
 * 描述 : 描述
 */
class TripDBHelper(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private lateinit var mStringBuffer: StringBuffer
    private lateinit var mContentValues: ContentValues // 要插入的数据包

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
        Log.v("MYTAG", "addTrack start...")
        if (TextUtils.isEmpty(newLatLngs)) {
            Log.v("MYTAG", "Vector nodata")
            return
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
                    mStringBuffer.append(latlngs)
                    Log.v("MYTAG", "old data:$mStringBuffer")
                }
                if (!TextUtils.isEmpty(newLatLngs)) {
                    mStringBuffer.append(newLatLngs)
                    Log.v("MYTAG", "new data:$mStringBuffer")
                }
                mContentValues.apply {
                    clear()
                    put("trackid", trackid)
                    put("tracktime", tracktime)
                    put("latlngs", mStringBuffer.toString())
                }
                mContentValues.put("latlngs", mStringBuffer.toString())
                mDatabase.update(TABLAE_NAME, mContentValues, "trackid = ?", arrayOf(trackid))
                Log.v("MYTAG", "update data succ")
            } else {
                mContentValues.apply {
                    clear()
                    put("trackid", trackid)
                    put("tracktime", tracktime)
                    put("latlngs", mStringBuffer!!.append(newLatLngs).toString())
                }
                Log.v("MYTAG", "init data:$mStringBuffer")
                mDatabase.insert(TABLAE_NAME, null, mContentValues)
                Log.v("MYTAG", "init data succ")
            }
        } catch (e: Exception) {
            Log.v("MYTAG", "addTrack error:$e")
            e.printStackTrace()
        } finally {
            mDatabase?.close()
            if (!TextUtils.isEmpty(mStringBuffer.toString())) {
                mStringBuffer.delete(0, mStringBuffer.toString().length)
            }
        }
        Log.v("MYTAG", "addTrack end...")
    }

    @SuppressLint("Recycle")
    fun getTrack(trackid: String?): MutableList<LatLng?>? {
        Log.v("MYTAG", "getTrack start...")
        var mDatabase: SQLiteDatabase? = null
        var listTrack: MutableList<LatLng?>? = null
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
                Log.v("MYTAG", "hava data...")
                val latlngs = cursor.getString(cursor.getColumnIndex("latlngs"))
                if (!TextUtils.isEmpty(latlngs)) {
                    listTrack = ArrayList()
                    val lonlats = latlngs.split("\\|").toTypedArray()
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
}
