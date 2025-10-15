package com.keforth.eforth;

public interface NativeCallback {
    void onNativeEvent(String msg);
    void onNativeError(String err);
}
