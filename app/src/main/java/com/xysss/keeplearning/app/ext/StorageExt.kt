package com.xysss.keeplearning.app.ext

import com.tencent.mmkv.MMKV
import com.xysss.keeplearning.app.room.AppDatabase
import com.xysss.keeplearning.data.annotation.ValueKey

/**
 * 作者　: xys
 * 时间　: 2021/09/27
 * 描述　:
 */

/**
 * 获取MMKV
 */

val dataRecordDao = AppDatabase.getDatabase().dataRecordDao()
val dataAlarmDao = AppDatabase.getDatabase().dataAlarmDao()
val dataMatterDao = AppDatabase.getDatabase().dataMatterDao()

val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    MMKV.mmkvWithID(ValueKey.MMKV_APP_KEY)
}