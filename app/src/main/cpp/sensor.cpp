#include <queue>
#include <map>

#include <android/sensor.h>
#include <android/looper.h>
#include "android_native_app_glue.h"         /// For android_app struct

/// Define a unique ID for your looper events
#define LOOPER_ID_USER  4
#define TRIGGER_PERIOD  1000000               /// 1 second in nanoseconds

// Structure to hold application state and sensor-related objects
struct SensorEngine {
    struct android_app            *app;
    ASensorManager                *mgr;
    ASensorEventQueue             *que;
    std::map<int, const ASensor*> sensor;
    std::queue<int>               *fque;
};

/// Callback function to handle sensor events
int _sensor_event_handler(int fd, int events, void *data) {
    SensorEngine &eng = *(SensorEngine *)data;
    ASensorEvent ev;

    /// Process all pending events in the queue
    while (ASensorEventQueue_getEvents(eng.que, &ev, 1) > 0) {
        int v = ((int) (ev.data[0] * 100.0) << 8) | (int) ev.type;
        eng.fque->push(v);
    }
    return 1; /// Continue receiving callbacks
}

void _sensor_setup(SensorEngine &eng) {
    eng.mgr = ASensorManager_getInstance();
    eng.que = ASensorManager_createEventQueue(
            eng.mgr,
            eng.app->looper,        /// Use the application's main looper
            LOOPER_ID_USER,         /// A unique ID for your looper event source
            _sensor_event_handler,  /// Your callback function
            eng.app->userData       /// User data passed to the callback
    );
}

void _sensor_teardown(SensorEngine &eng) {
    for (auto kv: eng.sensor) {
        ASensorEventQueue_disableSensor(eng.que, kv.second);
    }
    if (eng.que) {
        ASensorManager_destroyEventQueue(eng.mgr, eng.que);
    }
}

SensorEngine gEng;
std::queue<int> fque;

void sensor_engine_start(struct android_app *app) {
    auto looper = ALooper_forThread();   /// get thread looper
    if (looper == NULL) {
        looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    }
    gEng.app  = app;
    gEng.fque = &fque;
    gEng.app->userData = &gEng;

    _sensor_setup(gEng);

    // Main application loop
    while (true) {
        void *data;
        int ev;
        int id = ALooper_pollOnce(               /// sleep till event arrives
                TRIGGER_PERIOD >> 1, NULL, &ev, (void **) &data);

        if (id == LOOPER_ID_USER && data != NULL) {
            // This case handles your sensor events, as the callback is registered with this ID.
            // The handleSensorEvent function will be called directly by the ALooper.
        }
        // Handle other application events (input, lifecycle, etc.) here if needed
    }
    ALooper_release(looper);

    _sensor_teardown(gEng);
}

void sensor_enable(int type_id, int period) {
    const ASensor *s = gEng.sensor[type_id];
    if (s == nullptr) {
        s = gEng.sensor[type_id] =
            ASensorManager_getDefaultSensor(gEng.mgr, type_id);
    }
    ASensorEventQueue_enableSensor(gEng.que, s);
    ASensorEventQueue_setEventRate(gEng.que, s, period);
}

void sensor_disable(int type_id) {
    ASensorEventQueue_disableSensor(gEng.que, gEng.sensor[type_id]);
}
