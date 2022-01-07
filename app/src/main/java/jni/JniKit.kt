package jni

/**
 * Author:bysd-2
 * Time:2021/11/511:13
 */
object JniKit {
    init {
        System.loadLibrary("jTest")
    }
    var staticField = 1
    var testField = "test1"

    external fun stringFromJNI(): String
    external fun setIntArray(arrayInt :IntArray)
    external fun setString(str:String)
    external fun getStaticString()
    external fun findClass()
}