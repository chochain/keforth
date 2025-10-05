## Install to Android device
adb install app/build/outputs/apk/debug/app-debug.apk
adb push tests/logo.fs /storage/emulated/0/Download/

## Install to Waydroid
adb install app/build/outputs/apk/debug/app-debug.apk
sudo cp tests/logo.fs ~/.local/share/waydroid/data/media/0/Download
