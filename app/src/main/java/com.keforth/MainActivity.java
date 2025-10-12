// MainActivity.java - Refactored main activity
package com.keforth;

/// layout
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.util.TypedValue;
/// JetPack
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
/// file loader
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
/// sensor
import android.hardware.SensorManager;
import android.hardware.Sensor;
/// keForth
import com.keforth.eforth.*;
import com.keforth.ui.*;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME = "keForth v0.8";

    private TextView             con;
    private EditText             ed;             ///< Forth input
    private FloatingActionButton fab;            ///< action button, show Logo panel
    private ViewGroup            vgrp;           ///< Logo panel
    private ProgressBar          pb;             ///< progress bar (loading Forth script)

    private InputHandler         in;             ///< Input component
    private OutputHandler        out;            ///< Output component
    private Eforth               forth;          ///< Forth processor (thread)
    private Elogo                logo;           ///< Logo processor (thread)
    private SensorManager        smgr;           ///< Sensor listing
    
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        
        initViews();
        initComponents();
        setupEventListeners();

        forth.start();                           /// * in a separate thread
    }

    private void initViews() {
        con   = findViewById(R.id.forthOutput);
        ed    = findViewById(R.id.forthInput);
        fab   = findViewById(R.id.buttonProcess);
        vgrp  = findViewById(R.id.logo);
        pb    = findViewById(R.id.progress);
    }

    private void initComponents() {
        out   = new OutputHandler(this, con, R.color.teal_200);
        forth = new Eforth(APP_NAME, out, this);
        in    = new InputHandler(ed, forth);
        
        logo  = new Elogo(vgrp);
//        smgr  = (SensorManager)getSystemService(Context.SENSOR_SERVICE);        
        smgr  = (SensorManager)getSystemService(SENSOR_SERVICE);
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
    public void onPost(String msg) {                         ///< eForth-Java API callback
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
            listSensors();
            break;
        case "font":
            int sz;
            try { sz = Integer.parseInt(ops[1]); }
            catch (NumberFormatException e) { sz = 12; }       /// default 12sp
            con.setTextSize(TypedValue.COMPLEX_UNIT_SP, sz);
            break;
        case "load":
            findFile(n > 1 ? ops[1] : null);
            break;
        default:
            out.debug("unsupported op="+ops[0]+"\n");
            break;
        }
    }

    private static final int OP_DIR_ACCESS     = 10;
    private static final int OP_CONSOLE_UPDATE = 11;
    
    @Override
    public void onActivityResult(int req, int rst, Intent data) {
        super.onActivityResult(req, rst, data);
        /// The ACTION_OPEN_DOCUMENT intent sent => request code OP_DIR_ACCESS
        switch (req) {
        case OP_DIR_ACCESS:
            loadFile(rst, data); break;
        case OP_CONSOLE_UPDATE:
        default: /* do nothing */ break;
        }
    }

    @SuppressLint("InlinedApi")
    private void findFile(String uri) {
        Intent nt = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        
        nt.addCategory(Intent.CATEGORY_OPENABLE);
        nt.setType("*/*");
        
//        String[] mimeTypes = new String[]{"application/x-binary, application/octet-stream"};
//        String[] mimeTypes = new String[]{"application/gpx+xml","application/vnd.google-earth.kmz"};
//        nt.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nt.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }
        if (nt.resolveActivity(getPackageManager()) == null) {
            out.debug("resolve ACTION_OPEN_DOCUMENT failed\n");
            return;
        }
        startActivityForResult(Intent.createChooser(nt, "Choose file"), OP_DIR_ACCESS);
    }
    
    private void loadFile(int rst, Intent data) {
        if (rst != AppCompatActivity.RESULT_OK) {
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

        pb.setVisibility(View.VISIBLE);
        new FileLoader(this,
            new FileLoader.AsyncResponse() {
                @Override
                public void fileLineRead(String cmd) {
//                    out.log(cmd+"\n");
                    forth.process(cmd);
                }
                @Override
                public void fileLoadFinish(String fname) {
                    pb.setVisibility(View.GONE);
                }
            }).execute(uri);
    }
    
    private void listSensors() {
        StringBuilder sb = new StringBuilder("sensor list:\n");
        for (Sensor s : smgr.getSensorList(Sensor.TYPE_ALL)) {
            sb.append(s.getVendor()).append(" ").append(s.getName()).append("\n");
        }
        out.debug(sb.toString());
    }
}




