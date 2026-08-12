public class Manager {
    private MyArrayList<ServerThread> threads = new MyArrayList<ServerThread>();
    private boolean gameStarted;
    private boolean[] ready = new boolean[]{false, false}; // ready[0] = player 1, ready[1] = player 2
    private Game game;
    private ServerScreen serverScreen;
    private Grid grid;
    private int currentPlayer; // 1 = black, 2 = white

    public Manager(ServerScreen serverScreen) {
        this.serverScreen = serverScreen;
        gameStarted = false;
        grid = new Grid();
        game = new Game(this, grid);
        currentPlayer = 1; // black goes first
        game.getValidMoves(currentPlayer); // build initial valid moves
    }

    public void add(ServerThread st) {
        threads.add(st);
        st.setPlayerNumber(threads.size());
        serverScreen.repaint();
    }

    public int getPlayerCount() {
        return threads.size();
    }

    public Grid getGrid() {
        return grid;
    }

    // Called by ServerThread when a move comes in from a client
    public void playerMove(int row, int col, int playerNumber) {
        if (grid.isGameOver()) return;
        if (playerNumber != currentPlayer) return;

        int next = game.makeMove(row, col, currentPlayer);

        // -1 = illegal move, nothing changed, so don't bother broadcasting.
        // 0 = game just ended (leave currentPlayer as-is, but still broadcast
        // so clients see the end-game grid). Otherwise it's the next player.
        if (next == -1) return;
        if (next != 0) {
            currentPlayer = next;
        }
        broadcastGrid();
    }

    // Send the grid to every connected client
    public void broadcastGrid() {
        grid.setCurrentPlayer(currentPlayer);
        grid.setValidCells(game.getValidMovesMask()); // hints for the current player
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).sendGrid(grid);
        }
        // Clear the flip list AFTER sending so the next broadcast (initial
        // connect, ready-up, endgame cheat, reset) doesn't re-send stale flips.
        // A move broadcast carries the list because makeMove stamped it just
        // before broadcastGrid ran.
        grid.setToFlip(new DLList<>());
    }

    public void readyUp(int playerNumber, boolean isReady) {
        ready[playerNumber - 1] = isReady;

        // Start game only when both players are ready
        if (ready[0] && ready[1]) {
            gameStarted = true;
            broadcastMessage("start");
            broadcastGrid();
        }
    }

    public void endGame() {
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

        broadcastGrid();
    }

    // Reset everything back to a fresh game and send all players back to the
    // start (lobby) screen. This mirrors the work done in the constructor.
    public void resetGame() {
        grid = new Grid();
        game = new Game(this, grid);   // Game holds a ref to grid, so rebuild it with the new one
        currentPlayer = 1;             // black goes first again
        game.getValidMoves(currentPlayer);
        ready[0] = false;
        ready[1] = false;
        gameStarted = false;
        broadcastMessage("reset");     // tell clients to drop back to the start screen
        broadcastGrid();               // send the fresh board
    }

    // Send a String message to every connected client
    public void broadcastMessage(String message) {
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).sendMessage(message);
        }
    }
}