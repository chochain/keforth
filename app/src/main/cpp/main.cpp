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

jobject   gForthObj    = nullptr;            ///< Eforth Activity object
jmethodID gForthPostID = nullptr;            ///< onNativeForth(rst)

void android_main(struct android_app *app) {
    sensor_engine_start(app);
}

extern "C"
{
    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniInit(JNIEnv *env, jobject thiz) {
        if (gForthObj != nullptr) env->DeleteGlobalRef(gForthObj);
        gForthObj = env->NewGlobalRef(thiz);
        jclass cb = env->GetObjectClass(gForthObj);

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
        static JNIEnv gEnv;
        // Convert the Java string to a C-style string
        const char *cmd = env->GetStringUTFChars(js, nullptr);

        // Check for null if the string conversion fails (e.g., out of memory)
        if (env==nullptr || cmd==nullptr) return;

        gEnv = env;
        forth_vm(cmd, [](int, const char *rst){
            /// send Forth response to Eforth.onNativeForth
            gEnv->CallVoidMethod(
                        gForthObj, gForthPostID, gEnv->NewStringUTF(rst));
        });
        env->ReleaseStringUTFChars(js, cmd);           /// release js, cmd
    }
}
