// ============================================================================
// InputHandler.java - Manages user input and keyboard events
package com.demo.ui;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import com.demo.forth.Eforth;

public class InputHandler {
    private final EditText in;
    private final Eforth   forth;
    
    public InputHandler(EditText in, Eforth forth) {
        this.in    = in;
        this.forth = forth;

    }
    
    public void setupKeyListener() {
        in.setOnKeyListener(new View.OnKeyListener() {
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
        String cmd = in.getText().toString().trim();
        
        if (TextUtils.isEmpty(cmd)) return;
        
        forth.process(cmd);
        clearInput();
    }
    
    private void clearInput() {
        in.setText(null);
        in.requestFocus();
    }
}

