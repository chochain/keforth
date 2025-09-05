package com.demo.ui;

import android.widget.TextView;
import android.text.SpannableStringBuilder;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;

public class Scroll extends OutputStream {
    private final TextView vu;
    private final int      fg;

    Scroll(TextView vu, int color) {
        this.vu = vu;
        this.fg = color;
    }

    public void show(String str, int color) {
        SpannableString s = new SpannableString(str);
        s.setSpan(new ForegroundColorSpan(color),
                  0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        vu.post(() -> vu.append(s));
//            sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));        ///> CC: this does not work
    }
    
    @Override
    public void write(int n) throws IOException {
        byte[] b = { (byte)n };
        write(b, 0, b.length);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String rst = new String(b, off, len);
        show(rst, fg);
    }
}


