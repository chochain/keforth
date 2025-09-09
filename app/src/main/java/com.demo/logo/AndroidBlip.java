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
    private Canvas sfcCanvas, eveCanvas;
    private Paint  sfcPaint,  evePaint;
    private Path   path;
    private float  x0, y0;                  ///< offsets to screen coordinates
    
    public AndroidBlip(Canvas sfc, Canvas eve) {
        this.sfcCanvas = sfc;
        this.eveCanvas = eve;
        
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
    public void init(int w, int h, int fg, int pw, int ts) {
        this.x0 = 0.5f * w;              ///< logical 0,0 at center of canvas
        this.y0 = 0.5f * h;              ///< TODO: use Matirx
        setColor(fg);
        setWidth(pw);
        setTextSize(ts);
    }

    @Override
    public void setColor(int color) {
//        path.rewind();                 /// from Logo1
        sfcPaint.setColor(color);
//        path.moveTo(st.x, st.y);       /// from Logo1
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
        float x1 = x0 + x, y1 = y0 - y;   ///< logical => screen coordinates
        
        if (penDown) path.lineTo(x1, y1);
        else         path.moveTo(x1, y1);
    }
    
    @Override
    public void label(String txt, float x, float y, float angle) {
        float x1 = x0 + x, y1 = y0 - y;  ///< logical => screen coordinate
        
        sfcCanvas.save();
        sfcCanvas.translate(x1, y1);
        sfcCanvas.rotate(angle);
        
        Rect r = new Rect();             ///< boundig box
        sfcPaint.getTextBounds(txt, 0, txt.length(), r);
        sfcCanvas.drawText(txt, -0.5f * r.width(), 0.5f * r.height(), sfcPaint);
        
        sfcCanvas.restore();
    }
    ///
    ///> Turtle shaped like Eve (as in Wall-E)
    ///
    @Override
    public void turtle(float x, float y, float angle, int color, boolean show) {
        final float ANGLE = 30;          ///< startding point of the shoulder
        final float SWEEP = 18;          ///< sweep angle
        final float HEAD  = 24;          ///< head height
        final float SKULL = 4;
        
        if (!show) return;
        
        float x1 = x0 + x, y1 = y0 - y;  ///< logical => screen coordinate
        
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
    public void clear() {
        sfcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        eveCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        path.rewind();
    }
    
    @Override
    public void render() {
        sfcCanvas.drawPath(path, sfcPaint);
        path.rewind();
    }
}

