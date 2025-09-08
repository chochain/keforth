// ============================================================================
// LogoX.java - Processes Logo commands
package com.demo.logo;

import android.content.Context;
import android.view.View;
import com.demo.ui.OutputHandler;

public class Elogo {
    Logo2         logo;
    OutputHandler out;
    
    public Elogo(View vu, OutputHandler out) {
        this.out = out;
        
        logo = new Logo2(vu.getContext());
        ///
        ///> resize Logo panel only after layed out (see View life-cycle)
        ///
        vu.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                int l1, int t1, int r1, int b1,               ///< new layout
                int l0, int t0, int r0, int b0) {             ///< orig layout (0,0,0,0)
                v.removeOnLayoutChangeListener(this);         ///< once, fixed size
                out.debug("logo w="+v.getWidth()+" h="+v.getHeight()+"\n");
//                logo.reset(v.getWidth(), v.getHeight());
            }
        });
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

        out.debug("before " + logo.to_s() + "\n");
        logo.execute(op, v1, v2);
        out.debug("after  " + logo.to_s() + "\n");
    }
}
