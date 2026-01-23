///
/// @file
/// @brief - System/Device Interface
///
package com.gnii.keforth;

import static android.content.Context.*;

/// file loader
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.widget.ProgressBar;

/// sensor
import android.hardware.SensorManager;
import android.hardware.Sensor;

import androidx.appcompat.app.AppCompatActivity;
import com.gnii.keforth.JavaCallback.PostType;
import com.gnii.keforth.ui.*;

public class Esystem {
    static final int OP_DIR_ACCESS = 11;

    private final MainActivity  main;
    private final SensorManager smgr;
    private final ProgressBar   pb;     ///< progress bar (loading Forth script)

    public  final InputHandler  in;
    public  final OutputHandler out;


    public Esystem(MainActivity main, InputHandler in, OutputHandler out, ProgressBar pb) {
        this.main = main;
        this.smgr = (SensorManager)main.getSystemService(SENSOR_SERVICE);
        this.pb   = pb;
        this.in   = in;
        this.out  = out;
    }

    public String sensorList() {
        StringBuilder sb = new StringBuilder();
        for (Sensor s : smgr.getSensorList(Sensor.TYPE_ALL)) {
            sb.append(s.getType()).append("> ")
              .append(s.getName()).append(" [")
              .append(s.getMinDelay()/1000).append(" ms, R=")
              .append(s.getMaximumRange()).append("]\n");
        }
        return sb.toString();
    }

    @SuppressLint("InlinedApi")
    public void findFile(String uri) {
        Intent nt = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        
        nt.addCategory(Intent.CATEGORY_OPENABLE);
        nt.setType("*/*");
        
//        String[] mimeTypes = new String[]{"application/x-binary, application/octet-stream"};
//        String[] mimeTypes = new String[]{"application/gpx+xml","application/vnd.google-earth.kmz"};
//        nt.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nt.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }
        if (nt.resolveActivity(main.getPackageManager()) == null) {
            out.debug("resolve ACTION_OPEN_DOCUMENT failed\n");
            return;
        }
        main.startActivityForResult(Intent.createChooser(nt, "Choose file"), OP_DIR_ACCESS);
    }

    public void loadFile(int rst, Intent data) {
        /// The document URI selected returned as intent
        if (rst != AppCompatActivity.RESULT_OK) {
            out.debug("findFile cancelled\n");
            return;
        }
        if (data == null || data.getData() == null) {
            out.debug("Uri not found\n");
            return;
        }
        Uri uri = data.getData();
        out.debug("uri="+uri+"\n");

        pb.setVisibility(View.VISIBLE);
        new FileLoader(main.getApplication(),
            new FileLoader.AsyncResponse() {
                @Override
                public void fileLineRead(String cmd) {
                    main.onPost(PostType.FORTH, cmd);
                }

                @Override
                public void fileLoadFinish(String fname) {
                    pb.setVisibility(View.GONE);
                }
            }).execute(uri);
    }
}
