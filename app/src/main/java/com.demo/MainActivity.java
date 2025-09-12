// MainActivity.java - Refactored main activity
package com.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
    static final String APP_NAME = "keForth v0.6";
    
    private EditText             ed;
    private FloatingActionButton fab;
    private ViewGroup            vgrp;

    private InputHandler         in;
    private OutputHandler        out;
    private Eforth               forth;
    private Elogo                logo;
    
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        
        initViews();
        initComponents();
        setupEventListeners();
    }
    
    private void initViews() {
        ed    = findViewById(R.id.forthInput);
        fab   = findViewById(R.id.buttonProcess);
        vgrp  = findViewById(R.id.logo);
    }
    
    private void initComponents() {
        out   = new OutputHandler(this);
        forth = new Eforth(APP_NAME, out, this);
        in    = new InputHandler(ed, forth);
        forth.init();
        
        logo  = new Elogo(vgrp);
    }
    
    private void setupEventListeners() {
        ///
        ///> force scroll to bottom of view once updated
        ///
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
                out.debug("fab clicked\n");
            }
        });
        in.setupKeyListener();
    }
    
    @Override
    public void onPost(String msg) {
        out.debug(msg+"\n");
        out.debug(logo.status()+"\n");
        logo.process(msg);
        out.debug(logo.status()+"\n");
    }
}




