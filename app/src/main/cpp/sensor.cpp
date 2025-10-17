#include <android/sensor.h>
#include <android/looper.h>
#include "android_native_app_glue.h" // For android_app struct

// Define a unique ID for your looper events
#define LOOPER_ID_USER 3

// Structure to hold application state and sensor-related objects
struct Engine {
    struct android_app  *app;
    const ASensor       *sensor;
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
            case ASENSOR_TYPE_ACCELEROMETER:
                // Handle accelerometer data
                // ev.acceleration.x, ev.acceleration.y, ev.acceleration.z
                // are available here
                break;
            // Add cases for other sensor types as needed
        }
    }
    return 1; // Continue receiving callbacks
}

// Initialize sensors
void initSensors(Engine &eng) {
    eng.mgr    = ASensorManager_getInstance();
    eng.sensor = ASensorManager_getDefaultSensor(eng.mgr, ASENSOR_TYPE_ACCELEROMETER);

    if (!eng.sensor) return;
	
	eng.que = ASensorManager_createEventQueue(
		eng.mgr,
		eng.app->looper,  // Use the application's main looper
		LOOPER_ID_USER,    // A unique ID for your looper event source
		handleSensorEvent, // Your callback function
		eng.app->userData     // User data passed to the callback
        );

	ASensorEventQueue_enableSensor(eng.que, eng.sensor);
	// Set desired sensor event rate (e.g., 10000 microseconds for 100Hz)
	ASensorEventQueue_setEventRate(eng.que, eng.sensor, 10000);
}

// Teardown sensors
void teardownSensors(Engine &eng) {
    if (eng.sensor) {
        ASensorEventQueue_disableSensor(eng.que, eng.sensor);
    }
    if (eng.que) {
        ASensorManager_destroyEventQueue(eng.mgr, eng.que);
    }
}

// Example usage within android_main (from android_native_app_glue)
void android_main(struct android_app *app) {
    Engine eng;
    app->userData = &eng;
    eng.app = app;

    initSensors(eng);

    // Main application loop
    int ident;
    int events;
    struct android_poll_source* src;

    while (true) {
        ident = ALooper_pollOnce(-1, NULL, &events, (void**)&src);

        if (ident == LOOPER_ID_USER && src != NULL) {
            // This case handles your sensor events, as the callback is registered with this ID.
            // The handleSensorEvent function will be called directly by the ALooper.
        }
        // Handle other application events (input, lifecycle, etc.) here if needed
    }
    teardownSensors(eng);
}
