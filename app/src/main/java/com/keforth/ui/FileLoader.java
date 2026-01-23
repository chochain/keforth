///
/// @file
/// @brief - load file from content resolver into app cache directory
///
package com.keforth.ui;

import java.lang.ref.WeakReference;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;

import android.os.AsyncTask;
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

public class FileLoader extends AsyncTask<Uri, Void, String> {
    private final WeakReference<Context> ref;
    public AsyncResponse           callback;

    public interface AsyncResponse {
        void fileLineRead(String cmd);
        void fileLoadFinish(String result);
    }

    public FileLoader(Context ctx , AsyncResponse cb) {
        ref      = new WeakReference<>(ctx);
        callback = cb;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String rst) {
        callback.fileLoadFinish(rst);
        super.onPostExecute(rst);
    }

    protected String doInBackground(@NonNull Uri... uris) {
        Context         ctx  = ref.get();
        ContentResolver rsvr = ctx.getContentResolver();

        Uri uri = uris[0];
        try {
            //File    dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            //String  fn  = dir + "/" + fname;
            String mimeType = rsvr.getType(uri);
            Cursor cursor   = rsvr.query(uri, null, null, null, null);
            int    idx      = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            
            cursor.moveToFirst();
            
            String  fname = cursor.getString(idx).replace("..","_");
            File    cache = ctx.getCacheDir();
            
            InputStream  ins = rsvr.openInputStream(uri);
            OutputStream out = new FileOutputStream(new File(cache, fname));

            copyTo(ins, out);
            cursor.close();

            return fname;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int copyTo(InputStream ins, OutputStream out) {
        int len = 0;
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(ins, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                callback.fileLineRead(line);
                out.write(line.getBytes());
                len += line.length();
            }
            out.flush();
            out.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return len;
    }
}
