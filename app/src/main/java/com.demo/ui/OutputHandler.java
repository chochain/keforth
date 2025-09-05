// ============================================================================
// ForthOutputHandler.java - Manages output display and colors
package com.demo.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.R;
import java.io.IOException;
import java.io.OutputStream;

public class OutputHandler {
    private final Scheme scheme;
    private final Scroll scroll;
    
    public OutputHandler(AppCompatActivity act) {
        scheme = new Scheme(act);
        
        ScrollView sv = act.findViewById(R.id.forthView);
        sv.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    sv.post(new Runnable() {
                            @Override
                            public void run() {
                                sv.fullScroll(ScrollView.FOCUS_DOWN);
//                                in.requestFocus();
                            }
                        });
                }
            });
        
        TextView out = act.findViewById(R.id.forthOutput);
        this.scroll = new Scroll(out, scheme.fg);
    }
    
    public void showCommand(String cmd) {
        scroll.show(cmd + "\n", scheme.cm);
    }
    
    public void showCallback(String msg) {
        scroll.show(msg, scheme.cb);
    }
    
    public void showOutput(String output) {
        scroll.show(output, scheme.fg);
    }
    
    public OutputStream getScroll() {
        return scroll;
    }
    
    private static class Scheme {
        public final int fg;
        public final int cm;
        public final int cb;
        
        public Scheme(AppCompatActivity act) {
            fg = Color.WHITE;
            cm = act.getResources().getColor(R.color.teal_200);
            cb = Color.RED;
        }
    }
}
