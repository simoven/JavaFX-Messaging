package application.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.misc.LongUser;
import application.net.misc.Protocol;
import application.net.misc.User;
import application.net.misc.Utilities;
import javafx.util.Pair;

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
						handleSendMessage();
						break;
						
					case Protocol.ONLINE_STATUS_REQUEST:
						handleOnlineStatusRequest();
						break;
						
					case Protocol.CONTACTS_SEARCH:
						handleContactSearch();
						break;
						
					case Protocol.GROUP_CREATION:
						handleGroupCreation();
						break;
						
					case Protocol.CONTACT_INFORMATION_REQUEST:
						handleContactInformation(false);
						break;
						
					case Protocol.CONTACT_FULL_INFORMATION_REQUEST:
						handleContactInformation(true);
						break;
						
					case Protocol.GROUP_INFORMATION_REQUEST:
						handleGroupInformation();
						break;
						
					case Protocol.GROUP_PARTECIPANT_REQUEST:
						handleGroupPartecipants();
						break;
						
					case Protocol.GROUP_MEMBER_RIMOTION:
						handleGroupRimotion();
						break;
						
					case Protocol.GROUP_MEMBER_ADD:
						handleGroupAdd();
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
	
	@SuppressWarnings("unchecked")
	private void handleGroupAdd() throws IOException, SQLException, ClassNotFoundException {
		String requester = (String) inputStream.readObject();
		int groupId = Integer.parseInt((String) inputStream.readObject());
		Vector <String> users = (Vector <String>) inputStream.readObject();
		
		if(!requester.equals(DatabaseHandler.getInstance().getGroupInfo(groupId).getGpOwner()))
			return;
		
		ArrayList <String> listPartecipanti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		if(listPartecipanti.indexOf(requester) == -1) {
			System.out.println("owner not in group");
			return;
		}
		
		listPartecipanti.addAll(users);
		
		DatabaseHandler.getInstance().addPartecipantsToGroup(groupId, users);
		InformationMessage msg;
		ChatMessage infMsg;
		for(String added : users) {
			msg = new InformationMessage();
			msg.setInformation(Protocol.GROUP_MEMBER_ADD);
			msg.setPacket(new Pair <String, Integer>(added, groupId));
			
			for(String receiver : listPartecipanti) {
				ObjectOutputStream destOutput = server.getStream(receiver);
				//Se è offline lascio la richiesta in sospeso
				if(destOutput == null) {
					infMsg = new ChatMessage("null", receiver);
					infMsg.setGroupMessage(true);
					infMsg.setText("ADDED:" + added);
					infMsg.setTimestamp(Utilities.getCurrentISODate());
					infMsg.setGroupId(groupId);
					DatabaseHandler.getInstance().addPendingMessage(infMsg, receiver);
				}
				else {
					//Se l'utente è online gli notifico subito l'aggiunta di un partecipante al gruppo
					destOutput.writeObject(Protocol.GROUP_MEMBER_ADD);
					destOutput.writeObject(msg);
				}
			}
		}
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
	
	private void handleGroupRimotion() throws ClassNotFoundException, IOException, SQLException {
		int groupId = Integer.parseInt((String) inputStream.readObject());
		String members = (String) inputStream.readObject();
		String [] split = members.split(":");
		String requester = split [0];
		String memberRemoved = split [1];
		
		if(!requester.equals(DatabaseHandler.getInstance().getGroupInfo(groupId).getGpOwner()))
			return;
		
		ArrayList <String> listPartecipanti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		if(listPartecipanti.indexOf(requester) == -1) {
			System.out.println("owner not in group");
			return;
		}
		
		DatabaseHandler.getInstance().removeUserFromGroup(groupId, memberRemoved);
		InformationMessage msg = new InformationMessage();
		msg.setInformation(Protocol.GROUP_MEMBER_RIMOTION);
		msg.setPacket(new Pair <String, Integer>(memberRemoved, groupId));
		for(String receiver : listPartecipanti) {
			ObjectOutputStream destOutput = server.getStream(receiver);
			if(destOutput == null) {
				ChatMessage infMsg = new ChatMessage("null", receiver);
				infMsg.setGroupMessage(true);
				infMsg.setText("REMOVED:" + memberRemoved);
				infMsg.setTimestamp(Utilities.getCurrentISODate());
				infMsg.setGroupId(groupId);
				DatabaseHandler.getInstance().addPendingMessage(infMsg, receiver);
			}
			else {
				destOutput.writeObject(Protocol.GROUP_MEMBER_RIMOTION);
				destOutput.writeObject(msg);
			}
		}
	}

	
	private void handleGroupPartecipants() throws SQLException, ClassNotFoundException, IOException {
		int groupId = Integer.parseInt((String) inputStream.readObject());
		ArrayList <String> utenti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		
		if(utenti != null) {
			InformationMessage msg = new InformationMessage();
			msg.setInformation(Protocol.GROUP_PARTECIPANT_REQUEST);
			msg.setGroupId(groupId);
			msg.setPacket(utenti);
			sendMessage(Protocol.GROUP_PARTECIPANT_REQUEST);
			sendObject(msg);
		}	
	}
	
	private void handleGroupInformation() throws ClassNotFoundException, IOException, SQLException {
		int groupId = Integer.parseInt((String) inputStream.readObject());
		User group = DatabaseHandler.getInstance().getGroupInfo(groupId);
		
		if(group != null) {
			InformationMessage msg = new InformationMessage();
			msg.setInformation(Protocol.GROUP_INFORMATION_REQUEST);
			msg.setPacket(group);
			msg.setGroupId(groupId);
			sendMessage(Protocol.GROUP_INFORMATION_REQUEST);
			sendObject(msg);
		}
	}

	private void handleContactInformation(boolean fullInfo) throws ClassNotFoundException, IOException, SQLException {
		String userToCheck = (String) inputStream.readObject();
		User utente = DatabaseHandler.getInstance().getUserInfo(userToCheck);
	
		if(utente != null) {
			InformationMessage msg = new InformationMessage();
			msg.setPacket(utente);
			if(!fullInfo) {
				msg.setInformation(Protocol.CONTACT_INFORMATION_REQUEST);
				sendMessage(Protocol.CONTACT_INFORMATION_REQUEST);
			}
			else {
				msg.setInformation(Protocol.CONTACT_FULL_INFORMATION_REQUEST);
				sendMessage(Protocol.CONTACT_FULL_INFORMATION_REQUEST);
			}
			sendObject(msg);
		}
		
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
		
		if(destStream == null) 
			DatabaseHandler.getInstance().addPendingMessage(msg, receiver);
		else {
			destStream.writeObject(Protocol.MESSAGE_SEND_REQUEST);
			destStream.writeObject(msg);
			destStream.flush();
		}
	}

	private void handleSendMessage() throws IOException, ClassNotFoundException, SQLException {
		try {
			Message msg = (Message) inputStream.readObject();
						
			if(!msg.isAGroupMessage()) {
				//Se è un messaggio singolo prendo il socket della persona e lo invio direttamente a lei
				handleSingleMessageSend(msg.getReceiver(), (ChatMessage) msg);
			}
			else {
				//Devo prendere tutte le persone che sono nel gruppo e inviarlo singolarmente a ognuno di esse
				ArrayList <String> groupUsers = DatabaseHandler.getInstance().getGroupPartecipants(msg.getGroupId());
				for(String partecipant : groupUsers) 
				{
					//Evito di mandare il messaggio a me stesso, a meno che non sia un messaggio informativo
					if(partecipant.equals(serverUsername) && !msg.getSender().equals("null"))
						continue;
					
					handleSingleMessageSend(partecipant, (ChatMessage) msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			//TODO Show error
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
	
	private void handleGroupCreation() throws ClassNotFoundException, IOException, SQLException {
		String groupName = (String) inputStream.readObject();
		String owner = (String) inputStream.readObject();
		String imageType = (String) inputStream.readObject();
		
		byte [] imgProfilo = null;
		if(imageType.equals(Protocol.IMAGE_NOT_NULL))
			imgProfilo = inputStream.readAllBytes();
		
		String request = (String) inputStream.readObject();
		Vector <String> partecipants = new Vector <String>();
		while(!request.equals(Protocol.GROUP_CREATION_DONE)) {
			partecipants.add((String) inputStream.readObject());
			request = (String) inputStream.readObject();
		}
		
		InformationMessage msg = new InformationMessage();
		int groupID = DatabaseHandler.getInstance().createGroup(groupName, owner, imgProfilo);
		msg.setInformation(Protocol.GROUP_CREATION_DONE);
		msg.setPacket(new Pair<String, Integer>(groupName, groupID));
		
		if(groupID != -1)
			DatabaseHandler.getInstance().addPartecipantsToGroup(groupID, partecipants);
		
		sendMessage(Protocol.GROUP_CREATION_DONE);
		sendObject(msg);
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

