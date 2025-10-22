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

extern void ISR_CALL(void *pvm, int word_id);

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

JNIEnv    *gEnv        = nullptr;            ///< JNI environment
jobject   gForthObj    = nullptr;            ///< Eforth Activity object
jmethodID gTimerPostID = nullptr;            ///< onNativeTimer(enable)
jmethodID gForthPostID = nullptr;            ///< onNativeForth(rst)

int       gPeriod      = 1000;               ///< default timer period (in ms)

std::map<int, std::pair<int, int>> gISR;     /// timer ISR map w -> <cnt, max>

void android_main(struct android_app *app) {
    sensor_engine_start(app);
}

void timer_enable(int enable) {
    if (gEnv == nullptr) return;
    gEnv->CallVoidMethod(gForthObj, gTimerPostID, enable);
}

void tmisr_set(int word_id, int period) {
    if (period == 0) {
        gISR.erase(word_id);
        return;
    }
    int ntic = 1 + (period > gPeriod ? period / gPeriod : 0);
    if (gISR.find(word_id) == gISR.end()) { /// new entry, TODO: thread-safe?
        gISR[word_id] = std::pair<int, int>(0, ntic);
    } else gISR[word_id].second = ntic;
}

void tmisr_service(void *vm) {
    for (auto kv : gISR) {
        auto &v = kv.second;
        if (v.first >= v.second) {
            ISR_CALL(vm, kv.first);
            v.first = 0;
        }
    }
}

extern "C"
{
    JNIEXPORT void JNICALL
    Java_com_keforth_MainActivity_jniTick(JNIEnv *env, jobject thiz) {
        for (auto kv : gISR) {
            kv.second.first += 1;            /// increment counter, TODO: thread-safe?
        }
    }

    JNIEXPORT void JNICALL
    Java_com_keforth_Eforth_jniInit(JNIEnv *env, jobject thiz) {
        if (gForthObj != nullptr) env->DeleteGlobalRef(gForthObj);
        gForthObj = env->NewGlobalRef(thiz);
        jclass cb = env->GetObjectClass(gForthObj);

        gTimerPostID = env->GetMethodID(cb, "onNativeTimer", "(I)V");
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
