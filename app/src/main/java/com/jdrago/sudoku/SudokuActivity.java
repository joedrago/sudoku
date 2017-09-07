package com.jdrago.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SudokuActivity extends AppCompatActivity {
    private static String TAG = "SudokuActivity";
    static final String STATE_GAME = "game";

    SudokuView view_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view_ = new SudokuView(this, null);
        setContentView(view_);

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        String gameState = pref.getString(STATE_GAME, "");
        view_.setGameState(gameState);
        Log.d(TAG, "onCreate (" + gameState + ")");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String gameState = view_.gameState();
        editor.putString(STATE_GAME, gameState);
        editor.commit();
        Log.d(TAG, "onSaveInstanceState ("+gameState+")");

        super.onSaveInstanceState(savedInstanceState);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.sudoku, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newgame:
                newGame();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newGame() {
        AlertDialog alertDialog = new AlertDialog.Builder(SudokuActivity.this).create();
        alertDialog.setTitle("New Game?");
        alertDialog.setMessage("Are you sure you want to start a new game?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        view_.newGame();
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
*/
}
