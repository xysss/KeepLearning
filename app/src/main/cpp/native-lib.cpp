#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "learnJNI" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

extern "C"
JNIEXPORT jstring JNICALL
Java_jni_JniKit_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    LOGE("my name is %s\n", "aserbao");//简约型z
    //__android_log_print(ANDROID_LOG_INFO, "android", "my name is %s\n", "aserbao"); //如果第二步省略也可以通过这个直接打印日志。

    return env->NewStringUTF(hello.c_str());
}