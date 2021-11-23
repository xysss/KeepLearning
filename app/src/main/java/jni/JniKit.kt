package jni

/**
 * Author:bysd-2
 * Time:2021/11/511:13
 */
object JniKit {
    init {
        System.loadLibrary("jTest")
    }
    external fun stringFromJNI(): String
}