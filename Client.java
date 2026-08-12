import java.io.*;
import javax.swing.JFrame;

public class Client {

    public static void main(String args[]) throws IOException {

        JFrame frame = new JFrame("Soveen A - Q4 Project - Reversi");

        ClientScreen sc = new ClientScreen();
        frame.add(sc);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        sc.connect();
    }
}
