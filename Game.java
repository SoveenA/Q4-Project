public class Game {

    private Manager manager;
    private Grid grid;
    private MyHashMap<Integer, DLList<int[]>> validMoves;

    private static final int[][] DIRECTIONS = {
            { -1, -1 }, { -1, 0 }, { -1, 1 },
            { 0, -1 }, { 0, 1 },
            { 1, -1 }, { 1, 0 }, { 1, 1 }
    };

    public Game(Manager manager, Grid grid) {
        this.manager = manager;
        this.grid = grid;
        validMoves = new MyHashMap<>();
    }

    // Builds the HashMap of all valid moves for the given player.
    // Key = row * 8 + col, Value = DLList of pieces that would be flipped.
    public void getValidMoves(int player) {
        validMoves = new MyHashMap<>();
        int opponent = (player == 1) ? 2 : 1;

        // for each cell
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid.getCell(r, c) != 0)
                    continue; // skip non-empty cells

                // instansiate list of pieces to flip if this is a valid move
                DLList<int[]> toFlip = new DLList<>();

                // for each direction
                for (int[] dir : DIRECTIONS) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];

                    // list of potential pieces to flip in this direction
                    DLList<int[]> potential = new DLList<>();

                    // walk while we see opponent pieces
                    // while we stay on the board and see opponent pieces
                    while (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && grid.getCell(nr, nc) == opponent) {
                        potential.add(new int[] { nr, nc }); // add to potential flips
                        nr += dir[0];
                        nc += dir[1];
                    }
                    // ends when we go off board or see empty cell or see our own piece

                    // if we ended on our own piece, these are valid flips
                    if (potential.size() > 0 && nr >= 0 && nr < 8 && nc >= 0 && nc < 8
                            && grid.getCell(nr, nc) == player) {
                        for (int i = 0; i < potential.size(); i++) {
                            toFlip.add(potential.get(i));
                        }
                    }
                }

                // Only add to map if at least one piece would be flipped
                if (toFlip.size() > 0) {
                    validMoves.put(r * 8 + c, toFlip);
                }
            }
        }
    }

    // O(1) lookup - returns true if the cell is in the valid moves map
    public boolean isValidMove(int row, int col) {
        return validMoves.get(row * 8 + col) != null;
    }

    // Builds an 8x8 mask of the current valid moves straight from the HashMap.
    // Each cell is an O(1) lookup, so this is just 64 constant-time checks.
    public boolean[][] getValidMovesMask() {
        boolean[][] mask = new boolean[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                mask[r][c] = isValidMove(r, c);
            }
        }
        return mask;
    }

    public int getValidMovesSize() {
        return validMoves.size();
    }

    // Places the piece, flips all captured pieces, rebuilds valid moves, and
    // returns who moves next: the opponent normally, the same player on a pass,
    // 0 if the game just ended, or -1 if the move was illegal (nothing changed).
    public int makeMove(int row, int col, int player) {
        // reject if not a valid move
        if (!isValidMove(row, col)) return -1;

        // place the piece
        grid.setCell(row, col, player);

        // grab the list of pieces to flip from the HashMap (O(1))
        DLList<int[]> toFlip = validMoves.get(row * 8 + col);

        // flip each piece
        for (int i = 0; i < toFlip.size(); i++) {
            int[] cell = toFlip.get(i);
            grid.setCell(cell[0], cell[1], player);
        }

        // stamp the flip list onto the grid so the client can animate exactly
        // these cells before committing to the new board state
        grid.setToFlip(toFlip);

        // can the opponent move?
        int nextPlayer = (player == 1) ? 2 : 1;
        getValidMoves(nextPlayer);
        if (validMoves.size() > 0) {
            return nextPlayer; // normal case: turn passes to opponent
        }

        // opponent has no moves - can the same player go again?
        getValidMoves(player);
        if (validMoves.size() > 0) {
            return player; // opponent passes, same player moves again
        }

        // neither player can move - game over
        int blackCount = 0, whiteCount = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid.getCell(r, c) == 1) blackCount++;
                else if (grid.getCell(r, c) == 2) whiteCount++;
            }
        }

        grid.setGameOver(true);
        if (blackCount > whiteCount)      grid.setWinner(1);
        else if (whiteCount > blackCount) grid.setWinner(2);
        else                              grid.setWinner(0);

        return 0; // nobody moves next
    }

}