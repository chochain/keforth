// ============================================================================
// ForthOutputHandler.java - Manages output display and colors
package com.demo.ui;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.R;
import java.io.IOException;
import java.io.OutputStream;

public class OutputHandler extends OutputStream {
    private final Scheme   scheme;
    private final TextView out;
    
    public OutputHandler(AppCompatActivity act) {
        scheme = new Scheme(act);
        out    = act.findViewById(R.id.forthOutput);
        
    }
    
    @Override
    public void write(int n) throws IOException {
        byte[] b = { (byte)n };
        write(b, 0, b.length);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String rst = new String(b, off, len);
        print(rst);
    }
    
    public void print(String txt) { show(txt, scheme.fg);        }
    public void log(String cmd)   { show(cmd + "\n", scheme.cm); }
    public void debug(String msg) { show(msg, scheme.cb);        }
    
    private void show(String str, int color) {
        SpannableString s = new SpannableString(str);
        s.setSpan(new ForegroundColorSpan(color),
                  0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.post(() -> out.append(s));
//            sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));        ///> CC: this does not work
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
