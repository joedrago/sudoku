package com.jdrago.sudoku;

// Pilfered/adapted from https://github.com/tbourvon/sudoku/blob/master/Generator.java
// Copyrights / ownership kept intact.


/*
 * Copyright (c) 2015 Alexane Rose, Etienne Casanova, Ewen Fagon and Tristan Bourvon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SudokuGenerator {

    public class Board {

        static final int SIZE = 9; // Must be a squared number

        private int [][] boardNumbers = new int [SIZE][SIZE];
        private boolean [][] boardConst = new boolean [SIZE][SIZE];

        public Board() {

        }

        /**
         * Copy constructor.
         * @param otherBoard Board to make a copy from.
         */
        public Board(Board otherBoard) {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    boardNumbers[i][j] = otherBoard.boardNumbers[i][j];
                    boardConst[i][j] = otherBoard.boardConst[i][j];
                }
            }
        }

        /**
         * Converts a board to string, mostly used for display, but could also be used
         * for serialization (storing in a file or a db, for example).
         * @return A string representation of the board.
         */
        public String toString() {
            String str = "\n";
            for (int i = 0; i < Board.SIZE; i++) {
                if (i%((int)Math.sqrt(Board.SIZE))==0) {
                    str += addLine(true);
                } else {
                    str += addLine(false);
                }

                for (int j = 0; j < Board.SIZE; j++) {
                    if(j%((int)Math.sqrt(Board.SIZE))==0) {
                        str += "| ";
                    } else {
                        str += "  ";
                    }

                    if (getNumber(j,i) == 0 ) {
                        str += "  ";
                    } else if (getNumber(j, i) == 10){
                        str += "X ";
                    } else {
                        str += getNumber(j,i) + " ";
                    }
                }
                str += "|" + "\n";
            }
            str += addLine(true);

            return str;
        }

        /**
         * REturns a separating row-wise line depending on the boolean passed.
         * If the bool is true, then the line is a square separation line.
         * Else, it is just a regular filling line.
         * @param show Determines if we generate a square line or a filling line.
         * @return A String representation of the line.
         */
        private String addLine(boolean show) {
            String str = "";
            for ( int j = 0; j < Board.SIZE; j++) {
                if (show) {
                    if ( j%((int)Math.sqrt(Board.SIZE))==0 ) {
                        str += "+---";
                    } else {
                        str += "----";
                    }
                } else {
                    if ( j%((int)Math.sqrt(Board.SIZE))==0 ) {
                        str += "|   ";
                    } else {
                        str += "    ";
                    }
                }
            }

            if (show) {
                str += "+" + "\n";
            } else {
                str += "|" + "\n";
            }

            return str;
        }

        public void setNumber ( int x, int y, int num ) {
            boardNumbers[y][x] = num;
        }

        public void setConst( int x, int y, boolean isConstant) {
            boardConst[y][x] = isConstant;
        }

        public int getNumber ( int x, int y ) {
            return boardNumbers[y][x];
        }

        public boolean isConst( int x, int y ) {
            return boardConst[y][x];
        }

        public void eraseNumber ( int x, int y ) {
            setNumber(x, y, 0);
        }

        /**
         * Checks if the board is solved or not.
         * @return If the board is solved or not.
         */
        public boolean isSolved() {
            for (int i=0; i<Board.SIZE;i++){
                for (int j=0; j<Board.SIZE;j++){
                    if  (getNumber(i,j)==0 ) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public class Solver {

        private ArrayList< ArrayList< Set<Integer> >> testedNumbers = new ArrayList< ArrayList< Set<Integer> >>(Board.SIZE);

        private Board board;

        private int current_x = 0;
        private int current_y = 0;

        private boolean lastBacktrack = false;

        Solver(Board b) {
            for ( int i = 0; i< Board.SIZE; i++) {
                testedNumbers.add(i, new ArrayList < Set<Integer>>(Board.SIZE));
                for ( int j = 0; j < Board.SIZE; j++ ) {
                    testedNumbers.get(i).add(j, new HashSet<Integer> ());
                }
            }

            board = b;
        }

        /**
         * Checks if a number can be added at a certain position in the board.
         * @param x The x coordinate of the cell to be checked.
         * @param y The y coordinate of the cell to be checked.
         * @param num The number to check in the cell.
         * @return If the cell can be set to this number.
         */
        private boolean isValid ( int x, int y, int num ) {
            // First we check in the column
            for (int i = 0; i < Board.SIZE; i++) {
                if (i == y)
                    continue; // Jump to next iteration if we are on our cell, ignoring any further instruction

                if (board.getNumber(x, i) == num)
                    return false;
            }


            // Then we check the rows
            for (int i = 0; i < Board.SIZE; i++) {
                if (i == x)
                    continue; // Jump to next iteration if we are on our cell, ignoring any further instruction

                if (board.getNumber(i, y) == num)
                    return false;
            }

            // Finally, we check the squares
            final int square_x = x / (int)Math.sqrt(Board.SIZE);
            final int square_y = y / (int)Math.sqrt(Board.SIZE);

            final int start_x = square_x * (int)Math.sqrt(Board.SIZE);
            final int start_y = square_y * (int)Math.sqrt(Board.SIZE);

            final int end_x = (square_x + 1) * (int)Math.sqrt(Board.SIZE);
            final int end_y = (square_y + 1) * (int)Math.sqrt(Board.SIZE);

            for (int i = start_x; i < end_x; i++) {
                for (int j = start_y; j < end_y; j++) {
                    if (x == i && y == j)
                        continue; // Jump to next iteration if we are one our cell, ignoring any further instruction

                    if (board.getNumber(i, j) == num)
                        return false;
                }
            }

            return true; // We never returned false, hence we can safely return true
        }

        /**
         * Returns whether the specified cell is empty.
         * @param x The x coordinate of the cell to be checked.
         * @param y The y coordinate of the cell to be checked.
         * @return If the cell is empty or not.
         */
        private boolean isEmpty ( int x, int y ) {
            return (board.getNumber(x, y) == 0);
        }

        /**
         * Executes the next solving step in the solving process.
         * If we find a const, we either advance, or backtrack if the last action was a backtrack.
         * If we find an empty cell, we just fill it with a random number.
         * If we find an non-empty cell, we try to fill it with another random number.
         * In case there is no number left to try, we backtrack.
         * @return true if the sudoku is solved or if it is impossible, else returns false.
         */
        public boolean nextStep () {
            if (board.isConst(current_x, current_y)) {
                if (lastBacktrack)
                    return backtrack();
                lastBacktrack = false;

                return advanceCursor();
            } else {
                if (board.getNumber(current_x, current_y) == 0) {
                    int a;
                    a = (int)(Math.random()*Board.SIZE+1);
                    board.setNumber(current_x, current_y, a);
                    testedNumbers.get(current_y).get(current_x).add (a);
                    return false;
                }

                if (isValid(current_x, current_y, board.getNumber(current_x, current_y)) && !lastBacktrack)
                    return advanceCursor();
                else {
                    if (testedNumbers.get(current_y).get(current_x).size() == Board.SIZE) {
                        lastBacktrack = true;
                        board.setNumber(current_x, current_y, 0);
                        testedNumbers.get(current_y).get(current_x).clear();
                        return backtrack();
                    } else {
                        lastBacktrack = false;
                        int a;
                        while (true){
                            a = (int)(Math.random()*Board.SIZE+1);
                            if (!testedNumbers.get(current_y).get(current_x).contains (a)){
                                break;
                            }
                        }
                        board.setNumber(current_x, current_y, a);
                        testedNumbers.get(current_y).get(current_x).add (a);
                    }
                }
            }

            return false;
        }

        /**
         * Advances the cursor by one cell, taking into account rows and columns.
         * @eturns true if at the end, else false.
         */
        private boolean advanceCursor() {
            if (current_x == Board.SIZE - 1) {
                if (current_y == Board.SIZE - 1)
                    return true;

                current_x = 0;
                current_y++;
            } else {
                current_x++;
            }

            return false;
        }

        /**
         * Backtracks the cursor by one cell, taking into account rows and columns.
         * @return true if at the beginning, else false.
         */
        private boolean backtrack() {
            if (current_x == 0) {
                if (current_y == 0)
                    return true;

                current_x = Board.SIZE - 1;
                current_y--;
            } else {
                current_x--;
            }

            return false;
        }

        public Board getBoard () {
            return board;
        }

        /**
         * Helper function to execute all steps until one returns true.
         * Leaves the board in a solved, or any state if impossible.
         */
        public void solveBoard () {
            while (!nextStep());
        }


    }

    /**
     * We solve the sudokus 5 times to check for unicity.
     */
    private static final int UNICITY_ITERATIONS = 5;

    /**
     * We define the defficulty that can only take the following difficulty values: EASY, MEDIUM, HARD, EXTREME.
     */
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        EXTREME
    }

    /**
     * Method used to generate a Sudoku. We first generate a complete board. Then we delete random numbers, and verify that
     * the sudoku still posseses a unique solution. We stop when the solution is no longer unique. According to the difficulty
     * chosen, we return a board with different amounts of numbers on the board.
     * @param difficulty The difficulty of the sudoku to be generated.
     * @return The generated sudoku.
     */
    public Board generateSudoku (Difficulty difficulty) {
        int lowestSudoku = Board.SIZE * Board.SIZE;
        int attempts = 0;

        while (true) {
            ArrayList < Board > steps = new ArrayList < Board > ();

            Board generatedSudoku = new Board();
            Solver solver = new Solver ( generatedSudoku );
            solver.solveBoard();

            for (int i = 0 ; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    generatedSudoku.setConst(i, j, true);
                }
            }

            steps.add(generatedSudoku);

            while(true){
                Board b = new Board(generatedSudoku);
                b = deleteNumber(b);
                if (verifyUnicity(b)) {
                    steps.add(b);
                    generatedSudoku = b;
                } else {
                    break;
                }
            }

            // We display a progress log.
            attempts++;
            if (lowestSudoku > (Board.SIZE * Board.SIZE - steps.size())) {
                lowestSudoku = (Board.SIZE * Board.SIZE - steps.size());
                System.out.println("\nFound a sudoku with " + lowestSudoku + " (" + steps.size() + " numbers removed).");
                System.out.print("Tried " + attempts + " sudokus.");
            } else {
                System.out.print("\rTried " + attempts + " sudokus.");
            }

            if (steps.size() > (Board.SIZE * Board.SIZE) / 1.5) {
                if (difficulty == Difficulty.EXTREME) {
                    return steps.get(steps.size() - 1);
                }
            }
            if (steps.size() > (Board.SIZE * Board.SIZE) / 1.6) {
                if (difficulty == Difficulty.HARD) {
                    return steps.get(steps.size() - 1);
                } else if (difficulty == Difficulty.MEDIUM) {
                    return steps.get((int)((steps.size() - 1) * 0.72));
                } else if (difficulty == Difficulty.EASY) {
                    return steps.get((int)((steps.size() - 1) * 0.44));
                }
            }
            if (steps.size() > (Board.SIZE * Board.SIZE) / 2.3) {
                if (difficulty == Difficulty.MEDIUM) {
                    return steps.get((int)((steps.size() - 1) * 0.95));
                } else if (difficulty == Difficulty.EASY) {
                    return steps.get((int)((steps.size() - 1) * 0.6));
                }
            }
            if (steps.size() > (Board.SIZE * Board.SIZE) / 3.2) {
                if (difficulty == Difficulty.EASY) {
                    return steps.get((int)((steps.size() - 1) * 0.8));
                }
            }
        }
    }

    /**
     * Method used to verify that a Sudoku posseses a unique solution.
     * @param b The board we need to verify the unicity of.
     * @return If the board possesses a unique solution.
     */
    public boolean verifyUnicity (Board b ) {
        Board[] boards = new Board[UNICITY_ITERATIONS];

        for ( int i = 0; i< boards.length; i++ ){
            Board board = new Board(b);
            Solver solver = new Solver(board);
            solver.solveBoard();
            boards[i] = board;
        }

        for (int i = 1; i < boards.length; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                for (int k = 0; k < Board.SIZE; k++) {
                    if (boards[i].getNumber(j, k) != boards[0].getNumber(j, k))
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * Deletes a random number in the board.
     * @param b The board we need to delete a number from.
     * @return The board with the deleted number.
     */
    public Board deleteNumber (Board b) {
        Board board = new Board(b);
        int x;
        int y;
        while (true){
            x = (int)(Math.random()*Board.SIZE);
            y = (int)(Math.random()*Board.SIZE);
            if (board.getNumber(x, y) != 0 ) {
                board.setNumber(x, y, 0);
                board.setConst(x, y, false);
                break;
            }
        }

        return board;
    }

}
