///
/// @file
/// @brief - handles messages to/from Forth VM thread
///
package com.demo.eforth;

import java.lang.Thread;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import com.demo.ui.OutputHandler;

public class Eforth extends Thread {
    public static final int MSG_TYPE_STR = 1;
    
    private final String        name;
    private final OutputHandler out;
    private final JavaCallback  api;
    
    private Handler hndl;
    private IO      io;
    private VM      vm;
    
    public Eforth(String name, OutputHandler out, JavaCallback api) {
        this.name = name;
        this.out  = out;
        this.api  = api;
    }

    @Override
    public void run() {
        io   = new IO(name, System.in, out);
        vm   = new VM(io, api);
        io.mstat();
        
        Looper.prepare();
        hndl = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what != MSG_TYPE_STR) return;
                String cmd = (String)msg.obj;
                out.log(cmd + "\n");
                io.rescan(cmd);
                 
                while (io.readline()) {
                    if (!vm.outer()) break;
                }
            }
        };
        Looper.loop();
    }

    public void process(String cmd) {
        Message msg = Message.obtain();
        msg.what = MSG_TYPE_STR;
        msg.obj  = cmd;
        hndl.sendMessage(msg);
    }
}

