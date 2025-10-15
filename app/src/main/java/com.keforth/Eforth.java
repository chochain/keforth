///
/// @file
/// @brief - handles messages to/from Forth VM thread
///
package com.keforth;

import java.lang.Thread;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.keforth.ui.OutputHandler;
import com.keforth.eforth.*;

public class Eforth extends Thread implements JavaCallback {
    public static final int POST_TGT_MAIN   = 0;
    public static final int POST_TGT_EFORTH = 1;
    public static final int MSG_TYPE_STR    = 1;
    private native void forthInit();
    private native void processJNI(String cmd);
    private native void forthTeardown();

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

        forthInit();
    }

    public void run0() {
        io   = new IO(name, System.in, out);
        vm   = new VM(io, api);
        io.mstat();
        
        Looper.prepare();
        hndl = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
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

    @Override
    public void run() {
        Looper.prepare();
        hndl = new Handler() {
            @Override public void handleMessage(@NonNull Message msg) {
                if (msg.what != MSG_TYPE_STR) return;
                String cmd = (String)msg.obj;
                out.log(cmd + "\n");
                processJNI(cmd);
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

    @Override
    public void onPost(int tid, String rst) {
 //       if (tid==Eforth.POST_TGT_EFORTH) {
        out.print(rst);
 //       }
 //       else api.onPost(tid, rst);
    }
}

