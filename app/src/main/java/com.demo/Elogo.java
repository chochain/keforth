// ============================================================================
// LogoX.java - Processes Logo commands
package com.demo.logo;

import com.demo.ui.OutputHandler;

public class Elogo {
    public static void process(String msg, Logo1 logo, OutputHandler out) {
        final String rx = "\\s+(?=(?:[^']*'[^']*')*[^']*$)"; ///< regex single quotes
        String[] ops = msg.split(rx);                        ///< parse parameters
        int      n   = ops.length;
        
        // Debug output
        out.log(msg + " n=" + n);
        for (int i = 0; i < n; i++) {
            out.log(" " + i + ":" + ops[i]);
        }
        out.log("\n");
        
        if (n < 1) return;

        String op = ops[0];
        String v1 = n > 1 ? ops[1] : "0";
        String v2 = n > 2 ? ops[2] : "0";
            
        out.debug("before " + logo.to_s() + "\n");
        logo.update(op, v1, v2);
        out.debug("after  " + logo.to_s() + "\n");
    }
}
