// MainActivity.java - Refactored main activity
package com.demo;
/// layout
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.EditText;
/// JetPack
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
/// file loader
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
/// sensor
import android.hardware.SensorManager;
import android.hardware.Sensor;
/// keForth
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
        NestedScrollView sv = findViewById(R.id.forthView);
    
        sv.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    sv.post(new Runnable() {                     ///< update UI, thread-safe way
                        @Override
                        public void run() {
                            sv.fullScroll(View.FOCUS_DOWN);
                            ed.requestFocus();
                        }
                    });
                }
            });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vgrp.setAlpha(0.9f);
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
        switch (ops[0]) {                                    ///< Java command dispatcher
        case "logo":
            out.debug(logo.status()+"\n");
            logo.process(ops);
            out.debug(logo.status()+"\n");
            break;
        case "sensors":
            listSensors();
            break;
        case "load":
            findFile(n > 1 ? ops[1] : null);
            break;
        default:
            out.debug("unsupported op="+ops[0]+"\n");
            break;
        }
    }

    private static final int DIR_ACCESS_REQUEST_CODE = 13;
    
    @Override
    public void onActivityResult(int req, int rst, Intent data) {
        super.onActivityResult(req, rst, data);
        /// The ACTION_OPEN_DOCUMENT intent sent => request code OPEN_DIRECTORY_REQUEST_CODE
        if (req != DIR_ACCESS_REQUEST_CODE) return;
        if (rst != Activity.RESULT_OK) {
            out.debug("findFile cancelled\n");
            return;
        }
        if (data == null || data.getData() == null) {
            out.debug("Uri not found\n");
            return;
        }
        /// The document URI selected returned as intent
        Uri uri = data.getData();
        out.debug("uri="+uri+"\n");
/*
        mProgressBar.setVisibility(View.VISIBLE);
        new FileLoader(this,
            new FileLoader.AsyncResponse() {
                @Override
                public void fileLoadFinish(String result) {
                    processFile(new File(result));
                    mProgressBar.setVisibility(View.GONE);
                }
            }).execute(uri);
*/            
    }
    
    public void findFile(String uri) {
        Intent nt = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        
        nt.addCategory(Intent.CATEGORY_OPENABLE);
        nt.setType("*/*");
        
//        String[] mimeTypes = new String[]{"application/x-binary, application/octet-stream"};
//        String[] mimeTypes = new String[]{"application/gpx+xml","application/vnd.google-earth.kmz"};
//        nt.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        if (uri != null) nt.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        if (nt.resolveActivity(getPackageManager()) == null) {
            out.debug("resolve ACTION_OPEN_DOCUMENT failed\n");
            return;
        }
        startActivityForResult(Intent.createChooser(nt, "Choose file"), DIR_ACCESS_REQUEST_CODE);
    }
    
    private void listSensors() {
        StringBuilder sb = new StringBuilder("sensor list:\n");
        for (Sensor s : smgr.getSensorList(Sensor.TYPE_ALL)) {
            sb.append(s.getVendor()).append(" ").append(s.getName()).append("\n");
        }
        out.debug(sb.toString());
    }
}




