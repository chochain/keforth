///
/// @file
/// @brief keForth - Logo/Turtle Graphic implementation (Android Canvas version)
///
package com.demo.logo;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Logo1 extends View {
    private static final String TAG = "Logo";
    
    // Helper methods for color conversion
    public static String RGB(int v) {
        int r = (v >> 16) & 0xff;
        int g = (v >> 8) & 0xff;
        int b = v & 0xff;
        return String.format("rgb(%d %d %d)", r, g, b);
    }
    
    public static int RGBColor(int v) {
        int r = (v >> 16) & 0xff;
        int g = (v >> 8) & 0xff;
        int b = v & 0xff;
        return Color.rgb(r, g, b);
    }
    
    public static int HSVColor(double h) { // 0 < h < 100
        double s = 1.0, v = 1.0;
        int i = (int) Math.floor(h * 0.06);
        double f = h * 0.06 - i;
        double p = v * (1.0 - s);
        double q = v * (1.0 - f * s);
        double t = v * (1.0 - (1.0 - f) * s);
        
        double r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
            default: r = g = b = 0; break;
        }
        return Color.rgb((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }
    
    private static final double RAD = Math.PI / 180.0;
    
    // State class to hold turtle state
    public static class TurtleState {
        public int    w, h;
        public double dir;
        public int    pen, pw, show;
        public int    fg, bg;
        
        public TurtleState(int width, int height) {
            this.w    = width;
            this.h    = height;
            this.dir  = 0;
            this.pen  = 1;
            this.pw   = 3;
            this.show = 0;
            this.fg   = Color.BLACK;
            this.bg   = Color.WHITE;
        }
        
        @Override
        public String toString() {
            return String.format("{w:%d, h:%d, dir:%.2f, pw:%d, pen:%d, show:%d}", 
                               w, h, dir, pw, pen, show);
        }
    }
    
    private TurtleState st;
    private Bitmap      surfaceBitmap, eveBitmap;
    private Canvas      surfaceCanvas, eveCanvas;
    private Paint       linePaint, evePaint;
    private Path        path;
    private boolean     needsRedraw = true;
    
    // Constructors
    public Logo1(Context context) {
        super(context); init();
    }
    
    public Logo1(Context context, AttributeSet attrs) {
        super(context, attrs); init();
    }
    
    public Logo1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }
    
    private void init() {
        // Initialize paints
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        
        evePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        evePaint.setStyle(Paint.Style.STROKE);
        evePaint.setStrokeWidth(3);
        evePaint.setColor(Color.RED);
        evePaint.setStrokeJoin(Paint.Join.ROUND);
        evePaint.setStrokeCap(Paint.Cap.ROUND);
        
        path = new Path();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        
        // Create bitmaps for the two layers
        surfaceBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        eveBitmap     = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        surfaceCanvas = new Canvas(surfaceBitmap);
        eveCanvas     = new Canvas(eveBitmap);
            
        // Initialize state
        st = new TurtleState(w, h);
        reset();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (surfaceBitmap == null || eveBitmap == null) return;
        
        canvas.drawBitmap(surfaceBitmap, 0, 0, null);  // Draw the surface layer
        canvas.drawBitmap(eveBitmap, 0, 0, null);      // Draw the eve (turtle) layer on top
    }
    
    private void clearEve() {
        if (eveCanvas == null) return;
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    private void drawEve(int color) {
        if (eveCanvas == null) return;
        
        final double W = Math.PI / 15, X = Math.PI / 6;
        
        evePaint.setColor(color);
        eveCanvas.save();                           // Save canvas state
        
        // Translate and rotate to current turtle position and direction
        eveCanvas.translate((float)st.w/2, (float)st.h/2);
        eveCanvas.rotate((float)Math.toDegrees(st.dir));
        
        // Draw turtle body using paths
        Path turtle = new Path();
        
        // Left shoulder arc
        RectF shoulderRect = new RectF(-20, -20, 20, 20);
        turtle.addArc(shoulderRect, (float)Math.toDegrees(-X), (float)Math.toDegrees(X - W));
        
        // Move to center for right shoulder
        turtle.moveTo(0, 0);
        turtle.addArc(shoulderRect, (float)Math.toDegrees(W), (float)Math.toDegrees(X - W));
        
        // Head circle
        turtle.addCircle(24, 0, 4, Path.Direction.CW);
        eveCanvas.drawPath(turtle, evePaint);
        
        // Restore canvas state
        eveCanvas.restore();
    }

    private void center(float x, float y, double deltaDir) {
        st.dir = 0;
        path.reset();                                // Reset to center
        path.moveTo(st.w/2f + x, st.h/2f + y);
    }
    
    private void xform(float x, float y, double deltaDir) {
        st.dir -= deltaDir;
        // Calculate new position based on current transform
        float newX = (float)(st.w/2 + x * Math.cos(st.dir) - y * Math.sin(st.dir));
        float newY = (float)(st.h/2 + x * Math.sin(st.dir) + y * Math.cos(st.dir));
        
        if (st.pen == 1) path.lineTo(newX, newY);
        else             path.moveTo(newX, newY);
    }
    
    public void reset() {
        if (st == null) return;
        
        center(0, 0, st.dir + 90.0 * RAD);
        linePaint.setStrokeWidth(st.pw);
        linePaint.setColor(st.fg);
        
        if (st.show == 1) drawEve(st.fg);

        invalidate();                              // Trigger redraw
    }
    
    public boolean update(Object[] av) {
        if (av.length < 2 || st == null) return false;
        
        String op = (String) av[1];
        double v = av.length > 2 ? ((Number) av[2]).doubleValue() : 0;
        
        clearEve();
        
        switch (op) {
            case "cs":                                         /// clear screen
                surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                path.reset();
                path.moveTo(st.w/2f, st.h/2f);         break;
            case "st": st.show = 1;                    break;  /// show turtle
            case "ht": st.show = 0;                    break;  /// hide turtle
            case "ct": reset();                        break;  /// center turtle
            case "pd": st.pen = 1;                     break;  /// pen down
            case "pu": st.pen = 0;                     break;  /// pen up
            case "hd": xform(0, 0, st.dir - v * RAD);  break;  /// set heading
            case "fd": xform((float)v, 0, 0);          break;  /// forward
            case "bk": xform((float)-v, 0, 0);         break;  /// backward
            case "rt": xform(0, 0, v * RAD);           break;  /// right turn
            case "lt": xform(0, 0, -v * RAD);          break;  /// left turn
            case "pc":                                         /// pen color (HSV)
                st.fg = HSVColor(v);
                linePaint.setColor(st.fg);             break;
            case "fg":                                         /// foreground color (RGB)
                st.fg = RGBColor((int) v);
                linePaint.setColor(st.fg);             break;
            case "bg": st.bg = RGBColor((int) v);      break;  /// background color (RGB)
            case "pw":                                         /// pen width
                st.pw = (int)v;
                linePaint.setStrokeWidth(st.pw);       break;
            case "xy":                                        // set position
                int intV = (int) v;
                int x = (intV & 0xffff);
                int y = (intV >> 16) & 0xffff;
                
                // Handle negative coordinates (sign extension)
                if ((x & 0x8000) != 0) x |= 0xffff0000;
                if ((y & 0x8000) != 0) y |= 0xffff0000;
                
                float newX = st.w/2f + x;
                float newY = st.h/2f - y; // Flip Y for screen coordinates
                
                if (st.pen == 1) path.lineTo(newX, newY);
                else             path.moveTo(newX, newY);
                
                st.dir = 0;
                break;
            default: return false;
        }
        
        // Draw the current path on surface canvas
        surfaceCanvas.drawPath(path, linePaint);
        
        // Draw turtle if visible
        if (st.show == 1) drawEve(st.fg);
        
        // Debug output
        Log.d(TAG, st.toString());
        
        // Trigger view redraw
        invalidate();
        return true;
    }
}
