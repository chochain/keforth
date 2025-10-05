///
/// @file
/// @brief - Manages output display and colors
///
package com.demo.ui;

import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.app.Activity;

import com.demo.R;

public class OutputHandler extends OutputStream {
    private final TextView                out;
    private final Theme                   theme;
    
    public OutputHandler(Activity act, int view_id, int color_cmd) {
        out   = act.findViewById(view_id);
        theme = new Theme(act, color_cmd);
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
    
    public void print(String txt) { post(txt, theme.fg); }
    public void log(String cmd)   { post(cmd, theme.cm); }
    public void debug(String msg) { post(msg, theme.cb); }
    
    private void post(String str, int color) {
        SpannableString s = new SpannableString(str);
        s.setSpan(new ForegroundColorSpan(color),
                  0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.post(() -> out.append(s));                 /// * enqueue Main.Looper
    }
    
    private static class Theme {
        public final int fg;
        public final int cm;
        public final int cb;
        
        public Theme(Activity act, int color_id) {
            fg = Color.WHITE;
            cm = act.getResources().getColor(color_id);
            cb = Color.RED;
        }
    }
}
