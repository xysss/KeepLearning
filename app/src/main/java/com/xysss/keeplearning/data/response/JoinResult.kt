package com.xysss.keeplearning.data.response

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者 : xys
 * 时间 : 2022-02-17 10:08
 * 描述 : 描述
 */

class JoinResult(
    val id:Long,
    val timestamp: String,
    val cf: String,
    val alarm: String,  //报警状态
    val userId: String,  //用户ID
    val placeId: String,  //地点ID
    val matterName: String?,
    val recordName: String?,
    val ppm: String?
    )