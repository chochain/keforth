///
/// @file
/// @brief keForth - Logo/Turtle Graphic implementation (Android Canvas version)
/// @note
///    Logo1 is an implementation that has no separation among the following
///    * Turtle computational logic
///    * No Renderer abstraction
///    * Android specific rendering (separate logical from screen coordinates)
///    * View interface
///    Logo2 does those but bloated the code 2x lines
///
package com.keforth.logo;

import android.content.Context;
import android.graphics.*;
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
        public float  d    = 0;           ///< direction (in degree, 0=East)
        public int    pen  = 1;           ///< 0: pen-up, 1: pen-down
        public int    pw   = 3;           ///< pen brush width (3-pixel)
        public int    show = 0;           ///< 0: hide turtle, 1: show turtle
        public int    fg   = Color.WHITE; ///< brush foreground colors
        public int    bg   = Color.GREEN; ///< background color (not used now)
        
        public State(int w, int h) {
            this.w    = w;
            this.h    = h;                ///< x, y, d will be set by reset()
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
    
    public Logo1(Context context) { super(context); init(); }
    
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
        final float A = 30; //(float)Math.toDegrees(Math.PI/6);   ///< starting angle
        final float S = 18; // (float)Math.toDegrees(Math.PI/10);  ///< sweeping angle
        
        if (eveCanvas==null) return;
        
        evePaint.setColor(color);
        eveCanvas.save();                           /// save canvas state
        
        /// Translate and rotate to current turtle position and direction
        eveCanvas.translate(st.x, st.y);
        eveCanvas.rotate(st.d);
        
        Path  eve = new Path();                     /// draw Eve body using paths
        eve.addCircle(24, 0, 4, Path.Direction.CW); /// Eve's head
        
        RectF rec = new RectF(-20, -20, 20, 20);    /// bounding oval of shoulder arcs
        eve.addArc(rec, -A, S);                     /// left shoulder, (oval, start, sweep)
        eve.lineTo(0, 0);                           /// move to feet, then right shoulder
        eve.addArc(rec, A, -S);                     /// right shoulder
        eve.lineTo(0, 0);                           /// final at feet
        
        eveCanvas.drawPath(eve, evePaint);
        eveCanvas.restore();                        /// Restore canvas state
    }
    private void xcenter(                           /// change center
        float x, float y, float delta) {  
        path.rewind();                              /// reset to (0,0)
        st.d += delta;
        st.x = 0.5f * st.w + x;                     /// absolute coor
        st.y = 0.5f * st.h - y;
        path.moveTo(st.x, st.y);                    ///< move to new center
    }
    private void xcolor(int color) {                ///< change surface paint color
        path.rewind();
        st.fg = color;
        sfcPaint.setColor(st.fg);
        path.moveTo(st.x, st.y);
    }
    
    private void xform(int x, int y, float delta) {
        st.d += delta;
        float c = (float)Math.cos(st.d * RAD);     /// cos(dir)
        float s = (float)Math.sin(st.d * RAD);     /// sin(dir)
        
        // Calculate new position based on current transform
        st.x += c * x - s * y;                     /// * TODO: use 4x4 matrix
        st.y += s * x + c * y;
        
        if (st.pen==1) path.lineTo(st.x, st.y);
        else           path.moveTo(st.x, st.y);
    }
    private void xtext(String txt) {
        sfcCanvas.save();

        Rect r = new Rect();
        sfcPaint.getTextBounds(txt, 0, txt.length(), r);
        
        sfcCanvas.translate(st.x, st.y);           ///< reposition text at turtle
        sfcCanvas.rotate(st.d + 90);               ///< reorient text to st.d
        sfcCanvas.drawText(                        ///< remove quotes
            txt.substring(1, txt.length()-1),
            -0.5f * r.width(), 0.5f * r.height(), sfcPaint);
        
        sfcCanvas.restore();
     }
    
    public void reset() {
        if (st==null) return;

        xcenter(0, 0, -st.d - 90);                 /// recenter, due North
        sfcPaint.setStrokeWidth(st.pw);
        sfcPaint.setColor(st.fg);
        sfcPaint.setTextSize(20);                  /// default text size
        
        if (st.show==1) drawEve(st.fg);

        invalidate();                                      /// trigger redraw
    }

    public String to_s() { return st.toString(); }
    public boolean execute(String op, String v1, String v2) {
        if (st == null) return false;
        int n = op.equals("tt") ? 0 : Integer.parseInt(v1);
        
        clearEve();                                        /// hide turtle
        
        switch (op) {
        case "cs":                                         /// clear screen
            sfcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            xcenter(0, 0, -st.d - 90);             break;  /// center, due North
        case "ht": st.show = 0;                    break;  /// hide turtle
        case "st": st.show = 1;                    break;  /// show turtle
        case "ct": xcenter(0, 0, 0);               break;  /// center turtle
        case "pd": st.pen = 1;                     break;  /// pen down
        case "pu": st.pen = 0;                     break;  /// pen up
        case "hd": xform(0, 0, -st.d -(90 - n));   break;  /// set heading, 0=North
        case "fd": xform(n, 0, 0);                 break;  /// forward
        case "bk": xform(-n, 0, 0);                break;  /// backward
        case "rt": xform(0, 0, n);                 break;  /// right turn
        case "lt": xform(0, 0, -n);                break;  /// left turn
        case "pc": xcolor(HSVColor(n));            break;  /// change pen color (HSV)
        case "fg": xcolor(RGBColor(n));            break;  /// change foreground color (RGB)
        case "bg": st.bg = RGBColor(n);            break;  /// background color (RGB)
        case "pw":                                         /// pen width
            st.pw = n;
            sfcPaint.setStrokeWidth(st.pw);        break;
        case "tt": xtext(v1);                      break;
        case "ts": sfcPaint.setTextSize(n);        break;  /// default 12
        case "xy":                                         /// set position
            int x = (n & 0xffff);
            int y = (n >> 16) & 0xffff;
                
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
