import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    private int playerNumber;
    private ObjectOutputStream outObj;
    private ObjectInputStream inObj;
    private Manager manager;

    public ServerThread(Socket socket, Manager manager) {
        try {
            outObj = new ObjectOutputStream(socket.getOutputStream());
            inObj = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Error setting up server thread: " + e.getMessage());
        }

        this.manager = manager;
    }

    public void setPlayerNumber(int n) {
        playerNumber = n;
    }

    public void run() {
        // Tell the client which player number they are
        try {
            outObj.writeObject(playerNumber);
        } catch (Exception e) {
            System.out.println("Error sending player number: " + e.getMessage());
        }

        // Send the grid to the client as soon as they connect
        try {
            outObj.writeObject(manager.getGrid());
        } catch (Exception e) {
            System.out.println("Error sending grid: " + e.getMessage());
        }

        while (true) {
            try {
                Object storage = inObj.readObject();

                if (storage instanceof int[]) {
                    int[] move = (int[]) storage;
                    manager.playerMove(move[0], move[1], playerNumber);
                } else if (storage instanceof String) {
                    String msg = (String) storage;
                    if (msg.equals("ready")) manager.readyUp(playerNumber, true);
                    else if (msg.equals("unready")) manager.readyUp(playerNumber, false);
                    else if (msg.equals("endgame")) manager.endGame();
                    else if (msg.equals("reset")) manager.resetGame();
                }

            } catch (Exception e) {
            }
        }
    }

    // Send updated grid to this client
    public void sendGrid(Grid grid) {
        try {
            outObj.reset();
            outObj.writeObject(grid);
        } catch (Exception e) {
            System.out.println("Error sending grid to player " + playerNumber);
        }
    }

    // Send a String message to this client
    public void sendMessage(String message) {
        try {
            outObj.reset();
            outObj.writeObject(message);
        } catch (Exception e) {
            System.out.println("Error sending message to player " + playerNumber);
        }
    }

    public void recieveMessage(String[] message) {
        try {
            outObj.reset();
            outObj.writeObject(message);
        } catch (Exception e) {
        }
    }

    public void recieveGrid(Grid grid) {
        try {
            outObj.reset();
            outObj.writeObject(grid);
        } catch (Exception e) {
        }
    }
}