import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ClientScreen extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
    private ObjectOutputStream outObj;
    private ObjectInputStream inObj;
    private int mouseX;
    private int mouseY;
    private JButton startButton;
    private JButton seeBoardButton;
    private JButton playAgainButton;
    private boolean gameStarted = false;
    private boolean seeBoard = false;
    private Grid grid;
    private int playerNumber = 0;
    private int cellSize = 80;
    private int offsetX = 100;
    private int offsetY = 100;
    private boolean lossTrigger = true;
    private boolean victoryTrigger = true;
    private int lastPieceCount = -1;
    private BufferedImage background;
    private boolean[][] flippingMask;
    private DLList<int[]> flipCells; // cells that just flipped on the latest move; empty for non-move broadcasts
    private javax.swing.Timer flipTimer;
    private int flipTickCount;
    private static final int FLIP_TICKS = 20;       // total frames in the flip animation
    private static final int FLIP_DELAY_MS = 15;    // ms between frames 

    public ClientScreen() {
        flipCells = new DLList<>();
        mouseX = 0;
        mouseY = 0;

        this.setLayout(null);

        startButton = new JButton("Ready");
        startButton.setBounds(445, 770, 110, 44);
        this.add(startButton);
        startButton.addActionListener(this);

        seeBoardButton = new JButton("See Board");
        seeBoardButton.setBounds(450, 550, 120, 40);
        seeBoardButton.addActionListener(this);
        seeBoardButton.setVisible(false);
        this.add(seeBoardButton);

        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds(450, 610, 120, 40);
        playAgainButton.addActionListener(this);
        playAgainButton.setVisible(false);
        this.add(playAgainButton);

        this.setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);

        try {
            background = ImageIO.read(new File("background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameStarted) {
            // Title
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.setColor(new Color(0, 120, 60));
            String title = "Reversi";
            int titleX = (getWidth() - g.getFontMetrics().stringWidth(title)) / 2;
            g.drawString(title, titleX, 110);

            // Instructions card
            int cardX = 130, cardY = 150, cardW = 740, cardH = 590;
            g.setColor(new Color(245, 248, 245));
            g.fillRoundRect(cardX, cardY, cardW, cardH, 24, 24);
            g.setColor(new Color(0, 120, 60));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 24, 24);

            // "How to Play" heading (centered)
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.setColor(new Color(0, 120, 60));
            String heading = "How to Play";
            int headingX = (getWidth() - g.getFontMetrics().stringWidth(heading)) / 2;
            g.drawString(heading, headingX, 200);

            // Body
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(new Color(40, 40, 40));
            String[] rules = {
                "Reversi is a two-player game on an 8x8 board.",
                "You play Black or White \u2014 Black always moves first.",
                "",
                "Click a square to place a piece. The move is legal only if it traps",
                "a straight line of the opponent's pieces \u2014 horizontal, vertical,",
                "or diagonal \u2014 between your new piece and another of yours.",
                "Every piece you trap then flips to your color.",
                "",
                "Legal squares show a faint ghost piece on your turn.",
                "If you have no legal move, your turn is skipped automatically.",
                "",
                "The game ends when neither player can move.",
                "The player with the most pieces on the board wins.",
                "",
                "Click \"Ready\" \u2014 the game starts once both players are ready.",
                "Tip: press \"u\" anytime to jump to the end-game screen."
            };
            int textX = cardX + 45;
            int textY = 245;
            int lineHeight = 28;
            for (String line : rules) {
                g.drawString(line, textX, textY);
                textY += lineHeight;
            }
            return;
        }

        // -- Game Over Screen --
        // Hold off the overlay while a flip animation is in progress so the
        // winning move's flips are visible; once the timer nulls the mask,
        // the next repaint falls through here and the overlay drops.
        if (grid != null && grid.isGameOver() && !seeBoard && flippingMask == null) {
            seeBoardButton.setVisible(true);
            playAgainButton.setVisible(true);
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.setColor(Color.WHITE);
            g.drawString("Game Over!", 270, 350);

            g.setFont(new Font("Arial", Font.BOLD, 32));
            if (grid.getWinner() == 0) {
                g.setColor(Color.YELLOW);
                g.drawString("It's a tie!", 370, 430);
            } else if (grid.getWinner() == playerNumber) {
                g.setColor(Color.GREEN);
                g.drawString("You win!", 380, 430);
                playVictorySound();
            } else {
                g.setColor(Color.RED);
                g.drawString("You lose!", 370, 430);
                playLossSound();
            }

            // show final piece counts
            int blackCount = 0, whiteCount = 0;
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if (grid.getCell(r, c) == 1) blackCount++;
                    else if (grid.getCell(r, c) == 2) whiteCount++;
                }
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Black: " + blackCount + "  White: " + whiteCount, 340, 490);
            return;
        }

        if (grid != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
            g.setFont(new Font("Arial", Font.BOLD, 18));

            // Which color you are
            String you = (playerNumber == 1) ? "You: Black" : "You: White";
            g.setColor(Color.BLACK);
            g.drawString(you, 100, 60);

            // Whose turn it is
            String turn = (grid.getCurrentPlayer() == 1) ? "Turn: Black" : "Turn: White";
            g.drawString(turn, 350, 60);

            // Count pieces
            int blackCount = 0, whiteCount = 0;
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if (grid.getCell(r, c) == 1) blackCount++;
                    else if (grid.getCell(r, c) == 2) whiteCount++;
                }
            }
            g.drawString("Black: " + blackCount + "  White: " + whiteCount, 100, 780);
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) g.setColor(new Color(200, 230, 200));
                else                   g.setColor(new Color(0, 120, 60));
                g.fillRect(offsetX + c * cellSize, offsetY + r * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(offsetX + c * cellSize, offsetY + r * cellSize, cellSize, cellSize);
            }
        }

        // Draw pieces from grid
        if (grid != null) {
            // animation progress, 0 -> 1 across FLIP_TICKS frames
            double t = 0;
            if (flippingMask != null) {
                t = Math.min(1.0, flipTickCount / (double) FLIP_TICKS);
            }
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if(flippingMask != null && flippingMask[r][c]) {
                        // grid is already committed, so "to" is the cell's
                        // current color and "from" is its opposite
                        Color to   = (grid.getCell(r, c) == 1) ? Color.BLACK : Color.WHITE;
                        Color from = (grid.getCell(r, c) == 1) ? Color.WHITE : Color.BLACK;
                        drawFlipPiece(g, r, c, from, to, t);
                    }
                    else if (grid.getCell(r, c) == 1)
                        drawPiece(g, r, c, Color.BLACK);
                    else if (grid.getCell(r, c) == 2)
                        drawPiece(g, r, c, Color.WHITE);
                }
            }
        }

        // Draw valid-move hints, but only when it's THIS player's turn
        if (grid != null && !grid.isGameOver() && grid.getCurrentPlayer() == playerNumber) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if (grid.getCell(r, c) == 0 && grid.isValidCell(r, c)) {
                        drawHint(g, r, c);
                    }
                }
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, 1000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            if (startButton.getText().equals("Ready")) {
                startButton.setText("Unready");
                sendMessage("ready");
            } else {
                startButton.setText("Ready");
                sendMessage("unready");
            }
        } else if (e.getSource() == seeBoardButton) {
            seeBoard = !seeBoard;
            seeBoardButton.setText(seeBoard ? "See Results" : "See Board");
        } else if (e.getSource() == playAgainButton) {
            sendMessage("reset");
        }
        repaint();
    }

    public void sendMessage(String message) {
        try {
            outObj.writeObject(message);
        } catch (Exception ex) {
            System.out.println("Error sending message: " + ex.getMessage());
        }
    }

    private void drawPiece(Graphics g, int row, int col, Color color) {
        int margin = 10;
        g.setColor(color);
        g.fillOval(offsetX + col * cellSize + margin, offsetY + row * cellSize + margin, cellSize - margin * 2, cellSize - margin * 2);
    }

    // Faint "ghost" piece showing a legal placement
    private void drawHint(Graphics g, int row, int col) {
        int margin = 28;
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(offsetX + col * cellSize + margin, offsetY + row * cellSize + margin, cellSize - margin * 2, cellSize - margin * 2);
    }

    // Coin-flip frame: oval whose width is scaled by |cos(t*PI)| so it
    // squeezes to a thin sliver at t=0.5, with the color swapping at that
    // edge-on moment. t runs 0 -> 1 across the animation.
    private void drawFlipPiece(Graphics g, int row, int col, Color from, Color to, double t) {
        int margin = 10;
        int radius = (cellSize - margin * 2) / 2;
        int cx = offsetX + col * cellSize + cellSize / 2;
        int cy = offsetY + row * cellSize + cellSize / 2;
        int halfW = (int) (radius * Math.abs(Math.cos(t * Math.PI)));
        g.setColor(t < 0.5 ? from : to);
        g.fillOval(cx - halfW, cy - radius, halfW * 2, radius * 2);
    }

    public void connect() throws IOException {
        String hostName = "10.10.10.157";
        int portNumber = 1024;
        Socket socket = new Socket(hostName, portNumber);

        try {
            outObj = new ObjectOutputStream(socket.getOutputStream());
            inObj = new ObjectInputStream(socket.getInputStream());

            while (true) {
                try {
                    Object storage = inObj.readObject();

                    if (storage instanceof Grid) {
                        Grid newGrid = (Grid) storage;

                        // Count total pieces on the incoming board
                        int newCount = 0;
                        for (int r = 0; r < 8; r++) {
                            for (int c = 0; c < 8; c++) {
                                if (newGrid.getCell(r, c) != 0) newCount++;
                            }
                        }

                        // A real move always adds exactly one piece. If the count
                        // went up (and this isn't the very first grid), play the
                        // place sound. The win/loss sound now waits for the flip
                        // animation to finish, so there's no overlap to dodge.
                        if (grid != null && newCount > lastPieceCount) {
                            playPlaceSound();
                        }
                        lastPieceCount = newCount;

                        // animation loop here
                        flipCells = newGrid.getToFlip();
                        if (flipCells.size() > 0) {
                            // build the mask of cells to animate
                            flippingMask = new boolean[8][8];
                            for(int i = 0; i<flipCells.size(); i++) {
                                int[] cell = flipCells.get(i);
                                flippingMask[cell[0]][cell[1]] = true;
                            }

                            // Lazily create the timer the first time a flip happens.
                            // Fires every FLIP_DELAY_MS, increments the frame counter,
                            // repaints, and stops itself once FLIP_TICKS frames have run.
                            if (flipTimer == null) {
                                ActionListener flipListener = new ActionListener() {
                                    public void actionPerformed(ActionEvent ev) {
                                        flipTickCount++;
                                        if (flipTickCount >= FLIP_TICKS) {
                                            flippingMask = null;
                                            flipTimer.stop();
                                        }
                                        repaint();
                                    }
                                };
                                flipTimer = new javax.swing.Timer(FLIP_DELAY_MS, flipListener);
                            }
                            flipTickCount = 0;
                            flipTimer.restart();
                        }

                        grid = newGrid;
                        repaint();
                    } else if (storage instanceof Integer) {
                        playerNumber = (Integer) storage;
                    } else if (storage instanceof String) {
                        String msg = (String) storage;
                        if (msg.equals("start")) {
                            gameStarted = true;
                            startButton.setVisible(false);
                            repaint();
                        } else if (msg.equals("reset")) {
                            // Server sent everyone back to the lobby. Undo every
                            // local flag the game and game-over screen touched.
                            gameStarted = false;
                            grid = null;            // mirrors a fresh connect; avoids a stray place-sound
                            seeBoard = false;
                            lossTrigger = true;     // re-arm the one-shot end-game sounds
                            victoryTrigger = true;
                            lastPieceCount = -1;
                            if (flipTimer != null) flipTimer.stop();   // kill any in-flight flip
                            flippingMask = null;
                            startButton.setText("Ready");
                            startButton.setVisible(true);
                            seeBoardButton.setText("See Board");
                            seeBoardButton.setVisible(false);
                            playAgainButton.setVisible(false);
                            repaint();
                        }
                    }

                } catch (Exception e) {

                }
            }

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + "hostName");
            System.exit(1);
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!gameStarted || grid == null) return;

        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;

        if (row < 0 || row >= 8 || col < 0 || col >= 8) return;

        try {
            outObj.writeObject(new int[]{row, col});
        } catch (Exception ex) {
            System.out.println("Error sending move: " + ex.getMessage());
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (!gameStarted || grid == null) return;

        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;

        if (row < 0 || row >= 8 || col < 0 || col >= 8) return;

        try {
            outObj.writeObject(new int[]{row, col});
        } catch (Exception ex) {
            System.out.println("Error sending move: " + ex.getMessage());
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'u' && gameStarted && grid != null) {
            sendMessage("endgame");
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void playPlaceSound() {
        try {
            URL url = this.getClass().getClassLoader().getResource("place.wav");
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            clip.start();
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    public void playLossSound() {
        if(lossTrigger) {
            lossTrigger = false;
        } else {
            return;
        }
        try {
            URL url = this.getClass().getClassLoader().getResource("loss.wav");
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            clip.start();
            System.out.println("Played loss sound");
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    public void playVictorySound() {
        if(victoryTrigger) {
            victoryTrigger = false;
        } else {
            return;
        }
        try {
            URL url = this.getClass().getClassLoader().getResource("victory.wav");
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            clip.start();
            System.out.println("Played victory sound");
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}