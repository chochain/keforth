///
/// @file
/// @brief - Android Platform-Specific Implementation
///
/// * AndroidBlip handles all Android Canvas specifics
/// * Coordinate system conversion happens here
/// * Paint and drawing style management isolated
///
///     o------X Android Coordinate system
///     |\alpha  
///     | \
///     |  \
///     Y
///
package com.keforth.logo;

import android.graphics.*;

public class AndroidBlip implements Blip {
    private Paint  sfcPaint,  evePaint;     ///< Paint attributes
    private Canvas sfcCanvas, eveCanvas;    ///< Logical surfaces
    private Path   path;                    ///< Collections
    private float  x0, y0;                  ///< offsets to screen coordinates
    
    public AndroidBlip(Bitmap sfc, Bitmap eve) {
        sfcCanvas = new Canvas(sfc);        ///< Logo path
        eveCanvas = new Canvas(eve);        ///< Turtle overlay
        sfcPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        evePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        path      = new Path();
        
        sfcPaint.setStyle(Paint.Style.STROKE);
        sfcPaint.setStrokeJoin(Paint.Join.ROUND);
        sfcPaint.setStrokeCap(Paint.Cap.ROUND);
        
        evePaint.setStyle(Paint.Style.STROKE);
        evePaint.setStrokeWidth(3);
        evePaint.setColor(Color.RED);
        evePaint.setStrokeJoin(Paint.Join.ROUND);
        evePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void init(int w, int h, int fg, int pw, int ts) {
        x0 = 0.5f * w;                      ///< logical 0,0 at center of canvas
        y0 = 0.5f * h;                      ///< TODO: use Matirx
        
        setColor(fg);
        setWidth(pw);
        setTextSize(ts);
    }        

    @Override
    public void setColor(int color) {
//        path.rewind();                    /// from Logo1
        sfcPaint.setColor(color);
//        path.moveTo(st.x, st.y);          /// from Logo1
    }
    
    @Override
    public void setWidth(int width) {
        sfcPaint.setStrokeWidth(width);
    }

    @Override
    public void setTextSize(int ts) {
        sfcPaint.setTextSize(ts);
    }
    
    @Override
    public void moveTo(float x, float y, boolean penDown) {
        float x1 = x0 + x, y1 = y0 - y;     ///< logical => screen coordinates
        
        if (penDown) path.lineTo(x1, y1);
        else         path.moveTo(x1, y1);
    }
    
    @Override
    public void label(String txt, float x, float y, float angle) {
        float x1 = x0 + x, y1 = y0 - y;     ///< logical => screen coordinate
        
        sfcCanvas.save();
        sfcCanvas.translate(x1, y1);
        sfcCanvas.rotate(-angle+90);        /// * vertical to path
        
        Rect r = new Rect();                ///< boundig box
        sfcPaint.getTextBounds(txt, 0, txt.length(), r);
        sfcCanvas.drawText(txt, -0.5f * r.width(), 0.5f * r.height(), sfcPaint);
        
        sfcCanvas.restore();
    }
    ///
    ///> Turtle shaped like Eve (as in Wall-E)
    ///
    @Override
    public void draw(float x, float y, float angle, int color, boolean show) {
        sfcCanvas.drawPath(path, sfcPaint); ///< draw collected path
        path.rewind();
        moveTo(x, y, false);
        
        if (!show) return;                  ///< skip turtle
        
        drawTurtle(x, y, angle, color);
    }

    @Override
    public void clear() {                   ///< clean canvas
        sfcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        path.rewind();
    }
    
    private void drawTurtle(float x, float y, float angle, int color) {
        final float ANGLE = 30;             ///< startding point of the shoulder
        final float SWEEP = 18;             ///< sweep angle
        final float HEAD  = 24;             ///< head height
        final float SKULL = 4;
        
        float x1 = x0 + x, y1 = y0 - y;     ///< logical => screen coordinate
        
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        
        evePaint.setColor(color);
        eveCanvas.save();
        
        eveCanvas.translate(x1, y1);
        eveCanvas.rotate(-angle);
        
        Path  eve = new Path();
        eve.addCircle(HEAD, 0, SKULL, Path.Direction.CW);
        
        RectF r = new RectF(-20, -20, 20, 20);
        eve.addArc(r, -ANGLE, SWEEP);
        eve.lineTo(0, 0);
        eve.addArc(r,  ANGLE, -SWEEP);
        eve.lineTo(0, 0);
        
        eveCanvas.drawPath(eve, evePaint);
        eveCanvas.restore();
    }
}

