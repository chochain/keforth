// MainActivity.java - Refactored main activity
package com.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.demo.eforth.*;
import com.demo.logo.*;
import com.demo.ui.OutputHandler;
import com.demo.ui.InputHandler;
import com.demo.forth.Eforth;

public class MainActivity extends AppCompatActivity implements JavaCallback {
    static final String APP_NAME = "keForth v0";
    
    private EditText             edit;
    private FloatingActionButton fab;
    private Logo1                logo;
    
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
        edit = findViewById(R.id.forthInput);
        fab  = findViewById(R.id.buttonProcess);
        logo = findViewById(R.id.logo);
    }
    
    private void initComponents() {
        out   = new OutputHandler(this);
        forth = new Eforth(APP_NAME, out, this);
        in    = new InputHandler(edit, forth);
        
        forth.init();
    }
    
    private void setupEventListeners() {
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
        Elogo.process(msg, logo, out);
    }
}




