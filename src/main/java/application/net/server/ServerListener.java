package application.net.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import application.logic.ImageMessage;
import application.logic.InformationMessage;
import application.logic.Message;
import application.logic.TextMessage;
import application.net.misc.Protocol;
import application.net.misc.User;

public class ServerListener implements Runnable {

	private Socket socket;
	private BufferedReader inputStream = null;
	private BufferedOutputStream outputStream = null;
	private Server server;
	private String username;
	private boolean isLogged;
	
	public ServerListener(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.username = null;
		this.isLogged = false;
		
		try {
			outputStream = new BufferedOutputStream(socket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		} catch (IOException e) {
			//TODO mostra errore impossibile stabilire una connessione con il server
		}
	}
	
	public void run() {
		try {
			while(!isLogged) {
				String request = inputStream.readLine();
				if(request.equals(Protocol.REQUEST_LOGIN)) {
					if(!handleLogin()) {
						closeStreams();
						return;
					}
					
					sendMessage(Protocol.REQUEST_SUCCESSFUL);
					isLogged = true;
				}
				else if(request.equals(Protocol.REQUEST_REGISTRATION)) {
					if(!handleRegistration()) {
						closeStreams();
						return;
					}
					
					sendMessage(Protocol.REQUEST_SUCCESSFUL);
				}
				else 
					sendMessage(Protocol.BAD_REQUEST);
			}
			
			//Adesso sono loggato
			server.addOnlineUser(username, socket);
			
			while(!Thread.currentThread().isInterrupted() && inputStream != null && outputStream != null) {
				String request = inputStream.readLine();
				
				switch (request) {
					case Protocol.MESSAGE_SEND_REQUEST:
						handleSendMessage(false);
						break;
					
					case Protocol.IMAGE_SEND_REQUEST:
						handleImageSend(false);
						break;
						
					case Protocol.ONLINE_STATUS_REQUEST:
						handleOnlineStatusRequest();
						break;
						
					case Protocol.GROUP_MESSAGE_SEND_REQUEST:
						handleSendMessage(true);
						break;
						
					case Protocol.GROUP_IMAGE_SEND_REQUEST:
						handleImageSend(true);
						
					default:
						sendMessage(Protocol.BAD_REQUEST);
						break;
				}
			}
			
		} catch (IOException e) {
			sendMessage(Protocol.COMMUNICATION_ERROR);
			try {
				if(server.disconnectUser(username))
					DatabaseHandler.getInstance().updateLastAccess(username);
				} catch (SQLException i) {
					//nulla
				}
			closeStreams();
			return;
		} catch (SQLException i) {
			sendMessage(Protocol.SERVER_ERROR);
		} catch (ClassNotFoundException f) {
			sendMessage(Protocol.BAD_REQUEST);
			server.disconnectUser(username);
			closeStreams();
			return;
		}
	}
		
	private void handleOnlineStatusRequest() throws IOException, SQLException {
		String userToCheck = inputStream.readLine();
		try {
			PrintWriter writer = new PrintWriter(outputStream, true);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			InformationMessage info = new InformationMessage();
			
			writer.println(Protocol.ONLINE_STATUS_REQUEST);
			
			if(server.checkIsUserLogged(userToCheck))
				info.setInformation(Protocol.USER_ONLINE);
			else {
				String date = DatabaseHandler.getInstance().getLastAccess(userToCheck);
				info.setInformation(date);
				//TODO change date format
				System.out.println(date);
			}
			
			out.writeObject(info);
			out.close();
		} catch (NullPointerException e) {
			return;
		}
		
	}
	
	private void handleSingleImageSend(String receiver, ImageMessage msg) throws IOException, SQLException {
		Socket recSocket = server.getSocket(receiver);
		
		if(recSocket == null) {
			DatabaseHandler.getInstance().addPendingImage(msg, receiver);
		}
		else {
			PrintWriter writer = new PrintWriter(outputStream, true);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			
			writer.println(Protocol.IMAGE_SEND_REQUEST);
			out.writeObject(msg);
			
			writer.close();
			out.close();
		}
		
		socket.close();
	}

	private void handleImageSend(boolean isGroupMessage) throws IOException, SQLException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		
		try {
			Message msg = (Message) in.readObject();
			
			//Lo ignoro semplicemente
			if(!(msg instanceof ImageMessage))
				return;
			
			ImageMessage imgMsg = (ImageMessage) msg;
			
			String receiver = msg.getReceiver();
			
			if(!isGroupMessage) {
				handleSingleImageSend(receiver, imgMsg);
			}
			else {
				ArrayList <String> groupUsers = DatabaseHandler.getInstance().getGroupPartecipants(receiver);
				for(String partecipant : groupUsers) {
					handleSingleImageSend(partecipant, imgMsg);
				}
			}
			in.close();
		} catch (NullPointerException e) {
			return;
		}
	}
	
	private void handleSingleMessageSend(String receiver, TextMessage msg) throws IOException, SQLException {
		Socket recSocket = server.getSocket(receiver);
		
		if(recSocket == null) {
			DatabaseHandler.getInstance().addPendingMessage(msg, receiver);
		}
		else {
			PrintWriter writer = new PrintWriter(outputStream, true);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			
			writer.println(Protocol.MESSAGE_SEND_REQUEST);
			out.writeObject(msg);
			
			writer.close();
			out.close();
		}
	}

	private void handleSendMessage(boolean isGroupMessage) throws IOException, ClassNotFoundException, SQLException {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		
		try {
			Message msg = (Message) in.readObject();
			
			//Lo ignoro semplicemente
			if(!(msg instanceof TextMessage))
				return;
			
			TextMessage txtMsg = (TextMessage) msg;
			
			String receiver = msg.getReceiver();
			
			if(!isGroupMessage) {
				//Se è un messaggio singolo prendo il socket della persona e lo invio direttamente a lei
				handleSingleMessageSend(receiver, txtMsg);
			}
			else {
				//Devo prendere tutte le persone che sono nel gruppo e inviarlo singolarmente a ognuno di esse
				//Il receiver in questo caso è l'id del gruppo
				ArrayList <String> groupUsers = DatabaseHandler.getInstance().getGroupPartecipants(receiver);
				for(String partecipant : groupUsers) {
					handleSingleMessageSend(partecipant, txtMsg);
				}
			}
			
			in.close();
		} catch (NullPointerException e) {
			return;
		}
	}

	private boolean handleLogin() throws IOException, ClassNotFoundException, SQLException {
		String username = inputStream.readLine();
		String password = inputStream.readLine();
		
		if(server.checkIsUserLogged(username)) {
			sendMessage(Protocol.USER_ALREADY_LOGGED);
			return false;
		}	
		
		User utente = DatabaseHandler.getInstance().checkUserLogin(username, password);
		
		if(utente == null) {
			sendMessage(Protocol.WRONG_CREDENTIAL);
			return false;
		}
		
		sendUser(utente);
		return true;
	}
	
	private boolean handleRegistration() throws IOException, ClassNotFoundException, SQLException {
		User utente = retrieveUser();
		
		if(!DatabaseHandler.getInstance().registerUser(utente)) {
			sendMessage(Protocol.USER_ALREADY_EXIST);
			return false;
		}
		
		return true;
	}
	
	private void sendMessage(String message) {
		if(outputStream == null)
			return;
		
		PrintWriter pw = new PrintWriter(outputStream, true);
		pw.println(message);
	}
	
	private User retrieveUser() throws IOException, ClassNotFoundException {
		ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
		User utente = (User) input.readObject();
		
		input.close();
		
		return utente;
	}
	
	private void sendUser(User utente) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(utente);
		out.flush();
		
		out.close();
	}
	
	private void closeStreams() {
		try {
			if(outputStream != null)
				outputStream.close();
			
			if(inputStream != null)
				inputStream.close();
			
			if(socket != null)
				socket.close();
		} catch(IOException e) {
			//Niente
		}
		
		inputStream = null;
		outputStream = null;
		socket = null;
	}
}

