#include <android/sensor.h>
#include <android/looper.h>
#include "android_native_app_glue.h"         /// For android_app struct

// Define a unique ID for your looper events
#define LOOPER_ID_USER 3
#define TRIGGER_PERIOD 1000000               /// 1 second in nanoseconds

// Structure to hold application state and sensor-related objects
struct Engine {
    struct android_app  *app;
    const ASensor       *sensor[4];
    ASensorManager      *mgr;
    ASensorEventQueue   *que;
};

// Callback function to handle sensor events
int32_t handleSensorEvent(int fd, int events, void* data) {
    Engine &eng = *(Engine*)data;
    ASensorEvent ev;

    // Process all pending events in the queue
    while (ASensorEventQueue_getEvents(eng.que, &ev, 1) > 0) {
        switch (ev.type) {
            case ASENSOR_TYPE_ACCELEROMETER:       break;
            case ASENSOR_TYPE_AMBIENT_TEMPERATURE: break;
            case ASENSOR_TYPE_PRESSURE:            break;
            case ASENSOR_TYPE_RELATIVE_HUMIDITY:   break;
            default: /* do nothing */ break;
        }
    }
    return 1; // Continue receiving callbacks
}

// Initialize sensors
void initSensors(Engine &eng) {
    eng.mgr = ASensorManager_getInstance();
	eng.que = ASensorManager_createEventQueue(
		eng.mgr,
		eng.app->looper,            /// Use the application's main looper
		LOOPER_ID_USER,         /// A unique ID for your looper event source
		handleSensorEvent,  /// Your callback function
		eng.app->userData     /// User data passed to the callback
        );

    eng.sensor[0] = ASensorManager_getDefaultSensor(eng.mgr, ASENSOR_TYPE_ACCELEROMETER);
    eng.sensor[1] = ASensorManager_getDefaultSensor(eng.mgr, ASENSOR_TYPE_AMBIENT_TEMPERATURE);
    eng.sensor[2] = ASensorManager_getDefaultSensor(eng.mgr, ASENSOR_TYPE_PRESSURE);
    eng.sensor[3] = ASensorManager_getDefaultSensor(eng.mgr, ASENSOR_TYPE_RELATIVE_HUMIDITY);

    for (const ASensor *s : eng.sensor) {
        if (s == nullptr) continue;
        ASensorEventQueue_enableSensor(eng.que, s);
        ASensorEventQueue_setEventRate(eng.que, s, TRIGGER_PERIOD);  // 1Hz
    }
}

// Teardown sensors
void teardownSensors(Engine &eng) {
    for (const ASensor *s : eng.sensor) {
        if (s == nullptr) continue;
        ASensorEventQueue_disableSensor(eng.que, s);
    }
    if (eng.que) {
        ASensorManager_destroyEventQueue(eng.mgr, eng.que);
    }
}

// Example usage within android_main (from android_native_app_glue)
void android_main(struct android_app *app) {
    auto looper = ALooper_forThread();   /// get thread looper
    if (looper == NULL) {
        looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    }
    Engine eng;
    app->userData = &eng;
    eng.app = app;

    initSensors(eng);

    // Main application loop
    while (true) {
        void *data;
        int  ev;
        int  id = ALooper_pollOnce(               /// sleep till event arrives
                TRIGGER_PERIOD>>1, NULL, &ev, (void**)&data);

        if (id == LOOPER_ID_USER && data != NULL) {
            // This case handles your sensor events, as the callback is registered with this ID.
            // The handleSensorEvent function will be called directly by the ALooper.
        }
        // Handle other application events (input, lifecycle, etc.) here if needed
    }
    teardownSensors(eng);
}
