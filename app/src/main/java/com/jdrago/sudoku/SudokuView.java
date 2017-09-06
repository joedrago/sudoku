package com.jdrago.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SudokuView extends View {
    private static String TAG = "SudokuView";

    private final float FONT_SCALE = 0.8f;

    public enum Style {
        BLACK(0xff000000),
        BLUE(0xff0000ff),
        RED(0xffff0000),

        SELECTED(0xffeeeeaa),
        THICK(0xff000000); // thick black

        int color;
        Paint paint;

        private Style(int col) {
            color = col;
            paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStrokeWidth(0.0f);
        }
    }

    float cellSize_;
    boolean landscape_;
    int pen_;

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        cellSize_ = 0;
        pen_ = 0;
        calcSizes();
    }

    public String gameState() {
        return "WOO";
    }

    public void setGameState(String state) {
        Log.d(TAG, "setGameState");
    }

    public void newGame() {
        Log.d(TAG, "newGame");
    }

    protected void calcSizes() {
        float w = getWidth();
        float h = getHeight();
        float smallest = (w < h) ? w : h;

        float newCellSize = smallest / 9.0f;
        if (cellSize_ != newCellSize) {
            cellSize_ = newCellSize;
            for (Style s : Style.values()) {
                s.paint.setTextSize(cellSize_ * FONT_SCALE);
            }
            Style.THICK.paint.setStrokeWidth(cellSize_ / 15.0f);
        }

        landscape_ = (w > h);
    }

    private final Rect textBounds = new Rect();

    public void drawTextCentered(Canvas canvas, Paint paint, String text, float cx, float cy) {
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), paint);
    }

    static final int DC_SELECTED = 1 << 0;

    protected void drawCell(Canvas canvas, int x, int y, String s, int flags) {
        float px = x * cellSize_;
        float py = y * cellSize_;
        if ((flags & DC_SELECTED) != 0) {
            canvas.drawRect(px, py, px + cellSize_, py + cellSize_, Style.SELECTED.paint);
        }
        drawTextCentered(canvas, Style.BLACK.paint, s, px + (cellSize_ / 2), py + (cellSize_ / 2));
    }

    protected void onDraw(Canvas canvas) {
        calcSizes();

        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                drawCell(canvas, i, j, Integer.toString(9), 0);
            }
        }

        // Border
        canvas.drawLine(0, 0, cellSize_ * 9.0f, 0, Style.THICK.paint);
        canvas.drawLine(0, 0, 0, cellSize_ * 9.0f, Style.THICK.paint);
        canvas.drawLine(cellSize_ * 9.0f, 0, cellSize_ * 9.0f, cellSize_ * 9.0f, Style.THICK.paint);
        canvas.drawLine(0, cellSize_ * 9.0f, cellSize_ * 9.0f, cellSize_ * 9.0f, Style.THICK.paint);

        // Vertical lines
        canvas.drawLine(cellSize_ * 1, 0, cellSize_ * 1, cellSize_ * 9.0f, Style.BLACK.paint);
        canvas.drawLine(cellSize_ * 2, 0, cellSize_ * 2, cellSize_ * 9.0f, Style.BLACK.paint);
        canvas.drawLine(cellSize_ * 3, 0, cellSize_ * 3, cellSize_ * 9.0f, Style.THICK.paint);
        canvas.drawLine(cellSize_ * 4, 0, cellSize_ * 4, cellSize_ * 9.0f, Style.BLACK.paint);
        canvas.drawLine(cellSize_ * 5, 0, cellSize_ * 5, cellSize_ * 9.0f, Style.BLACK.paint);
        canvas.drawLine(cellSize_ * 6, 0, cellSize_ * 6, cellSize_ * 9.0f, Style.THICK.paint);
        canvas.drawLine(cellSize_ * 7, 0, cellSize_ * 7, cellSize_ * 9.0f, Style.BLACK.paint);
        canvas.drawLine(cellSize_ * 8, 0, cellSize_ * 8, cellSize_ * 9.0f, Style.BLACK.paint);

        // Horizontal lines
        canvas.drawLine(0, cellSize_ * 1, cellSize_ * 9.0f, cellSize_ * 1, Style.BLACK.paint);
        canvas.drawLine(0, cellSize_ * 2, cellSize_ * 9.0f, cellSize_ * 2, Style.BLACK.paint);
        canvas.drawLine(0, cellSize_ * 3, cellSize_ * 9.0f, cellSize_ * 3, Style.THICK.paint);
        canvas.drawLine(0, cellSize_ * 4, cellSize_ * 9.0f, cellSize_ * 4, Style.BLACK.paint);
        canvas.drawLine(0, cellSize_ * 5, cellSize_ * 9.0f, cellSize_ * 5, Style.BLACK.paint);
        canvas.drawLine(0, cellSize_ * 6, cellSize_ * 9.0f, cellSize_ * 6, Style.THICK.paint);
        canvas.drawLine(0, cellSize_ * 7, cellSize_ * 9.0f, cellSize_ * 7, Style.BLACK.paint);
        canvas.drawLine(0, cellSize_ * 8, cellSize_ * 9.0f, cellSize_ * 8, Style.BLACK.paint);

        // Buttons at the bottom
        if (landscape_) {
            for (int i = 0; i < 9; ++i) {
                int flags = 0;
                if(i+1 == pen_) {
                    flags = DC_SELECTED;
                }
                drawCell(canvas, 10, i, Integer.toString(i + 1), flags);
            }
            canvas.drawLine(cellSize_ * 10.0f, 0, cellSize_ * 10.0f, cellSize_ * 9.0f, Style.THICK.paint);
            canvas.drawLine(cellSize_ * 11.0f, 0, cellSize_ * 11.0f, cellSize_ * 9.0f, Style.THICK.paint);
        } else {
            for (int i = 0; i < 9; ++i) {
                int flags = 0;
                if(i+1 == pen_) {
                    flags = DC_SELECTED;
                }
                drawCell(canvas, i, 10, Integer.toString(i + 1), flags);
            }
            canvas.drawLine(0, cellSize_ * 10.0f, cellSize_ * 9.0f, cellSize_ * 10.0f, Style.THICK.paint);
            canvas.drawLine(0, cellSize_ * 11.0f, cellSize_ * 9.0f, cellSize_ * 11.0f, Style.THICK.paint);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            calcSizes();

            int x = (int)Math.floor(event.getX() / cellSize_);
            int y = (int)Math.floor(event.getY() / cellSize_);
            if(landscape_) {
                if(x > 10) {
                    pen_ = 0;
                } else  if(x == 10) {
                    pen_ = y+1;
                }
            } else {
                if(y > 10) {
                    pen_ = 0;
                } else  if(y == 10) {
                    pen_ = x+1;
                }
            }

            invalidate();
        }
        return true;
    }
}
