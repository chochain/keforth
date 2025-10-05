///
/// @file
/// @brief - Logo command processor
///
package com.keforth.logo;

import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

public class Elogo {                                        
    final private Logo2 logo;                                ///< Logo proxy
    
    public Elogo(ViewGroup vgrp) {
        this.logo = new Logo2(vgrp.getContext());
//        this.logo = new Logo1(vgrp.getContext());
        
        LayoutParams p = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        );
        logo.setLayoutParams(p);
        
        vgrp.addView(logo);                                  ///< dynamic view
    }

    public String status() {
        return logo.to_s() + " n="+logo.nx;
    }

    public void process(String[] ops) {
        int    n  = ops.length;
        if (n < 2) return;                                   ///* skip no-op
        
        String op = ops[1];
        String v1 = n > 2 ? ops[2] : "0";
        String v2 = n > 3 ? ops[3] : "0";

        logo.execute(op, v1, v2);
    }
}
