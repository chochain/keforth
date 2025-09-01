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
    Logo                  logo;     ///< Logo (View to paint on)
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
    public void post(String str) {
        up.show(str+"\n", color_cb);
    }
/*        
self.onmessage = function(e) {                        ///> worker input message queue
    let k = e.data[0], v = e.data[1]
    const post = (v)=>postMessage([k, v])             ///> macro to response to front-end
    switch (k) {
    case 'cmd': post(forth(0, v));           break    /// * call Forth VM (output=>Module.print)she
    case 'key': forth(1, v); post(0);        break    /// * PUSH(v), clear keypress mode
    case 'dc' : post(get_dict());            break    /// * built-in words
    case 'usr': post(get_dict(true));        break    /// * colon words
    case 'ss' : post(get_ss());              break    /// * dump stack
    case 'dm' :                                       /// * dump memory
        const idx = v[0], n = v[1]
        const here= wasmExports.vm_mem_idx()
        const len = (n + 0x10) & ~0xf                 ///> 16-byte blocks
        const off = idx < 0                           ///> idx < 0 => from 'HERE'
            ? (here > len ? here - len : 0)
            : idx
        const ma  = get_mem(off & ~0xf, len)          ///> get memory ref
        post(dump(ma, off));                 break
    case 'mm' :
        const mm = get_mem(v[0], v[1])                ///> fetch memory block
        postMessage(                                  /// * to front-end, transfer
            [ k, mm ],
            [ mm.buffer ]);                  break
    case 'px' :
        const px = get_px(v)                          ///> fetch px values from Forth
        postMessage(                                  ///> to front-end, transfer
            [ k, px ],
            [ px[2].buffer, px[3].buffer ]); break
    default   : post('unknown type');
    }
}
*/
    
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


