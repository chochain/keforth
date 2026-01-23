///
/// @file
/// @brief - handles messages to/from Forth VM thread
///
package com.gnii.keforth;

import java.lang.Thread;
import java.util.Objects;

import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.gnii.keforth.JavaCallback.PostType;
import com.gnii.keforth.eforth.*;

public class Eforth extends Thread {
    public static final int USE_JNI_FORTH = 1;
    public static final int MSG_TYPE_STR  = 1;
    
    private native void jniInit();
    private native void jniOuter(String cmd);
    private native void jniTeardown();

    private static Handler      hndl;
    private static IO           io;
    private static VM           vm;

    private final JavaCallback  main;
    private final Esystem       sys;
    private final String        name;
    private final boolean       busy = false;

    public Eforth(JavaCallback main, Esystem sys, String app_name) {
        this.main = main;
        this.sys  = sys;
        this.name = app_name;

        if (USE_JNI_FORTH != 0) jniInit();       /// * call JNI eForth constructor
    }

    @Override
    public void run() {
        if (USE_JNI_FORTH == 0) {
            io = new IO(name, System.in, sys.out);   /// * create IO handlers
            vm = new VM(io, main);                   /// * create Forth VM instance
            io.mstat();                              /// * display memory usage
        }
        Looper.prepare();                        /// * create thread MessageQueue
        hndl = new Handler(Objects.requireNonNull(Looper.myLooper())) {
            @Override public void handleMessage(@NonNull Message msg) {
                if (msg.what != MSG_TYPE_STR) return;
                String cmd = (String) msg.obj;   /// * handle Forth command
                if (!cmd.isEmpty()) {            /// * echo on UI thread
                    sys.out.log(cmd+"\n");
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

    public void process(String cmd) {            ///< Forth VM 
        Message msg = Message.obtain();
        msg.what = MSG_TYPE_STR;
        msg.obj  = cmd;
        hndl.sendMessage(msg);                   /// * send command to MessageQueue
    }

    public void onNativeForthFeedback(String rst) {
//        main.onPost(PostType.PRINT, rst);
        sys.out.print(rst);                      /// * update UI directly
    }

    public void onNativeJavaCmd(String cmd) {    /// * proxy to main (UI) thread
        main.onPost(PostType.JAVA, cmd);
    }
}
