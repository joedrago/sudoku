package com.jdrago.sudoku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SudokuActivity extends AppCompatActivity {

    SudokuView view_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view_ = new SudokuView(this, null);
        setContentView(view_);
    }
}
