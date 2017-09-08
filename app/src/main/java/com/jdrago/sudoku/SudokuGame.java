package com.jdrago.sudoku;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SudokuGame {
    public class Cell {
        int value;
        boolean locked;
        boolean error;
        boolean pencil[];

        public Cell(int v, boolean l) {
            value = v;
            locked = l;
            error = false;
            pencil = new boolean[9];
        }

        public String pencilString() {
            String s = "";
            for (int i = 0; i < 9; ++i) {
                if (pencil[i]) {
                    s += Integer.toString(i + 1);
                }
            }
            return s;
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

    public void clear(int x, int y) {
        grid[x][y] = new Cell(0, false);
        updateCells();
    }

    public void newGame() {
        clear();

        SudokuGenerator generator = new SudokuGenerator();
        int board[][] = generator.nextBoard(SudokuGenerator.Difficulty.EASY);
        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                if (board[i][j] != 0) {
                    grid[i][j].value = board[i][j];
                    grid[i][j].locked = true;
                }
            }
        }

//        loadOld("0000L7L1L2L80L9L1L8000L70L50L200L5L800L300L50L6L40L70000L70L90000L40L1L30L800L500L8L900L20L60L4000L9L3L70L7L9L6L40000");
    }

    public void setValue(int x, int y, int v) {
        Cell cell = grid[x][y];
        if (!cell.locked) {
            cell.value = v;
        }
        updateCells();
    }

    public void togglePencil(int x, int y, int v) {
        Cell cell = grid[x][y];
        if (!cell.locked) {
            cell.pencil[v - 1] = !cell.pencil[v - 1];
        }
        updateCells();
    }

    public void updateCell(int x, int y) {
        Cell cell = grid[x][y];

//        boolean taken[] = new boolean[9];
        for (int i = 0; i < 9; ++i) {
            if (x != i) {
                int v = grid[i][y].value;
                if (v > 0) {
//                    taken[v - 1] = true;
                    if (v == cell.value) {
                        grid[i][y].error = true;
                        cell.error = true;
                    }
                }
            }
            if (y != i) {
                int v = grid[x][i].value;
                if (v > 0) {
//                    taken[v - 1] = true;
                    if (v == cell.value) {
                        grid[x][i].error = true;
                        cell.error = true;
                    }
                }
            }
        }

        int sx = (x / 3) * 3;
        int sy = (y / 3) * 3;
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 3; ++i) {
                if ((x != (sx + i)) && (y != (sy + j))) {
                    int v = grid[sx + i][sy + j].value;
                    if (v > 0) {
//                        taken[v - 1] = true;
                        if (v == cell.value) {
                            grid[sx + i][sy + j].error = true;
                            cell.error = true;
                        }
                    }
                }
            }
        }
    }

    public void updateCells() {
        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                grid[i][j].error = false;
            }
        }
        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                updateCell(i, j);
            }
        }
    }

    public String save() {
        String savedata = "";
        try {
            JSONArray cells = new JSONArray();
            for (int j = 0; j < 9; ++j) {
                for (int i = 0; i < 9; ++i) {
                    Cell cell = grid[i][j];
                    JSONObject jcell = new JSONObject();
                    if (cell.locked)
                        jcell.put("l", 1);
                    jcell.put("v", cell.value);
                    boolean needsPencil = false;
                    for (int p = 0; p < 9; ++p) {
                        if (cell.pencil[p]) {
                            needsPencil = true;
                            break;
                        }
                    }
                    if (needsPencil) {
                        JSONArray jpencil = new JSONArray();
                        for (int p = 0; p < 9; ++p)
                            jpencil.put(cell.pencil[p] ? 1 : 0);
                        jcell.put("p", jpencil);
                    }
                    cells.put(jcell);
                }
            }
            JSONObject o = new JSONObject();
            o.put("cells", cells);
            savedata = o.toString();
        } catch (JSONException e) {
        }
        return savedata;
    }

    public void load(String s) {
        if (s.length() < 1)
            return;

        clear();

        try {
            JSONObject o = new JSONObject(s);
            JSONArray cells = o.getJSONArray("cells");
            for (int j = 0; j < 9; ++j) {
                for (int i = 0; i < 9; ++i) {
                    Cell cell = grid[i][j];
                    JSONObject jcell = cells.getJSONObject((j * 9) + i);
                    int value = jcell.optInt("v", 0);
                    cell.value = value;
                    int locked = jcell.optInt("l", 0);
                    if (locked == 1) {
                        cell.locked = true;
                    }
                    JSONArray jpencil = jcell.optJSONArray("p");
                    if (jpencil != null) {
                        for (int p = 0; p < 9; ++p) {
                            int pset = jpencil.optInt(p, 0);
                            if (pset == 1) {
                                cell.pencil[p] = true;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            newGame();
        }
        updateCells();
    }

    public boolean[] done() {
        boolean d[] = new boolean[9];
        int counts[] = new int[9];
        for (int j = 0; j < 9; ++j) {
            for (int i = 0; i < 9; ++i) {
                if(grid[i][j].value != 0) {
                    ++counts[grid[i][j].value-1];
                }
            }
        }
        for(int i = 0; i < 9; ++i) {
            if(counts[i] == 9) {
                d[i] = true;
            }
        }
        return d;
    }

    public void loadOld(String s) {
        if (s.length() < 1)
            return;

        clear();

        int index = 0;
        boolean locked = false;
        for (int i = 0; i < s.length(); i++) {
            if (index >= 81)
                break;

            char c = s.charAt(i);
            switch (c) {
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
        updateCells();
    }
}
