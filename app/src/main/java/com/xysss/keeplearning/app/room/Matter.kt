package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-16 10:41
 * 描述 : 描述
 */
@Entity(tableName = "Matter",
    foreignKeys = [
        ForeignKey(entity = Record::class,
            parentColumns = ["id"],
            childColumns = ["mId"],
            onDelete = CASCADE)],
    //indices = [Index(value= ["voc_index_matter"],unique = true)]
    )
data class Matter(
    val voc_index_matter: Int,
    val matterName: String,
    val mcfNum: String,
) {
    @PrimaryKey(autoGenerate = true)
    var mId = 0L
}