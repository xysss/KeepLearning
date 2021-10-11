package com.xysss.keeplearning.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author:bysd-2
 * Time:2021/10/1115:19
 */
@Entity
data class User(var firstName: String, var lastName: String, var age: Int) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}