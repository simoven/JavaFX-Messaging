package application.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;

import application.logic.ImageMessage;
import application.logic.InformationMessage;
import application.logic.Message;
import application.logic.TextMessage;
import application.net.misc.Protocol;
import application.net.misc.User;
import application.net.misc.Utilities;

public class ServerListener implements Runnable {

	private Socket socket;
	private ObjectInputStream inputStream = null;
	private ObjectOutputStream outputStream = null;
	private Server server;
	private String serverUsername;
	private boolean isLogged;
	
	public ServerListener(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.serverUsername = null;
		this.isLogged = false;
		
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());		
		} catch (IOException e) {
			//TODO mostra errore impossibile stabilire una connessione con il server
		}
	}
	
	public void run() {
		try {			
			inputStream = new ObjectInputStream(socket.getInputStream());
			while(!isLogged) {
				String request = (String) inputStream.readObject();
				
				if(request == null)
					continue;
				
				if(request.equals(Protocol.REQUEST_LOGIN)) {
					if(!handleLogin()) {
						closeStreams();
						return;
					}
					
					isLogged = true;
				}
				else if(request.equals(Protocol.REQUEST_REGISTRATION)) {
					if(!handleRegistration()) {
						closeStreams();
						return;
					}
				}
				else 
					sendMessage(Protocol.BAD_REQUEST);
			}
			
			//Adesso sono loggato
			server.addOnlineUser(serverUsername, socket);
			
			while(!Thread.currentThread().isInterrupted() && inputStream != null && outputStream != null) {
				String request = (String) inputStream.readObject();
				
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
			disconnect();
			e.printStackTrace();
			return;
		} catch (SQLException i) {
			sendMessage(Protocol.SERVER_ERROR);
			i.printStackTrace();
			return;
		}
		 catch (ClassNotFoundException f) {
			sendMessage(Protocol.BAD_REQUEST);
			disconnect();
			f.printStackTrace();
			return;
		}
	}
	
	private void disconnect() {
		try {
			if(server.disconnectUser(serverUsername))
				DatabaseHandler.getInstance().updateLastAccess(serverUsername);
		} catch (SQLException i) {/*nulla*/ }
		closeStreams();
	}
		
	private void handleOnlineStatusRequest() throws IOException, SQLException, ClassNotFoundException {
		String userToCheck = (String) inputStream.readObject();
		try {
			InformationMessage info = new InformationMessage();
			
			sendMessage(Protocol.ONLINE_STATUS_REQUEST);
			
			if(server.checkIsUserLogged(userToCheck))
				info.setInformation(Protocol.USER_ONLINE);
			else {
				String date = DatabaseHandler.getInstance().getLastAccess(userToCheck);
				info.setInformation(date);
				//TODO change date format
				System.out.println(date);
			}
			
			outputStream.writeObject(info);
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
			ObjectOutputStream out = new ObjectOutputStream(recSocket.getOutputStream());
			
			out.writeObject(Protocol.IMAGE_SEND_REQUEST);
			out.writeObject(msg);
			out.flush();
			
			out.close();
		}
	}

	private void handleImageSend(boolean isGroupMessage) throws IOException, SQLException, ClassNotFoundException {
		try {
			Message msg = (Message) inputStream.readObject();
		
			if(!(msg instanceof ImageMessage)) {
				sendMessage(Protocol.BAD_REQUEST);
				return;
			}
			
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
			
			sendMessage(Protocol.REQUEST_SUCCESSFUL);
		} catch (NullPointerException e) {
			sendMessage(Protocol.BAD_REQUEST);
			return;
		}
	}
	
	private void handleSingleMessageSend(String receiver, TextMessage msg) throws IOException, SQLException {
		Socket recSocket = server.getSocket(receiver);
		
		if(recSocket == null) {
			DatabaseHandler.getInstance().addPendingMessage(msg, receiver);
		}
		else {
			ObjectOutputStream out = new ObjectOutputStream(recSocket.getOutputStream());
			
			out.writeObject(Protocol.MESSAGE_SEND_REQUEST);
			out.writeObject(msg);
			out.flush();
			
			out.close();
		}
	}

	private void handleSendMessage(boolean isGroupMessage) throws IOException, ClassNotFoundException, SQLException {
		try {
			Message msg = (Message) inputStream.readObject();
			
			if(!(msg instanceof TextMessage)) {
				sendMessage(Protocol.BAD_REQUEST);
				return;
			}
			
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
			
			sendMessage(Protocol.REQUEST_SUCCESSFUL);
		} catch (NullPointerException e) {
			sendMessage(Protocol.BAD_REQUEST);
			return;
		}
	}

	private boolean handleLogin() throws IOException, ClassNotFoundException, SQLException {
		String username = (String) inputStream.readObject();
		String password = (String) inputStream.readObject();
		
		if(!Utilities.checkIfUsernameValid(username).equals(Utilities.USERNAME_VALID) ||
		   !Utilities.checkIfPasswordValid(password).equals(Utilities.PASSWORD_VALID)) {
			sendMessage(Protocol.INVALID_CREDENTIAL);
			return false;
		}
		
		if(server.checkIsUserLogged(username)) {
			sendMessage(Protocol.USER_ALREADY_LOGGED);
			return false;
		}	
		
		User utente = DatabaseHandler.getInstance().checkUserLogin(username, password);
		
		if(utente == null) {
			sendMessage(Protocol.WRONG_CREDENTIAL);
			return false;
		}
		
		serverUsername = username;
		
		sendMessage(Protocol.REQUEST_SUCCESSFUL);
		sendUser(utente);
		
		return true;
	}
	
	private boolean handleRegistration() throws IOException, ClassNotFoundException, SQLException {
		User utente = retrieveUser();
		
		if(!Utilities.checkIfUsernameValid(utente.getUsername()).equals(Utilities.USERNAME_VALID) ||
		   !Utilities.checkIfPasswordValid(utente.getPassword()).equals(Utilities.PASSWORD_VALID)) {
			sendMessage(Protocol.INVALID_CREDENTIAL);
			return false;
		}
		
		if(!DatabaseHandler.getInstance().registerUser(utente)) {
			sendMessage(Protocol.USER_ALREADY_EXIST);
			return false;
		}
		
		sendMessage(Protocol.REQUEST_SUCCESSFUL);
		return true;
	}
	
	private void sendMessage(String message) {
		if(outputStream == null)
			return;
		
		try {
			outputStream.writeObject(message);
			outputStream.flush();
		} catch (IOException e) {
			closeStreams();
			e.printStackTrace();
			return;
		}
	}
	
	private User retrieveUser() throws IOException, ClassNotFoundException {
		User utente = (User) inputStream.readObject();
		
		return utente;
	}
	
	private void sendUser(User utente) throws IOException {
		outputStream.writeObject(utente);
		outputStream.flush();
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

