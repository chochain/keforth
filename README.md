# Forth - in Kotlin

My entire carrier life was spent on enterprise backend systems. I've seen a lot of Java codes. Too much maybe. When Ruby came along, I was hoping that I never have to work on any Java again.

A few years ago, Dr. Ting found out that I knew Java and passed me his jeForth for help. We collaborated on it and finished his so called *Forth without Forth* in Java. He got excited, called it [ooeForth](https://github.com/chochain/ooeforth), and proudly presented to Silicon Valley Forth Interest Group in July 2021 meeting. After his passing 2022, I mothballed the project and never touched it again for 3+ years.

July 2025, I got a request (from Jeff Fox) fixing a bug. After that, I got a few free days and decided to spent some time refactor Dr. Ting's original single-file, ~400 lines packed, eForth source into modules. Maybe it's my age, this time, coding in Java was actually not bad at all or even pleasant at times.

I sort of appointed myself to carry the eForth lineage from Bill Munich and Dr. Ting. My [eforth](https://github.com/chochain/eforth) in 100% C++ is mature but, IMO, it will stay as a learning tool as was originally designed for. Facing the proliferation of Python, Rust, Go, ..., I don't see it is going anywhere. So, I took eForth to micro-controllers where it might still shines i.e. [eForth1](https://github.com/chochain/eForth1) and [nanoForth](https://github.com/chochain/nanoForth). These low-cost but tight MCUs, i.g. Arduino UNO/Nano, and ESP32, bought a lot of joy and fulfillment. However, they are mostly past-time.

I thought Java has faded quietly into the sunset. But, on one occasion, I saw a statistics on mobile phone market shares where Andriod still owns ~70% and suddenly dawned on me that Java is still alive and kicking in billions of devices. For $20, I can get one that has 64GB memory, 8-cores CPU at 2.0GHz, hi-res display, multiple cameras, WiFi, Bluetooth, GPS, G-seensor.... and the battery to run for a full day. My my my!

Then I bumped into Kotlin. So, here we go again!
