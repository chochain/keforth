// MainActivity.java - Refactored main activity
package com.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.hardware.SensorManager;
import android.hardware.Sensor;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.demo.eforth.*;
import com.demo.logo.*;
import com.demo.ui.*;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME = "keForth v0.8";
    
    private EditText             ed;
    private FloatingActionButton fab;
    private ViewGroup            vgrp;

    private InputHandler         in;
    private OutputHandler        out;
    private Eforth               forth;
    private Elogo                logo;
    private SensorManager        smgr;
    
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
        out   = new OutputHandler(this, R.id.forthOutput, R.color.teal_200);
        forth = new Eforth(APP_NAME, out, this);
        in    = new InputHandler(ed, forth);
        forth.init();
        
        logo  = new Elogo(vgrp);
//        smgr  = (SensorManager)getSystemService(Context.SENSOR_SERVICE);        
        smgr  = (SensorManager)getSystemService(this.SENSOR_SERVICE);
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
                vgrp.setVisibility(
                    vgrp.getVisibility()==View.GONE ? View.VISIBLE : View.GONE
                );
            }
        });
        in.setupKeyListener();
    }
    
    @Override
    public void onPost(String msg) {
        final String rx = "\\s+(?=(?:[^']*'[^']*')*[^']*$)"; ///< regex single quotes
        String[] ops = msg.split(rx);                        ///< parse parameters
        int      n   = ops.length;

        if (n < 1) return;                                   ///< skip blank lines
        
        out.debug(msg+"\n");
        switch (ops[0]) {
        case "logo":
            out.debug(logo.status()+"\n");
            logo.process(ops);
            out.debug(logo.status()+"\n");
            break;
        case "sensors":
            listSensors();
            break;
        default:
            out.debug("unsupported op="+ops[0]+"\n");
            break;
        }
    }

    private void listSensors() {
        StringBuilder sb = new StringBuilder("sensor list:\n");
        for (Sensor s : smgr.getSensorList(Sensor.TYPE_ALL)) {
            sb.append(s.getVendor()).append(" ").append(s.getName()).append("\n");
        }
        out.debug(sb.toString());
    }
}




