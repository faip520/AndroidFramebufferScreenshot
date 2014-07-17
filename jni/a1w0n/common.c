#include "common.h"
#include "termExec.h"

// 这个必须要有，不然Log功能不能使用
#define LOG_TAG "a1w0n"

/*
 * 给每个类注册它们的本地方法
 */
int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className);

    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

// ----------------------------------------------------------------------------

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    JNIEnv* env = NULL;

    LOGI("A1w0n : JNI_OnLoad");

    // 从JavaVM获取JNIEnv，一般使用1.4的版本
    if ((*vm)->GetEnv(vm, (void**)&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        return -1;
    }

    // 从VM获取到了JNIEnv对象
    env = uenv.env;

    if (init_Exec(env) != JNI_TRUE) {
        LOGE("ERROR: init of Exec failed");
        return -1;
    }

    return JNI_VERSION_1_4;
}
