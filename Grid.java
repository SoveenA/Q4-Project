import java.io.Serializable;

public class Grid implements Serializable {

    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private int winner; // 1 = black wins, 2 = white wins, 0 = tie
    private boolean[][] validCells; // legal moves for currentPlayer; stamped in by the server
    private DLList<int[]> toFlip;   // cells that just flipped on the latest move; empty for non-move broadcasts

    public Grid() {
        board = new int[8][8];

        // Starting position: 1 = black, 2 = white
        board[3][3] = 2;
        board[3][4] = 1;
        board[4][3] = 1;
        board[4][4] = 2;

        currentPlayer = 1; // black goes first
        validCells = new boolean[8][8];
        toFlip = new DLList<>();
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, int value) {
        board[row][col] = value;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int player) {
        currentPlayer = player;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public boolean isValidCell(int row, int col) {
        return validCells != null && validCells[row][col];
    }

    public void setValidCells(boolean[][] validCells) {
        this.validCells = validCells;
    }

    public DLList<int[]> getToFlip() {
        return toFlip;
    }

    public void setToFlip(DLList<int[]> toFlip) {
        this.toFlip = toFlip;
    }
}