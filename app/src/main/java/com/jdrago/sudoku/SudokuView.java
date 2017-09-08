package com.jdrago.sudoku;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SudokuView extends View {
    private static String TAG = "SudokuView";

    // ----------------------------------------------------------------------------------
    // Render

    static final int VALUE_POS_X = 1;
    static final int VALUE_POS_Y = 10;
    static final int PENCIL_POS_X = 5;
    static final int PENCIL_POS_Y = 10;
    static final int NEWGAME_POS_X = 6;
    static final int NEWGAME_POS_Y = 13;

    static final int COLOR_VALUE = 0xff339999;
    static final int COLOR_PENCIL = 0xff0000ff;

    public enum Style {
        BACKGROUND_SELECTED(0xffeeeeaa, 0, 0),
        BACKGROUND_CONFLICTED(0xffffffdd, 0, 0),
        BACKGROUND_ERROR(0xffffdddd, 0, 0),

        LINE_BLACK_THIN(0xff000000, 0, 0),
        LINE_BLACK_THICK(0xff000000, 0, 1.0f / 15.0f),

        TEXT_VALUE(COLOR_VALUE, 0.8f, 0),
        TEXT_PENCIL(COLOR_PENCIL, 0.3f, 0),
        TEXT_GRID_TITLE(0xff000000, 0.3f, 0),
        TEXT_LOCKED(0xff000000, 0.8f, 0),

        TEXT_BUTTON_VALUE(COLOR_VALUE, 0.8f, 0),
        TEXT_BUTTON_PENCIL(COLOR_PENCIL, 0.8f, 0),
        TEXT_BUTTON_NEWGAME(0xff008833, 0.4f, 0),
        TEXT_BUTTON_CLEAR(0xff008833, 0.4f, 0);

        int color;
        float textScale;
        float lineScale;
        Paint paint;

        private Style(int col, float ts, float ls) {
            color = col;
            textScale = ts;
            lineScale = ls;
            paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStrokeWidth(0.0f);
        }
    }

    float cellSize_;

    // ----------------------------------------------------------------------------------
    // Actions

    public enum ActionType {
        SELECT, PENCIL, VALUE, NEWGAME;
    }

    public class Action {

        private Action(ActionType _type, int _x, int _y) {
            type = _type;
            x = _x;
            y = _y;
        }

        ActionType type;
        int x;
        int y;
    }

    Action actions_[];

    // ----------------------------------------------------------------------------------
    // Game

    SudokuGame game_;
    int selectX_;
    int selectY_;

    // ----------------------------------------------------------------------------------
    // Init

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        cellSize_ = 0;

        initActions();

        game_ = new SudokuGame();
        selectX_ = -1;
        selectY_ = -1;

        calcSizes();
    }

    public void initActions() {
        actions_ = new Action[9 * 15];

        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                int index = (j * 9) + i;
                actions_[index] = new Action(ActionType.SELECT, i, j);
            }
        }

        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 3; ++i) {
                int index = ((VALUE_POS_Y + j) * 9) + (VALUE_POS_X + i);
                actions_[index] = new Action(ActionType.VALUE, 1 + (j * 3) + i, 0);
            }
        }

        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 3; ++i) {
                int index = ((PENCIL_POS_Y + j) * 9) + (PENCIL_POS_X + i);
                actions_[index] = new Action(ActionType.PENCIL, 1 + (j * 3) + i, 0);
            }
        }

        // New Game button
        int index = (NEWGAME_POS_Y * 9) + NEWGAME_POS_X;
        actions_[index] = new Action(ActionType.NEWGAME, 0, 0);

        // Clear value button
        index = ((VALUE_POS_Y+3) * 9) + VALUE_POS_X+1;
        actions_[index] = new Action(ActionType.VALUE, 0, 0);
    }

    // ----------------------------------------------------------------------------------
    // Save / Load

    public String gameState() {
        return game_.save();
    }

    public void setGameState(String state) {
        Log.d(TAG, "setGameState");
        game_.load(state);
    }

    public void newGame() {
        Log.d(TAG, "newGame");

        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("New Game?");
        alertDialog.setMessage("Are you sure you want to start a new game?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        game_.newGame();
                        invalidate();
                        selectX_ = -1;
                        selectY_ = -1;
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    // ----------------------------------------------------------------------------------
    // Render

    protected void calcSizes() {
        float w = getWidth();
        float h = getHeight();
        float smallest = (w < h) ? w : h;

        float newCellSize = smallest / 9.0f;
        if (cellSize_ != newCellSize) {
            cellSize_ = newCellSize;
            for (Style s : Style.values()) {
                s.paint.setTextSize(cellSize_ * s.textScale);
                s.paint.setStrokeWidth(cellSize_ * s.lineScale);
            }
        }
    }

    protected boolean conflicts(int x1, int y1, int x2, int y2) {
        // same row or column?
        if ((x1 == x2) || (y1 == y2))
            return true;

        // same section?
        int sx1 = (x1 / 3) * 3;
        int sy1 = (y1 / 3) * 3;
        int sx2 = (x2 / 3) * 3;
        int sy2 = (y2 / 3) * 3;
        if ((sx1 == sx2) && (sy1 == sy2)) {
            return true;
        }

        return false;
    }

    private final Rect textBounds = new Rect();

    public void drawTextCentered(Canvas canvas, Paint paint, String text, float cx, float cy) {
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), paint);
    }

    protected void drawCell(Canvas canvas, int x, int y, Style backgroundStyle, Style textStyle, String s) {
        float px = x * cellSize_;
        float py = y * cellSize_;
        if (backgroundStyle != null) {
            canvas.drawRect(px, py, px + cellSize_, py + cellSize_, backgroundStyle.paint);
        }
        drawTextCentered(canvas, textStyle.paint, s, px + (cellSize_ / 2), py + (cellSize_ / 2));
    }

    protected void drawGrid(Canvas canvas, int originX, int originY, int size) {
        for (int i = 0; i <= size; ++i) {
            Style style = Style.LINE_BLACK_THIN;
            if ((i % 3) == 0) {
                style = Style.LINE_BLACK_THICK;
            }
            // Horizontal lines
            canvas.drawLine(cellSize_ * (originX + 0), cellSize_ * (originY + i), cellSize_ * (originX + size), cellSize_ * (originY + i), style.paint);

            // Vertical lines
            canvas.drawLine(cellSize_ * (originX + i), cellSize_ * (originY + 0), cellSize_ * (originX + i), cellSize_ * (originY + size), style.paint);
        }
    }

    protected void onDraw(Canvas canvas) {
        calcSizes();

        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                SudokuGame.Cell cell = game_.grid[i][j];

                Style backgroundStyle = null;
                Style textStyle = null;
                String text = "";
                if (cell.value == 0) {
                    textStyle = Style.TEXT_PENCIL;
                    text = cell.pencilString();
                } else {
                    if (cell.locked) {
                        textStyle = Style.TEXT_LOCKED;
                    } else {
                        textStyle = Style.TEXT_VALUE;
                    }
                    if (cell.value > 0)
                        text = Integer.toString(cell.value);
                }
                if ((selectX_ != -1) && (selectY_ != -1)) {
                    if ((i == selectX_) && (j == selectY_)) {
                        backgroundStyle = Style.BACKGROUND_SELECTED;
                    } else if (conflicts(i, j, selectX_, selectY_)) {
                        backgroundStyle = Style.BACKGROUND_CONFLICTED;
                    }
                }
                if(cell.error) {
                    backgroundStyle = Style.BACKGROUND_ERROR;
                }
                drawCell(canvas, i, j, backgroundStyle, textStyle, text);
            }
        }

        SudokuGame.Cell selectedCell = null;
        if ((selectX_ != -1) && (selectY_ != -1)) {
            selectedCell = game_.grid[selectX_][selectY_];
        }
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 3; ++i) {
                int currentValue = (j * 3) + i + 1;
                String currentValueString = Integer.toString(currentValue);

                Style valueBackgroundStyle = null;
                Style pencilBackgroundStyle = null;
                if (selectedCell != null) {
                    if (selectedCell.value == currentValue) {
                        valueBackgroundStyle = Style.BACKGROUND_SELECTED;
                    }
                    if (selectedCell.pencil[currentValue - 1]) {
                        pencilBackgroundStyle = Style.BACKGROUND_SELECTED;
                    }
                }

                drawCell(canvas, VALUE_POS_X + i, VALUE_POS_Y + j, valueBackgroundStyle, Style.TEXT_BUTTON_VALUE, currentValueString);
                drawCell(canvas, PENCIL_POS_X + i, PENCIL_POS_Y + j, pencilBackgroundStyle, Style.TEXT_BUTTON_PENCIL, currentValueString);
            }
        }

        drawGrid(canvas, 0, 0, 9);
        drawGrid(canvas, VALUE_POS_X, VALUE_POS_Y, 3);
        drawGrid(canvas, PENCIL_POS_X, PENCIL_POS_Y, 3);
        drawTextCentered(canvas, Style.TEXT_GRID_TITLE.paint, "Values", ((VALUE_POS_X + 1) * cellSize_) + (cellSize_ / 2), (VALUE_POS_Y * cellSize_) - (cellSize_ / 4));
        drawTextCentered(canvas, Style.TEXT_GRID_TITLE.paint, "Pencil Marks", ((PENCIL_POS_X + 1) * cellSize_) + (cellSize_ / 2), (PENCIL_POS_Y * cellSize_) - (cellSize_ / 4));

        drawCell(canvas, VALUE_POS_X+1, VALUE_POS_Y+3, null, Style.TEXT_BUTTON_CLEAR, "Clear");

        drawCell(canvas, NEWGAME_POS_X, NEWGAME_POS_Y, null, Style.TEXT_BUTTON_NEWGAME, "New");
    }

    // ----------------------------------------------------------------------------------
    // Input

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            calcSizes();

            int x = (int) Math.floor(event.getX() / cellSize_);
            int y = (int) Math.floor(event.getY() / cellSize_);

            if ((x < 9) && (y < 15)) {
                int index = (y * 9) + x;
                Action action = actions_[index];
                if (action != null) {
                    Log.d(TAG, "selecting action " + action.type + " (" + action.x + "," + action.y + ")");
                    switch (action.type) {
                        case SELECT:
                            selectX_ = action.x;
                            selectY_ = action.y;
                            break;
                        case VALUE:
                            if ((selectX_ != -1) && (selectX_ != -1)) {
                                game_.setValue(selectX_, selectY_, action.x);
                            }
                            break;
                        case PENCIL:
                            if ((selectX_ != -1) && (selectX_ != -1)) {
                                game_.togglePencil(selectX_, selectY_, action.x);
                            }
                            break;
                        case NEWGAME:
                            newGame();
                            break;
                    }
                } else {
                    Log.d(TAG, "deselecting");
                    selectX_ = -1;
                    selectY_ = -1;
                }
            }

            invalidate();
        }
        return true;
    }

    // ----------------------------------------------------------------------------------
}
