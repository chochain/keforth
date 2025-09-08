///
/// @file
/// @brief - Android Platform-Specific Implementation
///
/// * AndroidBlip handles all Android Canvas specifics
/// * Coordinate system conversion happens here
/// * Paint and drawing style management isolated
///
package com.demo.logo;

import android.graphics.*;

public class AndroidBlip implements Blip {
    private Canvas sfcCanvas;
    private Canvas eveCanvas;
    private Paint  sfcPaint;
    private Paint  evePaint;
    private Path   path;
    private int    width, height;
    
    public AndroidBlip(Canvas sfc, Canvas eve, int w, int h) {
        this.sfcCanvas = sfc;
        this.eveCanvas = eve;
        this.width     = w;
        this.height    = h;
        
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
    public void clear() {
        sfcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        path.rewind();
    }
    
    @Override
    public void moveTo(float x, float y, boolean penDown) {
        /// Convert logical coordinates to screen coordinates
        float x1 = width  * 0.5f + x;
        float y1 = height * 0.5f - y;
        
        if (penDown) path.lineTo(x1, y1);
        else         path.moveTo(x1, y1);
    }
    
    @Override
    public void setColor(int color) {
        sfcPaint.setColor(color);
    }
    
    @Override
    public void setWidth(int width) {
        sfcPaint.setStrokeWidth(width);
    }
    
    @Override
    public void label(String txt, float x, float y, float angle) {
        float x1 = width  * 0.5f + x;
        float y1 = height * 0.5f - y;
        
        sfcCanvas.save();
        sfcCanvas.translate(x1, y1);
        sfcCanvas.rotate(angle);
        
        Rect r = new Rect();
        sfcPaint.getTextBounds(txt, 0, txt.length(), r);
        sfcCanvas.drawText(txt, -0.5f * r.width(), 0.5f * r.height(), sfcPaint);
        
        sfcCanvas.restore();
    }
    
    @Override
    public void drawTurtle(float x, float y, float angle, int color, boolean show) {
        final float ANGLE = 30;          ///< startding point of the shoulder
        final float SWEEP = 18;          ///< sweep angle
        final float HEAD  = 24;
        final float SKULL = 4;
        
        if (!show) return;
        
        float x1 = width  * 0.5f + x;
        float y1 = height * 0.5f - y;
        
        evePaint.setColor(color);
        eveCanvas.save();
        
        eveCanvas.translate(x1, y1);
        eveCanvas.rotate(angle);
        
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
    
    @Override
    public void render() {
        sfcCanvas.drawPath(path, sfcPaint);
        path.rewind();
    }
}

