///
/// @file
/// @brief - Logo on Android SurfaceView
///
package com.keforth.logo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

public class Logo extends View {
    private Paint paint;

    public Logo(Context context) {
        super(context);
        init();
    }
    public Logo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // Draw a red line
        canvas.drawLine(50, 50, 200, 200, paint);
    }
}
