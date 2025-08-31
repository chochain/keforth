// MainActivity.java
package com.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ScrollView;
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

public class MainActivity extends AppCompatActivity {
    static final String APP_NAME = "keForth v0";
    EditText               in;
    FloatingActionButton   fb;
    int                    color_fg;
    int                    color_cm;
    
    class Updater extends OutputStream {
        SpannableStringBuilder buf;
        TextView               out;
        ScrollView             sc;

        Updater() {
            buf = new SpannableStringBuilder();
            out = findViewById(R.id.textViewOutput);
            sc  = findViewById(R.id.scrollView);
        }

        public void show(String str, int c) {
            SpannableString s = new SpannableString(str);
            s.setSpan(new ForegroundColorSpan(c),
                      0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            buf.append(s);
//            out.append(s);
            out.post(() -> out.append(s));
            sc.requestFocus();
            buf.clear();
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
    Updater up;
    IO      io;
    VM      vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /// Initialize views
        in       = findViewById(R.id.editTextInput);
        fb       = findViewById(R.id.buttonProcess);
        color_fg = Color.WHITE;
        color_cm = getResources().getColor(R.color.teal_200);
        
        up  = new Updater();
        io  = new IO(APP_NAME, System.in, up);
        vm  = new VM(io);
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
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed()) {
                        doForth();
                        return true;                   /// Consume the event
                    }
                }
                return false;                          /// Let other key events pass through
            }
        });
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
}


