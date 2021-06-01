package application.net.server;

import javax.swing.JOptionPane;

public class MainServer {

	public static void main(String[] args) {
		Server server = new Server();
		
		if(server.startServer())
			JOptionPane.showMessageDialog(null, "Server has been correctly started and it's running", "Success", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(null, "Error while starting the server", "Error", JOptionPane.ERROR_MESSAGE);
	}
}
