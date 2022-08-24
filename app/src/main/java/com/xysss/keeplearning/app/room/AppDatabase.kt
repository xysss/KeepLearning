package com.xysss.keeplearning.app.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xysss.mvvmhelper.base.appContext

/**
 * Author:bysd-2
 * Time:2021/10/1115:22
 */

@Database(version = 1, entities = [Record::class,Alarm::class,Matter::class,Survey::class],exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dataRecordDao(): RecordDao
    abstract fun dataAlarmDao(): AlarmDao
    abstract fun dataMatterDao(): MatterDao
    abstract fun dataSurveyDao(): SurveyDao

    companion object {

//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("create table Book (id integer primary key autoincrement not null, name text not null, pages integer not null)")
//            }
//        }
//
//        private val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("alter table Book add column author text not null default 'unknown'")
//            }
//        }

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(appContext, AppDatabase::class.java, "app_database")
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build().apply {
                    instance = this
                }
        }
    }

}