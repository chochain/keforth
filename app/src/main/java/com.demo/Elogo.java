// ============================================================================
// LogoX.java - Processes Logo commands
package com.demo.logo;

import android.content.Context;
import android.view.View;
import com.demo.ui.OutputHandler;

public class Elogo {
    Logo2         logo;                                      ///< Logo implementation
    OutputHandler out;                                       ///< for debugging
    
    public Elogo(View vu, OutputHandler out) {
        this.logo = new Logo2(vu.getContext(), out);
        this.out  = out;
    }

    public void init(int w, int h) {                         ///< proxy to Logo2
        logo.reset(w, h);
    }

    public void process(String msg) {
        final String rx = "\\s+(?=(?:[^']*'[^']*')*[^']*$)"; ///< regex single quotes
        String[] ops = msg.split(rx);                        ///< parse parameters
        int      n   = ops.length;

        Runnable trace = () -> {                             ///< borrowed interface
            out.log(msg + " n=" + n);                        ///  to trace params
            for (int i = 0; i < n; i++) {
                out.log(" " + i + ":" + ops[i]);
            }
            out.log("\n");
        };
        trace.run();
        if (n < 1) return;

        String op = ops[0];
        String v1 = n > 1 ? ops[1] : "0";
        String v2 = n > 2 ? ops[2] : "0";

        logo.execute(op, v1, v2);
    }
}
