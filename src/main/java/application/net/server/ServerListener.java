package application.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.misc.LongUser;
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
			server.addOnlineUser(serverUsername, outputStream);
			
			//Prima di iniziare a gestire le richieste controllo se ci sono messaggi inviati quando il client era offline
			checkPendingMessage();
			
			while(!Thread.currentThread().isInterrupted()) {
				String request = (String) inputStream.readObject();
				
				switch (request) {
					case Protocol.MESSAGE_SEND_REQUEST:
						handleSendMessage(false);
						break;
						
					case Protocol.ONLINE_STATUS_REQUEST:
						handleOnlineStatusRequest();
						break;
						
					case Protocol.GROUP_MESSAGE_SEND_REQUEST:
						handleSendMessage(true);
						break;
						
					case Protocol.CONTACTS_SEARCH:
						handleContactSearch();
						break;
						
					default:
						sendMessage(Protocol.BAD_REQUEST);
						break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			return;
		} catch (SQLException i) {
			sendMessage(Protocol.SERVER_ERROR);
			i.printStackTrace();
			return;
		}
		 catch (ClassNotFoundException f) {
			sendMessage(Protocol.BAD_REQUEST);
			f.printStackTrace();
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
	
	private void checkPendingMessage() throws SQLException {
		ArrayList <Message> listMessaggi = DatabaseHandler.getInstance().getPendingMessages(serverUsername);
		if(listMessaggi.isEmpty())
			return;
		
		sendMessage(Protocol.MESSAGES_RETRIEVED);
		
		InformationMessage msg = new InformationMessage();
		msg.setPacket(listMessaggi);
		msg.setInformation(Protocol.MESSAGES_LIST);
		sendObject(msg);
	}
		
	private void handleOnlineStatusRequest() throws IOException, SQLException, ClassNotFoundException {
		String userToCheck = (String) inputStream.readObject();
		try {
			InformationMessage info = new InformationMessage();
			info.setInformation(Protocol.ONLINE_STATUS_REQUEST);
			
			sendMessage(Protocol.ONLINE_STATUS_REQUEST);
			
			if(server.checkIsUserLogged(userToCheck)) 
				info.setPacket(userToCheck + ";" + Protocol.USER_ONLINE);	
			else {
				String date = DatabaseHandler.getInstance().getLastAccess(userToCheck);
				System.out.println(date);
				if(date == null) 
					info.setPacket(userToCheck + ";" + null);		
				else {
					String dayDate = Utilities.getDateFromString(date);
					String hour = Utilities.getHourFromStringTrimmed(date);
					info.setPacket(userToCheck + ";" + dayDate + " " + hour);
					//TODO change date format
				}
			}
			
			sendObject(info);
		} catch (NullPointerException e) {
			return;
		}
	}
	
	private void handleContactSearch() throws ClassNotFoundException, IOException, SQLException {
		try {
			String subUsername = (String) inputStream.readObject();
			ArrayList <User> listaRicercati = DatabaseHandler.getInstance().searchUsers(subUsername);
			if(listaRicercati.size() > 0) {
				InformationMessage msg = new InformationMessage();
				msg.setPacket(listaRicercati);
				msg.setInformation(Protocol.CONTACTS_SEARCH);
				
				sendMessage(Protocol.CONTACTS_SEARCH);
				sendObject(msg);
			}
			
		} catch (NullPointerException e) {
			return;
		}
	}

	
	private void handleSingleMessageSend(String receiver, ChatMessage msg) throws IOException, SQLException {
		ObjectOutputStream destStream = server.getStream(receiver);
		
		if(destStream == null) {
			DatabaseHandler.getInstance().addPendingMessage(msg, receiver);
		}
		else {
			destStream.writeObject(Protocol.MESSAGE_SEND_REQUEST);
			destStream.writeObject(msg);
			destStream.flush();
		}
	}

	private void handleSendMessage(boolean isGroupMessage) throws IOException, ClassNotFoundException, SQLException {
		try {
			Message msg = (Message) inputStream.readObject();
			
			if(!(msg instanceof ChatMessage)) {
				sendMessage(Protocol.BAD_REQUEST);
				return;
			}
			
			ChatMessage chatMsg = (ChatMessage) msg;
			
			String receiver = msg.getReceiver();
			
			if(!isGroupMessage) {
				//Se è un messaggio singolo prendo il socket della persona e lo invio direttamente a lei
				handleSingleMessageSend(receiver, chatMsg);
			}
			else {
				//Devo prendere tutte le persone che sono nel gruppo e inviarlo singolarmente a ognuno di esse
				ArrayList <String> groupUsers = DatabaseHandler.getInstance().getGroupPartecipants(chatMsg.getGroupId());
				for(String partecipant : groupUsers) {
					handleSingleMessageSend(partecipant, chatMsg);
				}
			}
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
			System.out.println("Gia loggato");
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
		LongUser utente = (LongUser) retrieveUser();
		
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
		sendObject(message);
	}
	
	private void sendObject(Object obj) {
		if(outputStream == null)
			return;
		
		try {
			outputStream.writeObject(obj);
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

