#include <signal.h>
#include <unistd.h>
#include <jni.h>
#include <string>

const char* SYSTEM_BIN_PATH = "/system/bin/";

extern "C" JNIEXPORT jstring JNICALL
Java_app_compile_MainActivity_getSystemBinPath(JNIEnv *env, jobject) {
    return env->NewStringUTF(SYSTEM_BIN_PATH);
}

const char* PRIVATE_BIN_PATH = "/sdcard/Android/data/adb.shell.shizuku/files/shizuku/";

extern "C" JNIEXPORT jstring JNICALL
Java_app_compile_MainActivity_getPrivateBinPath(JNIEnv *env, jobject) {
    return env->NewStringUTF(PRIVATE_BIN_PATH);
}

const char* KSU_BIN_PATH = "/data/adb/ksu/bin/";

extern "C" JNIEXPORT jstring JNICALL
Java_app_compile_MainActivity_getKsuBinPath(JNIEnv *env, jobject) {
    return env->NewStringUTF(KSU_BIN_PATH);
}

const char* VENDOR_BIN_PATH = "/vendor/bin/";

extern "C" JNIEXPORT jstring JNICALL
Java_app_compile_MainActivity_getVendorBinPath(JNIEnv *env, jobject) {
    return env->NewStringUTF(VENDOR_BIN_PATH);
}

const char* SYSTEMS_BIN_PATH = "/system_ext/bin/";

extern "C" JNIEXPORT jstring JNICALL
Java_app_compile_MainActivity_getSystemsBinPath(JNIEnv *env, jobject) {
    return env->NewStringUTF(SYSTEMS_BIN_PATH);
}