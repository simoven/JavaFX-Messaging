package application.net.server;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainServer {

	public static void main(String[] args) {
		//Il pannello serve solo a non far chiudere il thread
		JFrame frame = new JFrame("prova");
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JPanel());
		Server server = new Server();
		
		if(server.startServer()) {
			JOptionPane.showMessageDialog(null, "Server has been correctly started and it's running", "Success", JOptionPane.INFORMATION_MESSAGE);
			frame.setVisible(true);
		}
		else
			JOptionPane.showMessageDialog(null, "Error while starting the server", "Error", JOptionPane.ERROR_MESSAGE);
	}
}
