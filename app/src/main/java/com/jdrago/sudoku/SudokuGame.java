package com.jdrago.sudoku;

public class SudokuGame {
    public class Cell {
        int value;
        boolean locked;

        public Cell(int v, boolean l) {
            value = v;
            locked = l;
        }
    }

    Cell grid[][];

    public SudokuGame() {
        newGame();
    }

    public void clear() {
        grid = new Cell[9][9];
        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                grid[i][j] = new Cell(0, false);
            }
        }
    }

    public void newGame() {
        clear();
        load("0000L7L1L2L80L9L1L8000L70L50L200L5L800L300L50L6L40L70000L70L90000L40L1L30L800L500L8L900L20L60L4000L9L3L70L7L9L6L40000");
    }

    public void poke(int x, int y, int v) {
        Cell cell = grid[x][y];
        if(!cell.locked) {
            cell.value = v;
        }
    }

    public String save() {
        return "";
    }

    public void load(String s) {
        if(s.length() < 1)
            return;

        clear();

        int index = 0;
        boolean locked = false;
        for (int i = 0; i < s.length(); i++) {
            if(index >= 81)
                break;

            char c = s.charAt(i);
            switch(c) {
                case 'L':
                    locked = true;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    int x = index % 9;
                    int y = index / 9;
                    int v = c - '0';
                    grid[x][y].value = v;
                    grid[x][y].locked = locked;
                    locked = false;
                    ++index;
                    break;
            }
        }
    }
}
