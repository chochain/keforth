///
/// @file
/// @brief - Android Platform-Specific Implementation
///
/// * AndroidTurtle handles all Android Canvas specifics
/// * Coordinate system conversion happens here
/// * Paint and drawing style management isolated
package com.demo.logo;

import android.graphics.*;

public class AndroidTurtle implements Turtle {
    private Canvas sfcCanvas;
    private Canvas eveCanvas;
    private Paint  sfcPaint;
    private Paint  evePaint;
    private Path   path;
    private int    width, height;
    
    public AndroidTurtle(Canvas src, Canvas eve, int w, int h) {
        this.sfcCanvas = src;
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
        float screenX = width * 0.5f + x;
        float screenY = height * 0.5f - y;
        
        if (penDown) {
            path.lineTo(screenX, screenY);
        } else {
            path.moveTo(screenX, screenY);
        }
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
    public void drawText(String text, float x, float y, float angle) {
        float screenX = width  * 0.5f + x;
        float screenY = height * 0.5f - y;
        
        sfcCanvas.save();
        sfcCanvas.translate(screenX, screenY);
        sfcCanvas.rotate(angle);
        
        Rect r = new Rect();
        sfcPaint.getTextBounds(text, 0, text.length(), r);
        sfcCanvas.drawText(text, -0.5f * r.width(), 0.5f * r.height(), sfcPaint);
        
        sfcCanvas.restore();
    }
    
    @Override
    public void drawTurtle(float x, float y, float angle, int color, boolean visible) {
        final float ANGLE = 30;          ///< startding point of the shoulder
        final float SWEEP = 18;          ///< sweep angle
        final float HEAD  = 24;
        final float SKULL = 4;
        if (!visible) return;
        
        float screenX = width  * 0.5f + x;
        float screenY = height * 0.5f - y;
        
        evePaint.setColor(color);
        eveCanvas.save();
        
        eveCanvas.translate(screenX, screenY);
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
    public void show() {
        sfcCanvas.drawPath(path, sfcPaint);
        path.rewind();
    }
}

