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
#if 1 || DO_SENSOR
void sensor_engine_start(struct android_app *app);
void sensor_enable(int type_id, int peroid);
void sensor_disable(int type_id);
#else
void sensor_engine_start(struct android_app *app) {}
#endif // 1 || DO_SENSOR

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

static jobject   gForthObj     = nullptr;
static jmethodID gForthPostID  = nullptr;
static jmethodID gSensorPostID = nullptr;

void eforth_main(struct android_app *app) {
    // forth_init();
}

void android_main(struct android_app *app) {
    eforth_main(app);
    sensor_engine_start(app);
}

extern "C"
{
    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniInit(JNIEnv *env, jobject thiz) {
        if (gForthObj != nullptr) env->DeleteGlobalRef(gForthObj);
        gForthObj = env->NewGlobalRef(thiz);
        jclass cb = env->GetObjectClass(gForthObj);

        gForthPostID  = env->GetMethodID(cb, "jniPost", "(Ljava/lang/String;)V");
        gSensorPostID = env->GetMethodID(cb, "jniSensor", "([I)V");

        env->DeleteLocalRef(cb);

        forth_init();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniTeardown(JNIEnv *env, jobject thiz) {
        forth_teardown();
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniOuter(JNIEnv *env, jobject thiz, jstring js) {
        static JNIEnv *gEnv = nullptr;

        // Convert the Java string to a C-style string
        const char *cmd = env->GetStringUTFChars(js, nullptr);

        // Check for null if the string conversion fails (e.g., out of memory)
        gEnv = env;
        if (env==nullptr || cmd==nullptr) return;

        forth_vm(cmd, [](int, const char *rst){
            /// send Forth response to Eforth.onPost
            gEnv->CallVoidMethod(
                        gForthObj, gForthPostID, gEnv->NewStringUTF(rst));
        });
        env->ReleaseStringUTFChars(js, cmd);           /// release js, cmd
    }
}
