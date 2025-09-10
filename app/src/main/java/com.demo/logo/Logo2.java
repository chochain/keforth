///
/// @file
/// @brief - Turtle Graphic Clean View Layer
///
/// * Logo2 becomes a thin orchestration layer
/// * Simply executes commands and triggers redraws
/// * Much easier to understand and maintain
///
/// With the following benifits
/// * Testability: can unit test Engine without any Android context
/// * Performance: Commands can be batched and optimized
/// * Debugging: can log/inspect the command stream
/// * Multiple Backends: Easy to add SVG export, printing, or other renderers
/// * State Management: Clear separation between logical state and visual state
///
package com.demo.logo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.util.AttributeSet;
import java.lang.IllegalStateException;

import com.demo.ui.OutputHandler;
    
public class Logo2 extends View {
    private Bitmap        sfc;                     ///< bitmap for path surface
    private Bitmap        eve;                     ///< bitmap for turtle cursor
    private OutputHandler out;                     ///< for debug tracing
    private Engine        core;                    ///< Logo logic
    private Blip          blip;                    ///< Renderer
    
    public Logo2(Context context, OutputHandler out) {
        super(context);
        this.out = out;
    }
    
    public Logo2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void reset(int w, int h) {
        if (w <= 0 || h <= 0) return;
        
        /// Initialize engine (state) and turtle (views)
        core = new Engine(w, h);

        Engine.State st = core.getState();
        
        sfc  = Bitmap.createBitmap(st.w, st.h, Bitmap.Config.ARGB_8888);
        eve  = Bitmap.createBitmap(st.w, st.h, Bitmap.Config.ARGB_8888);
        blip = new AndroidBlip(sfc, eve);
        
        blip.init(st.w, st.h, st.fg, st.pw, st.ts);

        execute("cs", "0", "0");
    }

    @Override
    protected void onSizeChanged(int w, int h, int w0, int h0) {
        super.onSizeChanged(w, h, w0, h0);
//        throw new IllegalStateException("logo.onSizeChanged");
        reset(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(sfc, 0, 0, null);
        canvas.drawBitmap(eve, 0, 0, null);
    }
    
    public String to_s() {
        return core != null ? core.getState().toString() : "na";
    }
    
    private void doLogo() {
        if (blip == null) return;
        
        for (Engine.Op op : core.getOps()) {  /// dispatch command from queue
            out.log(op.name+" ");
            op.exec(blip);
        }
        core.clearOps();                      /// clear command queue
        out.log("\n");
        
        Engine.State st = core.getState();    /// redraw turtle if visible
        
        blip.draw(st.x, st.y, st.d, st.fg, st.show==1);
        
        invalidate();
    }
    
    public boolean execute(String op, String v1, String v2) {
        if (core == null) return false;

        out.debug("before " + to_s() + "\n");
        boolean t = core.step(op, v1, v2);
        if (t) doLogo();
        out.debug("after  " + to_s() + "\n");
        return t;
    }
}
