// MainActivity.java
package com.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableStringBuilder;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import com.demo.R;

public class MainActivity extends AppCompatActivity {
    private EditText             in;
    private TextView             out;
    private FloatingActionButton fb;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        in  = findViewById(R.id.editTextInput);
        out = findViewById(R.id.textViewOutput);
        fb  = findViewById(R.id.buttonProcess);
        
        // Set click listener for the button
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processInput();
            }
        });
        
        // Process input when user presses Shift+Enter
        in.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed()) {
                        processInput();
                        return true; // Consume the event
                    }
                }
                return false; // Let other key events pass through
            }
        });
    }
    
    private void processInput() {
        // Get user input
        String cmd = in.getText().toString().trim();

        if (TextUtils.isEmpty(cmd)) return;
        
        // Process the input (example processing)
        String rst = processText(cmd);
        
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString s1 = new SpannableString(cmd);
        s1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.teal_200)),
                    0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(s1);

        SpannableString s2 = new SpannableString(rst); // Add a space if needed
        s2.setSpan(new ForegroundColorSpan(Color.WHITE),
                    0, s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(s2);
    
        // Display result in TextView
        out.append(builder);
        in.setText(null);
    }
    
    private String processText(String s) {
        String upperCase = s.toUpperCase();
        int    charCount = s.length();
        String reversed  = new StringBuilder(s).reverse().toString();
        
        return String.format("\nOriginal: %s\n" +
                           "Uppercase: %s\n" +
                           "Character count: %d\n" +
                           "Reversed: %s\n", 
                           s, upperCase, charCount, reversed);
    }
}


