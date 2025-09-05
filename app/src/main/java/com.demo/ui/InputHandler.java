// ============================================================================
// InputHandler.java - Manages user input and keyboard events
package com.demo.ui;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import com.demo.forth.Eforth;

public class InputHandler {
    private final EditText input;
    private final Eforth   forth;
    
    public InputHandler(EditText input, Eforth forth) {
        this.input = input;
        this.forth = forth;
    }
    
    public void setupKeyListener() {
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int key, KeyEvent ev) {
                if (ev.getAction() == KeyEvent.ACTION_DOWN) {
                    if (key == KeyEvent.KEYCODE_ENTER) {
                        doForth();
                        return true;
                    }
                }
                return false;
            }
        });
    }
    
    public void doForth() {
        String cmd = input.getText().toString().trim();
        
        if (TextUtils.isEmpty(cmd)) return;
        
        forth.outer(cmd);
        clearInput();
    }
    
    private void clearInput() {
        input.setText(null);
        input.requestFocus();
    }
}

