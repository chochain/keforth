///
/// @file
/// @brief - load file from content resolver into app cache directory
///
import java.lang.ref.WeakReference;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.AsyncTask;
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;

public class FileLoader extends AsyncTask<Uri, Void, String> {
    private WeakReference<Context> ref;
    public AsyncResponse           callback = null;

    public interface AsyncResponse {
        void fileLoadFinish(String result);
    }

    FileLoader(Context ctx , AsyncResponse cb) {
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

    protected String doInBackground(Uri... uris) {
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
            
            String  fname = cursor.getString(idx);
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
        byte[] buf = new byte[1024];
        int    tot = 0;
        int    len;
        try {
            while ((len = ins.read(buf)) > 0) {
                out.write(buf, 0, len);
                tot += len;
            }
            out.flush();
            out.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tot;
    }
}
