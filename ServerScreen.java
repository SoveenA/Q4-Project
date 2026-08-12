import java.awt.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

public class ServerScreen extends JPanel {

    private Manager manager;

    public ServerScreen() {
        setBackground(Color.WHITE);
    }

    public Dimension getPreferredSize() {
        return new Dimension(300, 150);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Server IP:", 20, 40);
        try {
            g.drawString(InetAddress.getLocalHost().getHostAddress(), 20, 65);
        } catch (Exception e) {
            g.drawString("Could not get IP", 20, 65);
        }

        g.drawString("Connected players:", 20, 95);
        if (manager != null) {
            g.drawString("" + manager.getPlayerCount(), 20, 120);
        }
    }

    public void startServer() throws IOException {
        int portNumber = 1024;
        ServerSocket serverSocket = new ServerSocket(portNumber);
        manager = new Manager(this);

        while (true) {
            System.out.println("Waiting for connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");
            ServerThread st = new ServerThread(clientSocket, manager);
            manager.add(st);
            new Thread(st).start();
        }
    }
}