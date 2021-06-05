package application;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JFileChooser;

import application.logic.messages.ChatMessage;
import application.net.client.Client;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.User;
import application.net.misc.Utilities;

public class Main {

	public static void main(String[] args) {
		MainApplication.main(args);
		//System.out.println(Utilities.getCurrentISODate());
	
		/*Scanner scn = new Scanner(System.in);
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
				ChatMessage msg = new ChatMessage(username, dest);
				msg.setText(message);
				msg.setGroupMessage(false);
				msg.setSentDate(Utilities.getDateFromString(Utilities.getCurrentISODate()));
				msg.setSentHour(Utilities.getHourFromString(Utilities.getCurrentISODate()));
				
				if(Client.getInstance().sendChatMessage(msg))
					System.out.println("Messaggio inviato");
				else
					System.out.println("Messaggio non inviato");
			}
		}*/
	}
}
