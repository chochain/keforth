// ============================================================================
// ForthProcessor.java - Handles Forth VM operations
package com.demo.forth;

import com.demo.eforth.*;
import com.demo.ui.OutputHandler;

public class Eforth {
    private final String        name;
    private final OutputHandler output;
    private final JavaCallback  callback;
    
    private IO io;
    private VM vm;
    
    public Eforth(String name, OutputHandler output, JavaCallback callback) {
        this.name     = name;
        this.output   = output;
        this.callback = callback;
    }
    
    public void init() {
        io = new IO(name, System.in, output.getScroll());
        vm = new VM(io, callback);
        io.mstat();
    }
    
    public void outer(String cmd) {
        output.showCommand(cmd);
        io.rescan(cmd);
        
        while (io.readline()) {
            if (!vm.outer()) break;
        }
    }
}

