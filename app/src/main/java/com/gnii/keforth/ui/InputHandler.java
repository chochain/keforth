///
/// @file
/// @brief - Manages user input and keyboard events
///
package com.gnii.keforth.ui;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;

import com.gnii.keforth.JavaCallback;

public class InputHandler {
    private final JavaCallback main;
    private final EditText in;
    
    public InputHandler(JavaCallback main, EditText in) {
        this.main = main;
        this.in   = in;
    }
    
    public void setupKeyListener() {
        in.setOnKeyListener((v, key, ev) -> {
            if (ev.getAction() == KeyEvent.ACTION_DOWN) {
                if (key == KeyEvent.KEYCODE_ENTER) {
                    doForth();
                    return true;
                }
            }
            return false;
        });
    }
    
    public void doForth() {
        String cmd = in.getText().toString().trim();
        
        if (TextUtils.isEmpty(cmd)) return;
        
        main.onPost(JavaCallback.PostType.FORTH, cmd);                /// * enqueue forth.Looper
        clearInput();
    }
    
    private void clearInput() {
        in.setText(null);
        in.requestFocus();
    }
}

