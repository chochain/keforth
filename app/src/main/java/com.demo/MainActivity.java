// MainActivity.java - Refactored main activity
package com.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.demo.eforth.*;
import com.demo.logo.*;
import com.demo.ui.OutputHandler;
import com.demo.ui.InputHandler;
import com.demo.forth.Eforth;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME = "keForth v0";
    
    private EditText             ed;
    private FloatingActionButton fab;
    private View                 vu;

    private Elogo                logo;
    private Eforth               forth;
    private InputHandler         in;
    private OutputHandler        out;
    
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        
        initViews();
        initComponents();
        setupEventListeners();
    }
    
    private void initViews() {
        ed  = findViewById(R.id.forthInput);
        fab = findViewById(R.id.buttonProcess);
        vu  = findViewById(R.id.logo);
    }
    
    private void initComponents() {
        out   = new OutputHandler(this);
        logo  = new Elogo(vu, out);
        forth = new Eforth(APP_NAME, out, this);
        in    = new InputHandler(ed, forth);
        
        forth.init();
    }
    
    private void setupEventListeners() {
        ScrollView sv = findViewById(R.id.forthView);
        sv.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    sv.post(new Runnable() {
                            @Override
                            public void run() {
                                sv.fullScroll(ScrollView.FOCUS_DOWN);
                                ed.requestFocus();
                            }
                        });
                }
            });
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                in.doForth();
            }
        });
        in.setupKeyListener();
    }
    
    @Override
    public void onPost(String msg) {
        logo.process(msg);
    }
}




