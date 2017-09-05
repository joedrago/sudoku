package com.jdrago.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SudokuView extends View {
    private static String TAG = "SudokuView";

    Paint paint_;
    int canvasW_;
    int canvasH_;
    boolean drawCircle_;

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_.setColor(0xff101010);

        drawCircle_ = false;
    }

    protected void onDraw(Canvas canvas) {
        if (drawCircle_) {
            int w = getWidth();
            int h = getHeight();
            canvas.drawCircle(w / 2, h / 2, w / 2, paint_);
        }
    }

    /*
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        invalidate(); // unnecessary?
    }
    */

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            drawCircle_ = !drawCircle_;
            invalidate();
        }
        return true;
    }
}
