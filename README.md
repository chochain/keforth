# keForth - Forth in Kotlin

My entire carrier life was spent on enterprise backend systems. I've seen a lot of Java codes. Too much maybe. When Ruby came along, I was hoping that I never have to work on any Java again.

A few years ago, Dr. Ting found out that I knew Java and passed me his jeForth for help. We collaborated on it and finished his so called *Forth without Forth* in Java. He got excited, called it [ooeForth](https://github.com/chochain/ooeforth), and proudly presented to Silicon Valley Forth Interest Group in July 2021 meeting. After his passing 2022, I mothballed the project and never touched it again for 3+ years.

July 2025, I got a request (from Jeff Fox) fixing a bug. After that, I got a few free days and decided to spent some time refactor Dr. Ting's original single-file, ~400 lines packed, eForth source into modules. Maybe it's my age, this time, coding in Java was actually not bad at all or even pleasant at times.

I sort of appointed myself to carry the eForth lineage from Bill Muench and Dr. Ting. My [eforth](https://github.com/chochain/eforth) in 100% C++ is mature but, IMO, it will stay as a learning tool as was originally designed for. Facing the proliferation of Python, Rust, Go, ..., I don't see it is going anywhere. So, I took eForth to micro-controllers where it might still shines i.e. [eForth1](https://github.com/chochain/eForth1) and [nanoForth](https://github.com/chochain/nanoForth). These low-cost but tight MCUs, i.g. Arduino UNO/Nano, and ESP32, bought a lot of joy and fulfillment. However, they are mostly past-time.

I thought Java has faded quietly into the sunset. But, on one occasion, I saw a statistics on mobile phone market shares where Andriod still owns ~70% and suddenly dawned on me that Java is still alive and kicking in billions of devices. For $20, I can get one that has 64GB memory, 8-cores CPU at 2.0GHz, hi-res display, multiple cameras, WiFi, Bluetooth, GPS, G-seensor.... and the battery to run for a full day. My my my!

Then I bumped into Kotlin. So, here we go again!

## Build & Run
### Linux et al.
Setup Waydroid, the container-based Android emulator, with Termnux. I'm sure there are other ways but, for a newbie Android app person, it gets me a comfortable place to start with.

```Bash
    # install kotlinc (either via snap or apt install)
    
    $ git clone this_repo
    $ cd keforth
    
    $ kotlinc src/Eforth.kt src/eforth/*.kt -include-runtime -d tests/Eforth.jar
    $ java -jar tests/Eforth.jar
```
<img src="https://github.com/chochain/keforth/blob/main/docs/img/android_eforth_01.png" width="800" />

### Android Application
With limited hardware, this was an arduous learning experience for me. So, make sure your system is up to the task first. Install Android Studio or do what I've done following [command-line tools](https://stackoverflow.com/questions/32643297/how-to-make-an-android-app-without-using-android-studio).

```Bash
    $ git clone this_repo
    $ cd keforth

    $ gradlew build

    # install app/build/outputs/apk/debug/app-debug.apk onto your device (virtual or real) for testing

```
<img src="https://github.com/chochain/keforth/blob/main/docs/img/android_keforth_01.png" width="800" />

Note: Though I've never tried, it's said that one can get app signed by Google [in Studio](https://developer.android.com/studio/publish/app-signing) or [uber-signer](https://github.com/patrickfav/uber-apk-signer) for release.

With the mass market of Android, the rich features from all walks of life can be brought into keForth and interact with. For example, a Turtle Graphics here and many more to have fun with.
<img src="https://github.com/chochain/keforth/blob/main/docs/img/keforth_logo_01.png" width="800" />


## TODO
1. Review - Android
<pre>
    + Jetpack
        * Navigation
        * Paging
        * Slice
        * WorkManager
    + Sensors - SensorManager
        * Motion:      Accelerometer, Gravity, Linear Acceleration, Gyroscope, Rotation Vector (motion)
        * Position:    Orientation, Magnetic Field (direction)
        * Environment: Temperature, Pressure, Light, Proximity (close to ear), Humidity
    + Multimedia
        * Camera
        * Audio
        * Text-To-Speach
    + Communication
        * MQTT - [Mosquitto example](https://highvoltages.co/iot-internet-of-things/mqtt-broker-on-android/), [Paho example](https://medium.com/@basitmukhtar210/connecting-to-an-mqtt-server-on-android-using-eclipse-paho-client-6f5ee42c7191)
        * Bluetooth - [EasyBle](https://github.com/Ficat/EasyBle)
        * USB - [UsbSerial](https://github.com/felHR85/UsbSerial)
        * Wifi - service, P2P
        * WireGuard - [Client Setup](https://serversideup.net/blog/how-to-configure-a-wireguard-android-vpn-client/)
        * Telephony
    + Object
        * instantiate from string
        * invokedynamic (instead of reflection).
          [First Taste](https://blog.headius.com/2008/09/first-taste-of-invokedynamic.html),
          [Secret](https://www.infoq.com/articles/Invokedynamic-Javas-secret-weapon/)
          [Indy](https://stackoverflow.com/questions/6638735/whats-invokedynamic-and-how-do-i-use-it)
        * lookup by hashcode (kept in map)
    + AI
        * [MediaPipe Guestures](https://github.com/google-ai-edge/mediapipe-samples/tree/main/examples/gesture_recognizer/android)
</pre>

2. Review - Comparables
<pre>
   + JNI - [Example](https://medium.com/@sarafanshul/jni-101-introduction-to-java-native-interface-8a1256ca4d8e)
   + Robot
      * [OpenBot](https://github.com/ob-f/OpenBot/blob/master/android/robot/src/main/java/org/openbot/OpenBotApplication.java)
      * [Smartphone Robot](https://www.voltpaperscissors.com/diy-smartphone-robot)
      * [MIT App Inventor](https://github.com/mit-cml/appinventor-sources)
   + Cordova - Google's cross-platform WebView
   + FireBase - Cloud Messaging, Chat API (for remote access)
   + DroidScript - Javascript + Python
   + Roboto - JRuby + React
   + ButterKnife => ViewBinding, DataBinding vs Jetpack Compose
   + AndroidClock => Forth-base solution
</pre>

3. Android Project - Jetpack Compose + Material Design
<pre>
    + Backdrop => layered
       + Cards => keforth responses
       + Divider
    + Floating Action Button
       + Chips << cmd input
    + Side Sheets => words (sorted), ss_dump
    + Tooltip => see
</pre>

## References

    - [Su keforth](https://lastnames.myheritage.tw/last-name/su_keforth)
    - [Emulator without Android Studio](https://dev.to/shivams136/run-android-emulator-without-installing-android-studio-3ji)
    - [Waydroid](https://ubuntuhandbook.org/index.php/2023/12/waydroid-run-android-apps-ubuntu/)
    - Bliss OS ... 202506 pending next release
    - [kforth](https://github.com/dblsaiko/kforth), a more classic approach
    - [exercism - Community Solutions](https://exercism.org/tracks/kotlin/exercises/forth/solutions), over a hundred "new kids" tried this, though missing out big time without definding words
