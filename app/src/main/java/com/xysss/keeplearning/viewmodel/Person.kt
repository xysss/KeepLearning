package com.xysss.keeplearning.viewmodel

import com.xysss.mvvmhelper.ext.logD

/**
 * Author:bysd-2
 * Time:2021/12/1515:54
 */
class Person(val age:Int,val name:String) {
    fun print(){
        "Person"+name + age + "岁了".logD()
    }
}