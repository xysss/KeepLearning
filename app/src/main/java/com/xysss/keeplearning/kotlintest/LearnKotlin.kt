package com.xysss.keeplearning.kotlintest

/**
 * Author:bysd-2
 * Time:2021/9/1816:58
 */


fun main() {

    val a: Int = 10
    val b: Int = 20
    for (i in 0 until 5 step 2) {  //for in循环 until [)左闭右开
        print("large=" + largerNumber(a, b))
        println("   core" + getScore("Tom"))
    }

    val list = listOf("Apple", "Orange", "Pear")
    val maxLengthFruit = list.maxByOrNull { it.length }  //lambda 表达式
    val newList = list.filter { it.length <= 5 }.map { it.toUpperCase() }
    val anyResult = list.any { it.length <= 5 }
    val allResult = list.all { it.length <= 5 }
    for (fruit in newList) {
        println(fruit)
    }
    println("anyResult is $anyResult allResult is $allResult")  //$字符串内嵌表达式
    Thread {
        println("Thread is running")
    }.start()
    printParams(str = "word")
}

fun largerNumber(num1: Int, num2: Int) = if (num1 > num2) num1 else num2

fun getScore(name: String) = when (name) {
    "Tom" -> 86
    "jin" -> 80
    else -> 0
}

fun getTextLength(text: String?) = text?.length ?: 0  //?. 不为空执行  ？: 左边不为空执行左边，右边不为空执行右边  !! 非空断言

fun printParams(num: Int = 100, str: String) {
    println("num is $num , str is $str")
}