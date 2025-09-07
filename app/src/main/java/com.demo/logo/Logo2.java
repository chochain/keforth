///
/// @file
/// @brief - Turtle Graphic Clean View Layer
///
/// * Logo2 becomes a thin orchestration layer
/// * Simply executes commands and triggers redraws
/// * Much easier to understand and maintain
///
/// With the following benifits
/// * Testability: You can unit test TurtleEngine without any Android context
/// * Performance: Commands can be batched and optimized
/// * Debugging: You can log/inspect the command stream
/// * Multiple Backends: Easy to add SVG export, printing, or other renderers
/// * State Management: Clear separation between logical state and visual state
package com.demo.logo;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.util.AttributeSet;
    
public class Logo2 extends View {
    private Engine core;
    private Turtle eve;
    private Bitmap sfcBitmap, eveBitmap;
    private Canvas sfcCanvas, eveCanvas;
    
    public Logo2(Context context) {
        super(context);
    }
    
    public Logo2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int w0, int h0) {
        super.onSizeChanged(w, h, w0, h0);
        if (w <= 0 || h <= 0) return;
        
        /// Create bitmaps and canvases
        sfcBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        eveBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        sfcCanvas = new Canvas(sfcBitmap);
        eveCanvas = new Canvas(eveBitmap);
        
        /// Initialize engine (state) and turtle (views)
        core = new Engine(w, h);
        eve  = new AndroidTurtle(sfcCanvas, eveCanvas, w, h);
        
        /// Execute initial commands
        doLogo();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sfcBitmap == null || eveBitmap == null) return;
        
        canvas.drawBitmap(sfcBitmap, 0, 0, null);
        canvas.drawBitmap(eveBitmap, 0, 0, null);
    }
    
    private void doLogo() {
        if (eve == null) return;
        
        /// Clear turtle layer
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        
        /// Execute all pending commands
        for (Engine.Op op : core.getOps()) {
            op.exec(eve);
        }
        
        /// Draw turtle if visible
        Engine.State st = core.getState();
        if (st.show==1) eve.draw(st.x, st.y, st.d, st.fg);
        
        /// Finish rendering
        eve.update();
    }
    
    public String to_s() {
        return core != null ? core.getState().toString() : "";
    }
    
    public boolean update(String op, String v1, String v2) {
        if (core == null) return false;
        
        boolean result = core.exec(op, v1, v2);
        if (result) {
            doLogo();
            invalidate();
        }
        return result;
    }
    
    public void reset() {
        if (core != null) {
            core.exec("cs", "", "");
            doLogo();
            invalidate();
        }
    }
}
