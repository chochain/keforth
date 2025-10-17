///
/// @file
/// @brief eForth main program for testing on Desktop PC (Linux and Cygwin)
///
#include <iostream>      // cin, cout
#include <fstream>       // ifstream
#include <cstdint>
#include <sys/sysinfo.h>
#include <jni.h>

using namespace std;

extern void forth_init();
extern int  forth_vm(const char *cmd, void(*)(int, const char*)=NULL);
extern void forth_teardown();

const char* APP_VERSION = "keForth v1.0";
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

static jobject   gE4Obj    = nullptr;
static jmethodID gE4PostID = nullptr;

void android_main(struct android_app *app) {
    //sensor_main(app);
    //forth_init();
}

extern "C"
{
    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_forthInit(JNIEnv *env, jobject thiz) {
        forth_init();

        if (gE4Obj != nullptr) env->DeleteGlobalRef(gE4Obj);
        gE4Obj = env->NewGlobalRef(thiz);

        jclass cb = env->GetObjectClass(gE4Obj);
        gE4PostID = env->GetMethodID(cb, "onPost", "(ILjava/lang/String;)V");
        env->DeleteLocalRef(cb); // Clean up local reference
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_forthTeardown(JNIEnv *env, jobject thiz) {
        forth_teardown();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_processJNI(JNIEnv *env, jobject thiz, jstring js) {
        static JNIEnv *gEnv = nullptr;

        // Convert the Java string to a C-style string
        const char *cmd = env->GetStringUTFChars(js, nullptr);

        // Check for null if the string conversion fails (e.g., out of memory)
        gEnv = env;
        if (env==nullptr || cmd==nullptr) return;

        forth_vm(cmd, [](int, const char *rst){
            /// send Forth response to Eforth.onPost
            gEnv->CallVoidMethod(
                        gE4Obj, gE4PostID, 1, gEnv->NewStringUTF(rst));
        });
        env->ReleaseStringUTFChars(js, cmd);           /// release js, cmd
    }
}
