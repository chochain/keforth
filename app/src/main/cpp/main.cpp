///
/// @file
/// @brief eForth main program for testing on Desktop PC (Linux and Cygwin)
///
#include <map>           // std::map
#include <iostream>      // cin, cout
#include <fstream>       // ifstream
#include <cstdint>
#include <sys/sysinfo.h>
#include <jni.h>

using namespace std;

extern void forth_init();
extern int  forth_vm(const char *cmd, void(*)(int, const char*)=NULL);
extern void forth_teardown();

extern void sensor_engine_start(struct android_app *app);
extern void sensor_add(int type_id, int peroid);
extern void sensor_read(int *data, int len);
///====================================================================
///
///> Memory statistics - for heap, stack, external memory debugging
///
typedef uint64_t U64;

void mem_stat() {
    // to JNI
}
void forth_include(const char *fn) {
    // to JNI
}

JavaVM    *gJVM        = nullptr;            ///< Main JVM
jobject   gMainObj     = nullptr;            ///< MainActivity object
jmethodID gTimerTickID = nullptr;            ///< onNativeTick()
jobject   gForthObj    = nullptr;            ///< Eforth Activity object
jmethodID gForthPostID = nullptr;            ///< onNativeForth(rst)
jmethodID gJavaCmdID   = nullptr;            ///< handleJavaAPI(cmd)

void android_main(struct android_app *app) {
    sensor_engine_start(app);
}

void android_tick() {                        ///< timer callback (called by ceforth_task)
    JNIEnv *env;
    gJVM->AttachCurrentThread(&env, nullptr);
    env->CallVoidMethod(gMainObj, gTimerTickID);
    gJVM->DetachCurrentThread();
}

void js_call(const char *cmd) {              ///< Java "scripting" API
    JNIEnv *env;
    gJVM->GetEnv((void**)&env, JNI_VERSION_1_6);
    env->CallVoidMethod(
            gForthObj, gJavaCmdID, env->NewStringUTF(cmd));
}

extern "C"
{
    JNIEXPORT void JNICALL
    Java_com_keforth_MainActivity_jniMainInit(JNIEnv* env, jobject thiz) {
        if (gMainObj != nullptr) env->DeleteGlobalRef(gMainObj);

        env->GetJavaVM(&gJVM);                             /// * capture JVM
        gMainObj = env->NewGlobalRef(thiz);                /// * create global reference

        jclass cb = env->FindClass("com/keforth/MainActivity");
        gTimerTickID = env->GetMethodID(cb, "onNativeTick", "()V");

        env->DeleteLocalRef(cb);
    }
    
    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniInit(JNIEnv *env, jobject thiz) {
        if (gForthObj != nullptr) env->DeleteGlobalRef(gForthObj);

        gForthObj = env->NewGlobalRef(thiz);
        jclass cb = env->GetObjectClass(gForthObj);
        gForthPostID = env->GetMethodID(cb, "onNativeForthFeedback", "(Ljava/lang/String;)V");
        gJavaCmdID   = env->GetMethodID(cb, "onNativeJavaCmd", "(Ljava/lang/String;)V");
        env->DeleteLocalRef(cb);

        forth_init();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniTeardown(JNIEnv *env, jobject thiz) {
        forth_teardown();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniOuter(JNIEnv *env, jobject thiz, jstring js) {
        static JNIEnv *gEnv;
        // Convert the Java string to a C-style string
        const char *cmd = env->GetStringUTFChars(js, nullptr);

        // Check for null if the string conversion fails (e.g., out of memory)
        if (env==nullptr || cmd==nullptr) return;

        gEnv = env;                            /// * capture JNI Environment
        ///> forth_vm(nullptr) process timer interrupt
        forth_vm(cmd, [](int, const char *rst){
            /// send Forth response to Eforth.onNativeForthFeedback
            gEnv->CallVoidMethod(
                gForthObj, gForthPostID, gEnv->NewStringUTF(rst));
        });
        
        env->ReleaseStringUTFChars(js, cmd);   /// release js, cmd
    }
}
