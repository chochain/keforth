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
    private static final double RAD = Math.PI / 180.0;
    
    public static int RGBColor(int v) {            /// convert v to ARGB
        return v | 0xff000000;                     /// alpha=100%, i.e. opaque
    }
    public static int HSVColor(int h) {            /// 0 < h < 100
        double s = 1.0, v = 1.0;
        int    i = (int)Math.floor(h * 0.06);
        double f = (double)h * 0.06 - i;
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
    
    // State class to hold turtle state
    public static class State {
        public float  x, y;               ///< current x, y (0,0 at top-left)
        public int    w, h;               ///< width, height of display port
        public double d;                  ///< direction
        public int    pen, pw, show;      ///< pen-down, pen-width, pen-shown
        public int    fg, bg;             ///< fore/background colors
        
        public State(int w, int h) {
            this.w    = w;
            this.h    = h;                ///< x, y, d will be set by reset()
            this.pen  = 1;
            this.pw   = 3;                ///< 3-pixel stroke
            this.show = 0;
            this.fg   = Color.WHITE;
            this.bg   = Color.GREEN;
        }
        @Override
        public String toString() {
            return String.format(
                "{x:%.1f, y:%.1f, w:%d, h:%d, d:%.2f, pw:%d, pen:%d, show:%d}",
                x, y, w, h, d, pw, pen, show);
        }
    }
    private State   st;
    private Bitmap  sfcBitmap, eveBitmap;
    private Canvas  sfcCanvas, eveCanvas;
    private Paint   sfcPaint,  evePaint;
    private Path    path;
    private boolean redraw = true;
    
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
        sfcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sfcPaint.setStyle(Paint.Style.STROKE);
        sfcPaint.setStrokeJoin(Paint.Join.ROUND);
        sfcPaint.setStrokeCap(Paint.Cap.ROUND);
        
        evePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        evePaint.setStyle(Paint.Style.STROKE);
        evePaint.setStrokeWidth(3);
        evePaint.setColor(Color.RED);
        evePaint.setStrokeJoin(Paint.Join.ROUND);
        evePaint.setStrokeCap(Paint.Cap.ROUND);
        
        path = new Path();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int w0, int h0) {
        super.onSizeChanged(w, h, w0, h0);
        if (w <= 0 || h <= 0) return;
        
        // Create bitmaps for the two layers
        sfcBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        eveBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        sfcCanvas = new Canvas(sfcBitmap);
        eveCanvas = new Canvas(eveBitmap);
            
        // Initialize state
        st = new State(w, h);
        reset();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sfcBitmap==null || eveBitmap==null) return;
        
        canvas.drawBitmap(sfcBitmap, 0, 0, null);  /// Draw the surface layer
        canvas.drawBitmap(eveBitmap, 0, 0, null);  /// Draw the eve (turtle) layer on top
    }

    private void clearEve() {
        if (eveCanvas==null) return;
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    private void drawEve(int color) {
        if (eveCanvas==null) return;
        
        final double W = Math.PI / 15, X = Math.PI / 6;
        evePaint.setColor(color);
        eveCanvas.save();                           /// save canvas state
        
        /// Translate and rotate to current turtle position and direction
        eveCanvas.translate(st.x, st.y);
        eveCanvas.rotate((float)Math.toDegrees(st.d));
        
        Path  eve = new Path();                     /// draw Eve body using paths
        eve.addCircle(24, 0, 4, Path.Direction.CW); /// Eve's head
        
        RectF rec = new RectF(-20, -20, 20, 20);    /// bounding oval of shoulder arcs
        eve.addArc(rec, (float)Math.toDegrees(-X), (float)Math.toDegrees(X - W));   /// left shoulder, (oval, start, sweep)
        eve.lineTo(0, 0);                           /// move to feet, then right shoulder
        eve.addArc(rec, (float)Math.toDegrees(X), (float)Math.toDegrees(W - X));    /// right shoulder
        eve.lineTo(0, 0);                           /// final at feet
        
        eveCanvas.drawPath(eve, evePaint);
        eveCanvas.restore();                        /// Restore canvas state
    }

    private void center(float x, float y, double delta) {
        path.reset();                               /// Reset to center
        st.d += delta;
        st.x = 0.5f * st.w + x;
        st.y = 0.5f * st.h - y;
        path.moveTo(st.x, st.y);
    }
    private void repath() {
        path.reset();
        path.moveTo(st.x, st.y);
    }
    
    private void xform(int x, int y, double delta) {
        st.d += delta;
        float c = (float)Math.cos(st.d), s = (float)Math.sin(st.d);
        
        // Calculate new position based on current transform
        st.x += c * x - s * y;                     /// * TODO: use 4x4 matrix
        st.y += s * x + c * y;
        
        if (st.pen==1) path.lineTo(st.x, st.y);
        else           path.moveTo(st.x, st.y);
    }
    
    public void reset() {
        if (st==null) return;

        center(0, 0, -st.d - Math.PI * 0.5);               /// recenter, due North
        sfcPaint.setStrokeWidth(st.pw);
        sfcPaint.setColor(st.fg);
        
        if (st.show==1) drawEve(st.fg);

        invalidate();                                      /// trigger redraw
    }

    public String to_s() { return st.toString(); }
    public boolean update(String op, int v) {
        if (st == null) return false;
        
        clearEve();                                        /// hide turtle
        
        switch (op) {
        case "cs":                                         /// clear screen
            sfcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            path.reset();
            center(0, 0, -st.d);                   break;
        case "st": st.show = 1;                    break;  /// show turtle
        case "ht": st.show = 0;                    break;  /// hide turtle
        case "ct": center(0, 0, 0);                break;  /// center turtle
        case "pd": st.pen = 1;                     break;  /// pen down
        case "pu": st.pen = 0;                     break;  /// pen up
        case "hd": xform(0, 0, -st.d-RAD*(90-v));  break;  /// set heading, 0=North
        case "fd": xform(v, 0, 0);                 break;  /// forward
        case "bk": xform(-v, 0, 0);                break;  /// backward
        case "rt": xform(0, 0, RAD * v);           break;  /// right turn
        case "lt": xform(0, 0, -RAD * v);          break;  /// left turn
        case "pc":                                         /// pen color (HSV)
            repath();
            st.fg = HSVColor(v);
            sfcPaint.setColor(st.fg);              break;
        case "fg":                                         /// foreground color (RGB)
            repath();
            st.fg = RGBColor(v);
            sfcPaint.setColor(st.fg);              break;
        case "bg":
            repath();
            st.bg = RGBColor(v);                   break;  /// background color (RGB)
        case "pw":                                         /// pen width
            st.pw = v;
            sfcPaint.setStrokeWidth(st.pw);        break;
        case "xy":                                         /// set position
            int x = (v & 0xffff);
            int y = (v >> 16) & 0xffff;
                
            // Handle negative coordinates (sign extension)
            if ((x & 0x8000) != 0) x |= 0xffff0000;
            if ((y & 0x8000) != 0) y |= 0xffff0000;
                
            st.x = 0.5f * st.w + x;
            st.y = 0.5f * st.h - y;                        /// Flip Y for screen coordinates
                
            if (st.pen==1) path.lineTo(st.x, st.y);
            else           path.moveTo(st.x, st.y); break;
        default: return false;
        }

        sfcCanvas.drawPath(path, sfcPaint);                /// Draw the current path on surface canvas
        if (st.show==1) drawEve(st.fg);                    /// Draw turtle if visible
        
        invalidate();                                      /// Trigger view redraw

        return true;
    }
}
