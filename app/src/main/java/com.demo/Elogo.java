///
/// @file
/// @brief - Logo command processor
///
package com.demo.logo;

import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import com.demo.ui.OutputHandler;

public class Elogo {                                        
    final private Logo1 logo;                                ///< Logo proxy
    
    public Elogo(ViewGroup vgrp, OutputHandler out) {
//        this.logo = new Logo2(vgrp.getContext(), out);
        this.logo = new Logo1(vgrp.getContext());
        
        LayoutParams p = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        );
        logo.setLayoutParams(p);
        
        vgrp.addView(logo);                                  ///< dynamic view
    }

    public String to_s() {
        return logo.to_s();
    }

    public void process(String msg) {
        final String rx = "\\s+(?=(?:[^']*'[^']*')*[^']*$)"; ///< regex single quotes
        String[] ops = msg.split(rx);                        ///< parse parameters
        int      n   = ops.length;

        if (n < 1) return;

        String op = ops[0];
        String v1 = n > 1 ? ops[1] : "0";
        String v2 = n > 2 ? ops[2] : "0";

        logo.execute(op, v1, v2);
    }
}
