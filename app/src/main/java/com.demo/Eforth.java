// ============================================================================
// ForthProcessor.java - Handles Forth VM operations
package com.demo.forth;

import com.demo.eforth.*;
import com.demo.ui.OutputHandler;

public class Eforth {
    private final String        name;
    private final OutputHandler out;
    private final JavaCallback  cb;
    
    private IO io;
    private VM vm;
    
    public Eforth(String name, OutputHandler out, JavaCallback cb) {
        this.name  = name;
        this.out   = out;
        this.cb    = cb;
    }
    
    public void init() {
        io = new IO(name, System.in, out);
        vm = new VM(io, cb);
        io.mstat();
    }
    
    public void outer(String cmd) {
        out.log(cmd);
        io.rescan(cmd);
        
        while (io.readline()) {
            if (!vm.outer()) break;
        }
    }
}

