// MainActivity.java
package com.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableStringBuilder;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import com.demo.R;

import java.io.*;
import com.demo.eforth.*;
import com.demo.logo.*;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME = "keForth v0";
    EditText              in;       ///< User Input for Forth command
    FloatingActionButton  fb;       ///< alternate command processor
    IO                    io;       ///< eForth IO dispatcher
    VM                    vm;       ///< eForth VM instance
    Updater               up;       ///< Forth output dispatcher
    Logo1                 logo;     ///< Logo (View to paint on)
    /// colors
    int  color_fg;                  ///< forth output color
    int  color_cm;                  ///< forth command color
    int  color_cb;                  ///< Logo command (via callback)
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /// Initialize views
        in       = findViewById(R.id.forthInput);
        fb       = findViewById(R.id.buttonProcess);
        logo     = findViewById(R.id.logo);
        
        color_fg = Color.WHITE;
        color_cm = getResources().getColor(R.color.teal_200);
        color_cb = Color.RED;
        
        up  = new Updater();
        io  = new IO(APP_NAME, System.in, up);
        vm  = new VM(io, this);
        io.mstat();
        
        /// Set click listener for the button
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { doForth(); }
        });
        /// Process input when user presses Shift+Enter
        in.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed()) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        doForth();
                        return true;                   /// Consume the event
                    }
                }
                return false;                          /// Let other key events pass through
            }
        });
    }
    ///
    /// implement JavaCallback interface
    ///
    public void onPost(String msg) {
        String[] op = msg.split(" ");
        int      n  = op.length;
        up.show("op["+n+"] ", color_cb);
        String k = n > 0 ? op[0] : "NA";       /// message key
        int    v = n > 1 ? Integer.parseInt(op[1]) : 0;
        up.show(k+"=>"+v+"\n", color_cb);
        
        up.show("before "+logo.to_s()+"\n", color_cb);
        logo.update(k, v);
        up.show("after "+logo.to_s()+"\n", color_cb);
    }
    
    private void doForth() {
        // Get user input
        String cmd = in.getText().toString().trim();

        if (TextUtils.isEmpty(cmd)) return;

        up.show(cmd+"\n", color_cm);                  /// echo cmd
        io.rescan(cmd);                               /// reload scanner
        
        while (io.readline()) {
            if (!vm.outer()) break;
        }
        in.setText(null);
        in.requestFocus();
    }
    
    private class Updater extends OutputStream {
        TextView   out;
        ScrollView sv;

        Updater() {
            out = findViewById(R.id.forthOutput);
            sv  = findViewById(R.id.forthView);
            sv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        sv.post(new Runnable() {
                            @Override
                            public void run() {
                                sv.fullScroll(ScrollView.FOCUS_DOWN);
                                in.requestFocus();
                            }
                        });
                    }
                }
            );
        }
        public void show(String str, int color) {
            SpannableString s = new SpannableString(str);
            s.setSpan(new ForegroundColorSpan(color),
                      0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            out.post(() -> out.append(s));
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
            show(rst, color_fg);
        }
    }
}


