package com.xysss.keeplearning.ui.activity.gaode.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.amap.api.maps.model.LatLng
import com.xysss.keeplearning.app.ext.*
import com.xysss.keeplearning.app.room.Track
import com.xysss.mvvmhelper.base.appContext
import com.xysss.mvvmhelper.ext.logE
import java.util.*

/**
 * 作者 : xys
 * 时间 : 2022-08-01 15:31
 * 描述 : 描述
 */
class TripDBHelper(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    private var mTimeStringBuffer = StringBuffer()
    private var mConcentrationValueStringBuffer = StringBuffer()
    private var mPpmStringBuffer = StringBuffer()
    private var mCfStringBuffer = StringBuffer()
    private var mLongitudeLatitudeStringBuffer = StringBuffer()

    private var mContentValues: ContentValues?= null // 要插入的数据包

    companion object {
        private var mTripDBHelper: TripDBHelper? = null

        fun getInstance(): TripDBHelper? {
            if (mTripDBHelper == null) {
                synchronized(TripDBHelper::class.java) {
                    mTripDBHelper = TripDBHelper(appContext, mDBName, version)
                }
            }
            return mTripDBHelper
        }
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $tableName($tableTrackIdName INTEGER PRIMARY KEY AUTOINCREMENT,$tableTrackBeginTimeName Long,$tableTrackEndTimeName Long," +
                "$tableTrackTimeName text,$tableTrackConcentrationValueName text,$tableTrackPpmName text,$tableTrackCfName text,$tableTrackLongitudeLatitudeName text)")
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
    fun addTrack(track: Track) {
        if (TextUtils.isEmpty(track.longitudeLatitude)) {
            return
        }
        var mDatabase: SQLiteDatabase? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前的数据
            if (track.beginTime!=0L) {
                cursor = mDatabase.rawQuery("select * from $tableName where $tableTrackBeginTimeName = ?", arrayOf(track.beginTime.toString()))
            }
            // 如果之前存储过
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val timeValue = cursor.getString(cursor.getColumnIndex(tableTrackTimeName))
                val conValue = cursor.getString(cursor.getColumnIndex(tableTrackConcentrationValueName))
                val ppmValue = cursor.getString(cursor.getColumnIndex(tableTrackPpmName))
                val cfValue = cursor.getString(cursor.getColumnIndex(tableTrackCfName))
                val lnglats = cursor.getString(cursor.getColumnIndex(tableTrackLongitudeLatitudeName))

                //time
                if (!TextUtils.isEmpty(timeValue)) {
                    mTimeStringBuffer.append(timeValue)
                    "mTime old data:$mTimeStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.time)) {
                    mTimeStringBuffer.append(track.time)
                    "mTime new data:$mTimeStringBuffer".logE(LogFlag)
                }

                //convalue
                if (!TextUtils.isEmpty(conValue)) {
                    mConcentrationValueStringBuffer.append(conValue)
                    "conValue old data:$mConcentrationValueStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.concentrationValue)) {
                    mConcentrationValueStringBuffer.append(track.concentrationValue)
                    "conValue new data:$mConcentrationValueStringBuffer".logE(LogFlag)
                }

                //ppmValue
                if (!TextUtils.isEmpty(ppmValue)) {
                    mPpmStringBuffer.append(ppmValue)
                    "ppmValue old data:$mPpmStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.ppm)) {
                    mPpmStringBuffer.append(track.ppm)
                    "ppmValue new data:$mPpmStringBuffer".logE(LogFlag)
                }

                //cfValue
                if (!TextUtils.isEmpty(cfValue)) {
                    mCfStringBuffer.append(cfValue)
                    "cfValue old data:$mCfStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.cf)) {
                    mCfStringBuffer.append(track.cf)
                    "cfValue new data:$mCfStringBuffer".logE(LogFlag)
                }

                //lnglats
                if (!TextUtils.isEmpty(lnglats)) {
                    mLongitudeLatitudeStringBuffer.append(lnglats)
                    "latlngs old data:$mLongitudeLatitudeStringBuffer".logE(LogFlag)
                }
                if (!TextUtils.isEmpty(track.longitudeLatitude)) {
                    mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude)
                    "latlngs new data:$mLongitudeLatitudeStringBuffer".logE(LogFlag)
                }


                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }

                mContentValues?.apply {
                    clear()
                    put(tableTrackBeginTimeName, track.beginTime)
                    put(tableTrackEndTimeName, track.endTime)
                    put(tableTrackTimeName, mTimeStringBuffer.toString())
                    put(tableTrackConcentrationValueName, mConcentrationValueStringBuffer.toString())
                    put(tableTrackPpmName, mPpmStringBuffer.toString())
                    put(tableTrackCfName, mCfStringBuffer.toString())
                    put(tableTrackLongitudeLatitudeName, mLongitudeLatitudeStringBuffer.toString())
                }
                //mContentValues?.put(tableTrackLongitudeLatitudeName, mStringBuffer.toString())
                mDatabase.update(tableName, mContentValues, "$tableTrackBeginTimeName = ?", arrayOf(track.beginTime.toString()))
            } else {
                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }
                mContentValues?.apply {
                    clear()
                    put(tableTrackBeginTimeName, track.beginTime)
                    put(tableTrackEndTimeName, track.endTime)
                    put(tableTrackTimeName, mTimeStringBuffer.append(track.time).toString())
                    put(tableTrackConcentrationValueName, mConcentrationValueStringBuffer.append(track.concentrationValue).toString())
                    put(tableTrackPpmName, mPpmStringBuffer.append(track.ppm).toString())
                    put(tableTrackCfName, mCfStringBuffer.append(track.cf).toString())
                    put(tableTrackLongitudeLatitudeName, mLongitudeLatitudeStringBuffer.append(track.longitudeLatitude).toString())
                }
                mDatabase.insert(tableName, null, mContentValues)
            }
        } catch (e: Exception) {
            "addTrack error:$e".logE(LogFlag)
            e.printStackTrace()
        } finally {
            mDatabase?.close()
            if (!TextUtils.isEmpty(mTimeStringBuffer.toString())) {
                mTimeStringBuffer.delete(0, mTimeStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mConcentrationValueStringBuffer.toString())) {
                mConcentrationValueStringBuffer.delete(0, mConcentrationValueStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mPpmStringBuffer.toString())) {
                mPpmStringBuffer.delete(0, mPpmStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mCfStringBuffer.toString())) {
                mCfStringBuffer.delete(0, mCfStringBuffer.toString().length)
            }
            if (!TextUtils.isEmpty(mLongitudeLatitudeStringBuffer.toString())) {
                mLongitudeLatitudeStringBuffer.delete(0, mLongitudeLatitudeStringBuffer.toString().length)
            }
        }
    }

    @SuppressLint("Recycle")
    fun getTrack(beginTime: Long): MutableList<LatLng>? {
        "getTrack start...".logE(LogFlag)
        var mDatabase: SQLiteDatabase? = null
        var listTrack: MutableList<LatLng>? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前beginTime的数据
            if (beginTime!=0L) {
                cursor = mDatabase.rawQuery("select * from $tableName where $tableTrackBeginTimeName = ?", arrayOf(beginTime.toString())
                )
            }
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val latlngs = cursor.getString(cursor.getColumnIndex(tableTrackLongitudeLatitudeName))
                if (!TextUtils.isEmpty(latlngs)) {
                    listTrack = ArrayList()
                    val lonlats = latlngs.split(delim).toTypedArray()
                    if (lonlats.isNotEmpty()) {
                        for (i in lonlats.indices) {
                            val lonlat = lonlats[i]
                            val split = lonlat.split(",").toTypedArray()
                            if (split.isNotEmpty()) {
                                try {
                                    listTrack.add(LatLng(split[0].toDouble(), split[1].toDouble()))
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
