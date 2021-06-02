package application;

import java.io.File;
import java.util.Scanner;

import javax.swing.JFileChooser;

import application.logic.ImageMessage;
import application.logic.TextMessage;
import application.net.client.Client;
import application.net.misc.User;
import application.net.misc.Utilities;

public class Main {

	public static void main(String[] args) {
		//MainApplication.main(args);
		Scanner scn = new Scanner(System.in);
		String username = null;
		
		while(true) {
			String request = scn.next();
			if(request.equals("login")) {
				String user = scn.next();
				String pass = scn.next();
				
				User ut = Client.getInstance().requestLogin(user, pass);
				
				if(ut != null) {
					System.out.println("Login ok");
					username = user;
				}
				else {
					System.out.println("Login error");
					Client.getInstance().reset();
				}
			}
			else if(request.equals("registration")) {
				String user = scn.next();
				String pass = scn.next();
				
				User utente = new User(user, pass, "simone", "ventrici");
				
				if(Client.getInstance().requestRegistration(utente))
					System.out.println("Registration ok");
				else {
					System.out.println("Registration error");
					Client.getInstance().reset();
				}
			}
			else if(request.equals("Message")) {
				String message = scn.next();
				String dest = scn.next();
				TextMessage msg = new TextMessage(username, dest, message);
				msg.setGroupMessage(false);
				msg.setSentDate(Utilities.getDateFromString(Utilities.getCurrentISODate()));
				msg.setSentHour(Utilities.getHourFromString(Utilities.getCurrentISODate()));
				
				if(Client.getInstance().sendChatMessage(msg, true))
					System.out.println("Messaggio inviato");
				else
					System.out.println("Messaggio non inviato");
			}
			else if(request.equals("Image")) {
				JFileChooser f = new JFileChooser();
				File file = null;
				int res = f.showOpenDialog(null);
				if(res == JFileChooser.APPROVE_OPTION)
					file = f.getSelectedFile();
				
				String dest = scn.next();
				ImageMessage msg = new ImageMessage(username,  dest);
				msg.setImage(Utilities.getByteArrFromFile(file));
				msg.setGroupMessage(false);
				msg.setSentDate(Utilities.getDateFromString(Utilities.getCurrentISODate()));
				msg.setSentHour(Utilities.getHourFromString(Utilities.getCurrentISODate()));
				
				if(Client.getInstance().sendChatMessage(msg, false))
					System.out.println("Messaggio inviato");
				else
					System.out.println("Messaggio non inviato");
			}
		}
	}
}
