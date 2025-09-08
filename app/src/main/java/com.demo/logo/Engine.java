///
/// @file
/// @brief - Pure Logic Layer (TurtleEngine)
///     No Android dependencies - can be unit tested easily
///     Handles all turtle state and coordinate calculations
///     Generates abstract drawing commands instead of direct rendering
///     Can be reused across different platforms (Desktop Java, Web, etc.)
///     You can unit test Engine without any Android context
///
package com.demo.logo;

import java.util.List;
import java.util.ArrayList;

public class Engine {
    private static final double RAD = Math.PI / 180.0;
    
    public static class State {
        public float x, y;               ///< current position (relative to center)
        public int   w, h;               ///< viewport dimensions
        public float d    = 0;           ///< direction in degrees, 0=East
        public int   pen  = 1;           ///< 0: pen-up, 1: pen-down  
        public int   pw   = 3;           ///< pen width
        public int   show = 0;           ///< 0: hide turtle, 1: show turtle
        public int   fg   = 0xFFFFFFFF;  ///< foreground color (white)
        public int   bg   = 0xFF00FF00;  ///< background color (green)
        
        public State(int w, int h) {
            this.w = w;
            this.h = h;
            reset();
        }
        
        public void reset() {
            x = y = 0;
            d = -90;                     /// North
        }
        
        @Override
        public String toString() {
            return String.format(
                "{x:%.1f, y:%.1f, w:%d, h:%d, d:%.2f, pw:%d, pen:%d, show:%d}",
                x, y, w, h, d, pw, pen, show);
        }
    }
    
    /// Drawing commands that will be executed by the renderer
    public static abstract class Op {
       public abstract void exec(Renderer r);
    }
    
    public static class OpMove extends Op {
        public final float   x, y;
        public final boolean penDown;
        
        public OpMove(float x, float y, boolean penDown) {
            this.x = x;
            this.y = y;
            this.penDown = penDown;
        }
        
        @Override
        public void exec(Renderer r) {
            r.moveTo(x, y, penDown);
        }
    }
    
    public static class OpColor extends Op {
        public final int c;
        public OpColor(int c)        { this.c = c; }
        @Override
        public void exec(Renderer r) { r.setColor(c); }
    }
    
    public static class OpWidth extends Op {
        public final int w;
        public OpWidth(int w)        { this.w = w; }
        @Override
        public void exec(Renderer r) { r.setWidth(w); }
    }
    
    public static class OpLabel extends Op {
        public final String txt;
        public final float  x, y, a;
        public OpLabel(String txt, float x, float y, float angle) {
            this.txt = txt;
            this.x   = x;
            this.y   = y;
            this.a   = angle;
        }
        @Override
        public void exec(Renderer r) { r.label(txt, x, y, a); }
    }
    
    public static class OpClear extends Op {
        @Override
        public void exec(Renderer r) { r.clear(); }
    }
    
    /// The engine state and command generation
    private State    st;
    private List<Op> ops = new ArrayList<>();
    
    public Engine(int w, int h) {
        this.st = new State(w, h);
//        add(new OpMove(st.x, st.y, false));
    }
    
    private void     add(Op op) { ops.add(op); }
    
    public  List<Op> getOps()   { return new ArrayList<>(ops); }
    public  void     clearOps() { ops.clear(); }
    public  State    getState() { return st; }
    
    /// Color conversion utilities
    public static int RGBColor(int v) {
        return v | 0xff000000;
    }
    
    public static int HSVColor(int h) {
        int i = (int)Math.floor(h * 0.06);
        double s = 1.0, v = 1.0;
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
        return (0xFF << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
    
    /// Turtle operations - pure logic, no rendering
    private void xform(int dx, int dy, float delta) {
        st.d += delta;
        float c = (float)Math.cos(st.d * RAD);
        float s = (float)Math.sin(st.d * RAD);
        
        st.x += c * dx - s * dy;
        st.y += s * dx + c * dy;
        
        add(new OpMove(st.x, st.y, st.pen == 1));
    }
    
    public boolean exec(String op, String v1, String v2) {
        int n = op.equals("tt") ? 0 : Integer.parseInt(v1);
        
        switch (op) {
        case "cs":                                       /// clear screen
            ops.clear();
            st.reset();
            add(new OpClear());
            add(new OpMove(st.x, st.y, false)); break;
        case "ht": st.show = 0;                 break;   /// hide turtle
        case "st": st.show = 1;                 break;   /// show turtle
        case "ct":                                       /// center turtle
            st.x = st.y = 0;
            add(new OpMove(0, 0, false));       break; 
        case "pd": st.pen = 1;                  break;   /// pen down
        case "pu": st.pen = 0;                  break;   /// pen up
        case "hd": st.d = n - 90;               break;   /// set heading
        case "fd": xform(n, 0, 0);              break;   /// forward
        case "bk": xform(-n, 0, 0);             break;   /// backward  
        case "rt": xform(0, 0, n);              break;   /// right turn
        case "lt": xform(0, 0, -n);             break;   /// left turn
        case "pc":                                       /// pen color (HSV)
            st.fg = HSVColor(n);
            add(new OpColor(st.fg));            break;
        case "fg":                                       /// foreground color (RGB)
            st.fg = RGBColor(n);
            add(new OpColor(st.fg));            break;
        case "bg": st.bg = RGBColor(n);         break;
            
        case "pw":
            st.pw = n;
            add(new OpWidth(n));                break;   /// pen width
        case "tt": /// text
            String s = v1.substring(1, v1.length()-1);   /// remove quotes
            add(new OpLabel(s, st.x, st.y, st.d + 90));
            break;
        case "ts": /* in renderer */            break;   /// text size - handled by renderer
        case "xy":                                       /// set position
            int x = (n & 0xffff);
            int y = (n >> 16) & 0xffff;
                
            if ((x & 0x8000) != 0) x |= 0xffff0000;
            if ((y & 0x8000) != 0) y |= 0xffff0000;
                
            st.x = x;
            st.y = -y;                                   /// Flip Y
            add(new OpMove(st.x, st.y, st.pen == 1));
            break;
        default: return false;
        }
        return true;
    }
}
