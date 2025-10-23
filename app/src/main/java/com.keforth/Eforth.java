///
/// @file
/// @brief - handles messages to/from Forth VM thread
///
package com.keforth;

import java.lang.Thread;
import java.util.Objects;

import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.keforth.eforth.*;
import com.keforth.ui.OutputHandler;

public class Eforth extends Thread implements JavaCallback {
    public static final int USE_JNI_FORTH   = 1;
    public static final int MSG_TYPE_STR    = 1;
    public static final int MSG_TYPE_TIMER  = 2;
    private native void jniInit();
    private native void jniOuter(String cmd);
    private native void jniTeardown();

    private static Handler      hndl;
    private static IO           io;
    private static VM           vm;

    private final String        name;
    private final OutputHandler out;
    private final JavaCallback  api;
    private int   timer;                         /// * timer interrupt period, in ms
    
    public Eforth(String name, OutputHandler out, JavaCallback api) {
        this.name  = name;
        this.out   = out;
        this.api   = api;
        this.timer = 1000;

        if (USE_JNI_FORTH != 0) jniInit();       /// * call JNI eForth constructor
    }

    @Override
    public void run() {
        if (USE_JNI_FORTH == 0) {
            io = new IO(name, System.in, out);   /// * create IO handlers
            vm = new VM(io, api);                /// * create Forth VM instance
            io.mstat();                          /// * display memory usage
        }
        Looper.prepare();                        /// * create thread MessageQueue
        hndl = new Handler(Objects.requireNonNull(Looper.myLooper())) {
            @Override public void handleMessage(@NonNull Message msg) {
                if (msg.what != MSG_TYPE_STR) return;
                String cmd = (String) msg.obj;   /// * handle Forth command
                if (!cmd.equals("tick")) {
                    onPost(PostType.LOG, cmd);   /// * echo
                }
                if (USE_JNI_FORTH == 0) {
                    io.rescan(cmd);              /// * update input stream
                    while (io.readline()) {      /// * fetch line-by-line
                        if (!vm.outer()) break;  /// * call Java Forth outer interpreter
                    }
                } else jniOuter(cmd);            /// * call JNI Forth outer interpreter
            }
        };
        Looper.loop();
    }

    public void process(String cmd) {
        Message msg = Message.obtain();
        msg.what = MSG_TYPE_STR;
        msg.obj  = cmd;
        hndl.sendMessage(msg);                  /// * send command to MessageQueue
    }

    public void onNativeTimer(int enable) {
        api.onPost(PostType.TIMER, enable!=0 ? "start" : "stop");
    }

    public void onNativeForth(String rst) {
        api.onPost(PostType.FORTH, rst);
    }

    @Override
    public void onPost(PostType tid, String rst) {  /// * proxy to main (UI) thread
        api.onPost(tid, rst);
    }
}
