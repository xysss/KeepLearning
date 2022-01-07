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
   /* std::string hello = "Hello from C++";
    LOGE("my name is %s\n", "aserbao");//简约型z
    //__android_log_print(ANDROID_LOG_INFO, "android", "my name is %s\n", "aserbao"); //如果第二步省略也可以通过这个直接打印日志。

    return env->NewStringUTF(hello.c_str());*/

    std::string hello = "Hello from C++";
    // 1. 获取 thiz 的 class，也就是 java 中的 Class 信息
    jclass thisclazz = env->GetObjectClass(thiz);
    // 2. 根据 Class 获取 getClass 方法的 methodID，第三个参数是签名(params)return
    jmethodID mid_getClass = env->GetMethodID(thisclazz, "getClass", "()Ljava/lang/Class;");
    // 3. 执行 getClass 方法，获得 Class 对象
    jobject clazz_instance = env->CallObjectMethod(thiz, mid_getClass);
    // 4. 获取 Class 实例
    jclass clazz = env->GetObjectClass(clazz_instance);
    // 5. 根据 class  的 methodID
    jmethodID mid_getName = env->GetMethodID(clazz, "getName", "()Ljava/lang/String;");
    // 6. 调用 getName 方法
    jstring name = static_cast<jstring>(env->CallObjectMethod(clazz_instance, mid_getName));
    LOGE("class name:%s", env->GetStringUTFChars(name, 0));
    // 7. 释放资源
    env->DeleteLocalRef(thisclazz);
    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(clazz_instance);
    env->DeleteLocalRef(name);

    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_jni_JniKit_setIntArray(JNIEnv *env, jobject thiz, jintArray array) {
    // 1.获取数组长度
    jint len = env->GetArrayLength(array);
    LOGE("array.length:%d", len);
    jboolean isCopy;
    // 2.获取数组地址
    // 第二个参数代表 javaArray -> c/c++ Array 转换的方式：
    // 0: 把指向Java数组的指针直接传回到本地代码中
    // 1: 新申请了内存，拷贝了数组
    // 返回值： 数组的地址（首元素地址）
    jint *firstElement = env->GetIntArrayElements(array, &isCopy);
    LOGE("is copy array:%d", isCopy);
    // 3.遍历数组（移动地址）
    for (int i = 0; i < len; ++i) {
        LOGE("array[%i] = %i", i, *(firstElement + i));
    }
    // 4.使用后释放数组
    // 第一个参数是 jarray，第二个参数是 GetIntArrayElements 返回值
    // 第三个参数代表 mode
    // mode = 0 刷新java数组 并 释放c/c++数组
    //mode = JNI_COMMIT (1) 只刷新java数组
    //mode = JNI_ABORT (2) 只释放c/c++数组
    env->ReleaseIntArrayElements(array,firstElement,0);

    // 5. 创建一个 java 数组
    jintArray newArray = env->NewIntArray(3);
}

extern "C"
JNIEXPORT void JNICALL
Java_jni_JniKit_setString(JNIEnv *env, jobject thiz, jstring str) {
    // 1.jstring -> char*
    // java  中的字符创是 unicode 编码， c/C++ 是UTF编码，所以需要转换一下。第二个参数作用同上面
    const char *c_str = env -> GetStringUTFChars(str,NULL);

    // 2.异常处理
    if(c_str == NULL){
        return;
    }
    // 3.当做一个 char 数组打印
    jint len = env->GetStringLength(str);
    for (int i = 0; i < len; ++i) {
        LOGE("c_str: %c",*(c_str+i));
    }
    // 4.释放
    env->ReleaseStringUTFChars(str,c_str);

}
extern "C"
JNIEXPORT void JNICALL
Java_jni_JniKit_getStaticString(JNIEnv *env, jobject thiz) {
    // 1. 获取类 class
    jclass clazz = env->GetObjectClass(thiz);

  /*  // 2. 获取成员变量 id
    jfieldID strFieldId = env->GetFieldID(clazz,"testField","Ljava/lang/String;");
    // 3. 根据 id 获取值
    jstring jstr = static_cast<jstring>(env->GetObjectField(thiz, strFieldId));
    const char* cStr = env->GetStringUTFChars(jstr,NULL);
    LOGE("获取 MainActivity 的 String field ：%s",cStr);

    // 4. 修改 String
    jstring newValue = env->NewStringUTF("新的字符创");
    env-> SetObjectField(thiz,strFieldId,newValue);

    // 5. 释放资源
    env->ReleaseStringUTFChars(jstr,cStr);
    env->DeleteLocalRef(newValue);
    env->DeleteLocalRef(clazz);
*/
    // 获取静态变量
    jfieldID staticIntFieldId = env->GetStaticFieldID(clazz,"staticField","I");
    jint staticJavaInt = env->GetStaticIntField(clazz,staticIntFieldId);

}
extern "C"
JNIEXPORT void JNICALL
Java_jni_JniKit_findClass(JNIEnv *env, jobject thiz) {
    // 1. 获取 Class
    jclass pClazz = env->FindClass("com/xysss/keeplearning/viewmodel/Person");
    // 2. 获取构造方法，方法名固定为<init>
    jmethodID constructID = env->GetMethodID(pClazz,"<init>","(ILjava/lang/String;)V");
    if(constructID == NULL){
        return;
    }
    // 3. 创建一个 Person 对象
    jstring name = env->NewStringUTF("alex");
    jobject person = env->NewObject(pClazz,constructID,1,name);

    jmethodID printId = env->GetMethodID(pClazz,"print","()V");
    if(printId == NULL){
        return;
    }
    env->CallVoidMethod(person,printId);

    // 4. 释放资源
    env->DeleteLocalRef(name);
    env->DeleteLocalRef(pClazz);
    env->DeleteLocalRef(person);

}