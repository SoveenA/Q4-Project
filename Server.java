import java.io.*;
import javax.swing.JFrame;

public class Server {

	public static void main(String args[]) throws IOException {

		JFrame frame = new JFrame("Server");

		ServerScreen sc = new ServerScreen();
		frame.add(sc);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		// try {
		// 	System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
		// } catch (Exception e) {
		// 	System.out.println("Could not find IP address for this host");
		// }
		sc.startServer();

	}
}
