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
extern void sensor_setup(int type_id, int peroid);
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

JavaVM    *gJVM        = nullptr;            ///<
jobject   gForthObj    = nullptr;            ///< Eforth Activity object
jmethodID gTimerTickID = nullptr;            ///< onNativeTick()
jmethodID gForthPostID = nullptr;            ///< onNativeForth(rst)

void android_main(struct android_app *app) {
    sensor_engine_start(app);
}

void android_tick() {                        ///< timer callback (called by ceforth_task)
    JNIEnv *env;
    jint rst = gJVM->AttachCurrentThread(&env, nullptr);
    if (rst != JNI_OK) {
        gJVM->DetachCurrentThread();
        return;
    }

    env->CallVoidMethod(gForthObj, gTimerTickID);

    gJVM->DetachCurrentThread();
}

extern "C"
{
    JNIEXPORT jint JNICALL
    JNI_OnLoad(JavaVM *vm, void *reserved) {
        gJVM = vm;
        return JNI_VERSION_1_6;
    }
    
    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniInit(JNIEnv *env, jobject thiz) {
        if (gForthObj != nullptr) env->DeleteGlobalRef(gForthObj);

        gForthObj = env->NewGlobalRef(thiz);
        jclass cb = env->GetObjectClass(gForthObj);

        gTimerTickID = env->GetMethodID(cb, "onNativeTick", "()V");
        gForthPostID = env->GetMethodID(cb, "onNativeForth", "(Ljava/lang/String;)V");

        env->DeleteLocalRef(cb);

        forth_init();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniTeardown(JNIEnv *env, jobject thiz) {
        // process fque
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniOuter(JNIEnv *env, jobject thiz, jstring js) {
        static JNIEnv *gEnv;
        // Convert the Java string to a C-style string
        const char *cmd = env->GetStringUTFChars(js, nullptr);

        // Check for null if the string conversion fails (e.g., out of memory)
        if (env==nullptr || cmd==nullptr) return;

        gEnv = env;                            /// * capture JNI Environment
        forth_vm(cmd, [](int, const char *rst){
            /// send Forth response to Eforth.onNativeForth
            gEnv->CallVoidMethod(
                gForthObj, gForthPostID, gEnv->NewStringUTF(rst));
        });
        
        env->ReleaseStringUTFChars(js, cmd);   /// release js, cmd
    }
}
