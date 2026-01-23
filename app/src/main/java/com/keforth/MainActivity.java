///
/// @file
/// @brief - Refactored main activity
///
package com.keforth;

/// layout
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.keforth.ui.*;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME   = "keForth v1.0";
    static {
        System.loadLibrary("ceforth");
    }
    private native void jniMainInit();

    private TextView             con;
    private EditText             ed;             ///< Forth input
    private FloatingActionButton fab;            ///< action button, show Logo panel
    private ProgressBar          pb;             ///< progress bar (loading Forth script)
    private ViewGroup            vgrp;           ///< Logo panel

    private InputHandler         in;             ///< Input component
    private OutputHandler        out;            ///< Output component
    private Esystem              sys;            ///< System/Device interface
    private Eforth               forth;          ///< Forth processor (thread)
    private Elogo                logo;           ///< Logo processor (thread)

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        initViews();
        initComponents();
        setupEventListeners();

        jniMainInit();
        forth.start();                           /// * in a separate thread
    }

    private void initViews() {
        con   = findViewById(R.id.forthOutput);
        ed    = findViewById(R.id.forthInput);
        fab   = findViewById(R.id.buttonProcess);
        pb    = findViewById(R.id.progress);
        vgrp  = findViewById(R.id.logo);
    }

    private void initComponents() {
        in    = new InputHandler(this, ed);
        out   = new OutputHandler(this, con, R.color.teal_200);
        sys   = new Esystem(this, in, out, pb);
        forth = new Eforth(this, sys, APP_NAME);
        logo  = new Elogo(vgrp);
    }

    private void setupEventListeners() {
        ///
        ///> force scroll to bottom of view once updated
        ///
        NestedScrollView sv = findViewById(R.id.forthView);
    
        sv.getViewTreeObserver().addOnGlobalLayoutListener(
            () -> sv.post(new Runnable() {
                /// < update UI, thread-safe way
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_DOWN);
                    ed.requestFocus();
                }
            }));

        fab.setOnClickListener(v -> {
            vgrp.setAlpha(0.9f);
            vgrp.setVisibility(
                vgrp.getVisibility()==View.GONE ? View.VISIBLE : View.GONE
            );
        });
        in.setupKeyListener();
    }

    @Override
    public void onPost(PostType tid, String msg) {           ///< eForth-Java API callback
        switch (tid) {
        case LOG:    out.log(msg);           break;
        case DEBUG:  out.debug(msg);         break;
        case PRINT:  out.print(msg);         break;
        case FORTH:  forth.process(msg);     break;
        case JAVA:   handleJavaAPI(msg);     break;
        default:     out.debug("unsupported tid="+tid+"\n");
        }
    }

    public void onNativeTick() {                             ///< native ticker callback
        forth.process("");
    }

    private void handleJavaAPI(String msg) {
        final String rx = "\\s+(?=(?:[^']*'[^']*')*[^']*$)"; ///< regex single quotes
        String[] ops = msg.split(rx);                        ///< parse parameters
        int      n   = ops.length;

        if (n < 1) return;                                   ///< skip blank lines
        
//        out.debug(msg+"\n");
        switch (ops[0]) {                                    ///< Java command dispatcher
        case "logo":
//            out.debug(logo.status()+"\n");
            logo.process(ops);
//            out.debug(logo.status()+"\n");
            break;
        case "sensors":
            out.debug("Device Sensor List\n");
            out.print(sys.sensorList());
            break;
        case "font":
            int sz;
            try { sz = Integer.parseInt(ops[1]); }
            catch (NumberFormatException e) { sz = 12; }    /// default 12sp
            con.setTextSize(TypedValue.COMPLEX_UNIT_SP, sz);
            break;
        case "load":
            if (n > 1) sys.findFile(ops[1]);
            break;
        default:
            out.debug("unsupported op="+ops[0]+"\n");
            break;
        }
    }

    private static final int OP_DIR_ACCESS = 11;

    @Override
    public void onActivityResult(int req, int rst, Intent data) {
        super.onActivityResult(req, rst, data);
        /// The ACTION_OPEN_DOCUMENT intent sent => request code OP_DIR_ACCESS
        if (req != OP_DIR_ACCESS) return;

        sys.loadFile(rst, data);
    }
}




