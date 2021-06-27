package application.net.server;

import java.io.File;
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
			e.printStackTrace();
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
						handleGroupRimotion(false);
						break;
						
					case Protocol.GROUP_MEMBER_ADD:
						handleGroupAdd();
						break;
						
					case Protocol.GROUP_MEMBER_LEFT:
						handleGroupRimotion(true);
						break;
						
					case Protocol.GROUP_DELETION:
						handleGroupDeletion();
						break;
						
					case Protocol.GROUP_PICTURE_CHANGED:
						handleGroupInfoChanged(true);
						break;
						
					case Protocol.GROUP_NAME_CHANGED:
						handleGroupInfoChanged(false);
						break;
						
					case Protocol.PASSWORD_CHANGE:
						handlePasswordChange();
						break;
						
					case Protocol.PHOTO_CHANGE:
						handleMyInfoChanged(true);
						break;
						
					case Protocol.STATUS_CHANGE:
						handleMyInfoChanged(false);
						break;
						
					case Protocol.REMOVE_MESSAGE:
						handleMessageRimotion();
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
	
	private void handleSingleMessageRimotion(InformationMessage infMsg, ChatMessage chatMsg, String receiver) throws SQLException, IOException {
		ObjectOutputStream destOut = server.getStream(receiver);
		if(destOut == null) 
			DatabaseHandler.getInstance().addPendingMessage(chatMsg, receiver);
		else {
			destOut.writeObject(Protocol.REMOVE_MESSAGE);
			destOut.writeObject(infMsg);
		}
	}
	
	private void handleMessageRimotion() throws ClassNotFoundException, IOException, SQLException {
		ChatMessage msg = (ChatMessage) inputStream.readObject();
		
		//Questi sono gli utenti che non hanno mai ricevuto il messaggio che è stato eliminato perché erano offline
		//e quindi non serve comunicargli che è stato eliminato il messaggio
		ArrayList <String> usersNotReceivedMessage = DatabaseHandler.getInstance().removeMessage(msg);
		
		InformationMessage infMsg = new InformationMessage();
		infMsg.setInformation(Protocol.REMOVE_MESSAGE);
		infMsg.setPacket(msg);
		msg.setDeleted(true);
		
		//Se il destinatario non è nell'array, allora ha ricevuto quel messaggio e, quindi, gli devo comunicare l'eliminazione
		if(!msg.isAGroupMessage()) {
			if(!usersNotReceivedMessage.contains(msg.getReceiver()))
				handleSingleMessageRimotion(infMsg, msg, msg.getReceiver());
			
			handleSingleMessageRimotion(infMsg, msg, msg.getSender());
		}
		else {
			ArrayList <String> partecipants = DatabaseHandler.getInstance().getGroupPartecipants(msg.getGroupId());
			for(String user : partecipants) {
				if(usersNotReceivedMessage.contains(user))
					continue;
				
				handleSingleMessageRimotion(infMsg, msg, user);
			}
		}
	}

	//Se non è la foto ad essere stata cambiata, è lo stato
	private void handleMyInfoChanged(boolean isProfilePic) throws IOException, ClassNotFoundException, SQLException {
		String requester = (String) inputStream.readObject();
		String status;
		File proPic;
		
		InformationMessage msg = new InformationMessage();
		if(isProfilePic) {
			proPic = (File) inputStream.readObject();
			msg.setInformation(Protocol.PHOTO_CHANGE);
			msg.setPacket(proPic);
			if(!DatabaseHandler.getInstance().updateProPic(requester, proPic))
				msg.setPacket("null");
			
			sendMessage(Protocol.PHOTO_CHANGE);
		}
		else {
			status = (String) inputStream.readObject();
			msg.setInformation(Protocol.STATUS_CHANGE);
			msg.setPacket(status);
			if(!DatabaseHandler.getInstance().updateStatus(requester, status))
				msg.setPacket(null);
				
			sendMessage(Protocol.STATUS_CHANGE);
		}
		
		sendObject(msg);
	}
	
	//Questo metodo controlla che il gruppo e che la persona che richiede l'operazione sia il propritario del gruppo
	private boolean checkGroupAndRequester(int groupId, String requester) throws SQLException {
		if(!DatabaseHandler.getInstance().checkGroupExists(groupId))
			return false;
		
		if(!requester.equals(DatabaseHandler.getInstance().getGroupInfo(groupId).getGpOwner()))
			return false;
		
		return true;
	}
	
	//Leggo sempre l'username anche se è già salvato nel server per una doppia sicurezza
	private void handlePasswordChange() throws IOException, SQLException, ClassNotFoundException {
		String username = (String) inputStream.readObject();
		String oldPassword = (String) inputStream.readObject();
		String newPassword = (String) inputStream.readObject();
		
		InformationMessage msg = new InformationMessage();
		msg.setInformation(Protocol.PASSWORD_CHANGE);
		
		User check = DatabaseHandler.getInstance().checkUserLogin(username, oldPassword);
		if(check == null) 
			msg.setPacket(Protocol.INVALID_CREDENTIAL);
		
		else {
			String result = Utilities.checkIfPasswordValid(newPassword);
			if(!result.equals(Utilities.PASSWORD_VALID)) 
				msg.setPacket(result);
			
			else {
				if(DatabaseHandler.getInstance().updatePassword(username, newPassword))
					msg.setPacket(Protocol.REQUEST_SUCCESSFUL);
				else
					msg.setPacket(Protocol.SERVER_ERROR);
			}
		}
		
		sendMessage(Protocol.PASSWORD_CHANGE);
		sendObject(msg);
	}
	
	//Se non è l'immagine di profilo allora è il nome
	private void handleGroupInfoChanged(boolean isProfilePic) throws ClassNotFoundException, IOException, SQLException{
		String requester = (String) inputStream.readObject();
		int groupId = Integer.parseInt((String) inputStream.readObject());
		String newName = "";
		File pic = null;
		if(isProfilePic)
			pic = (File) inputStream.readObject();
		else
			newName = (String) inputStream.readObject();
		
		if(!checkGroupAndRequester(groupId, requester))
			return;
		
		ArrayList <String> listPartecipanti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		if(listPartecipanti.indexOf(requester) == -1)
			return;
		
		InformationMessage msg = new InformationMessage();
		if(isProfilePic) {
			DatabaseHandler.getInstance().updateGroup(groupId, Utilities.getByteArrFromFile(pic));
			msg.setInformation(Protocol.GROUP_PICTURE_CHANGED);
			msg.setPacket(new Pair <Integer, File>(groupId, pic));
		}
		else {
			DatabaseHandler.getInstance().updateGroup(groupId, newName);
			msg.setInformation(Protocol.GROUP_NAME_CHANGED);
			msg.setPacket(new Pair <Integer, String>(groupId, newName));
		}
		
		for(String receiver : listPartecipanti) {	
			ObjectOutputStream destOutput = server.getStream(receiver);
			if(destOutput == null) {
				ChatMessage infMsg = new ChatMessage("null", receiver);
				if(isProfilePic)
					infMsg.setText("PIC_CHANGED:" + groupId);
				else
					infMsg.setText("NAME_CHANGED:" + newName);
				infMsg.setGroupMessage(true);
				infMsg.setTimestamp(Utilities.getCurrentISODate());
				infMsg.setGroupId(groupId);
				DatabaseHandler.getInstance().addPendingMessage(infMsg, receiver);
			}
			else {
				destOutput.writeObject(msg.getInformation());
				destOutput.writeObject(msg);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	//questo metodo gestisce l'aggiunta di un utente in un gruppo
	private void handleGroupAdd() throws IOException, SQLException, ClassNotFoundException {
		String requester = (String) inputStream.readObject();
		int groupId = Integer.parseInt((String) inputStream.readObject());
		Vector <String> users = (Vector <String>) inputStream.readObject();
		
		if(!checkGroupAndRequester(groupId, requester))
			return;
		
		ArrayList <String> listPartecipanti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		if(listPartecipanti.indexOf(requester) == -1) 
			return;
		
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
	
	//Questo metodo controlla, all'accesso di un utente, se ci sono messaggi che non ha ricevuto perchè era offline
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
	
	//Questo metodo gestisce la rimozione in un gruppo da parte dell'admin o l'abbandono spontaneo dell'utente
	private void handleGroupRimotion(boolean autoRimotion) throws ClassNotFoundException, IOException, SQLException {
		String requester = "";
		if(!autoRimotion)
			requester = (String) inputStream.readObject();
		
		int groupId = Integer.parseInt((String) inputStream.readObject());
		String memberRemoved = (String) inputStream.readObject();
		
		if(!DatabaseHandler.getInstance().checkGroupExists(groupId))
			return;
		
		if(!autoRimotion && !requester.equals(DatabaseHandler.getInstance().getGroupInfo(groupId).getGpOwner()))
			return;
		
		ArrayList <String> listPartecipanti = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		//Chi ha richiesto l'operazione non è nemmeno nel gruppo
		if(!autoRimotion && listPartecipanti.indexOf(requester) == -1) 
			return;
		
		
		String information;
		if(!autoRimotion)
			information = Protocol.GROUP_MEMBER_RIMOTION;
		else
			information = Protocol.GROUP_MEMBER_LEFT;
		
		DatabaseHandler.getInstance().removeUserFromGroup(groupId, memberRemoved);
		InformationMessage msg = new InformationMessage();
		msg.setInformation(information);
		msg.setPacket(new Pair <String, Integer>(memberRemoved, groupId));
		for(String receiver : listPartecipanti) {
			ObjectOutputStream destOutput = server.getStream(receiver);
			if(destOutput == null) {
				ChatMessage infMsg = new ChatMessage("null", receiver);
				if(autoRimotion)
					infMsg.setText("LEFT:" + memberRemoved);
				else
					infMsg.setText("REMOVED:" + memberRemoved);
				
				infMsg.setGroupMessage(true);
				infMsg.setTimestamp(Utilities.getCurrentISODate());
				infMsg.setGroupId(groupId);
				DatabaseHandler.getInstance().addPendingMessage(infMsg, receiver);
			}
			else {
				destOutput.writeObject(information);
				destOutput.writeObject(msg);
			}
		}
	}

	//Questo metodo restituisce i partecipanti di un gruppo
	private void handleGroupPartecipants() throws SQLException, ClassNotFoundException, IOException {
		int groupId = Integer.parseInt((String) inputStream.readObject());
		if(!DatabaseHandler.getInstance().checkGroupExists(groupId))
			return;
		
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
	
	//questo metodo gestisce l'eliminazione di un gruppo
	private void handleGroupDeletion() throws ClassNotFoundException, IOException, SQLException {
		String requester = (String) inputStream.readObject();
		int groupId = Integer.parseInt((String) inputStream.readObject());
		
		if(!checkGroupAndRequester(groupId, requester))
			return;
		
		ArrayList <String> partecipants = DatabaseHandler.getInstance().getGroupPartecipants(groupId);
		if(!partecipants.contains(requester))
			return;
		
		DatabaseHandler.getInstance().deleteGroup(groupId);
		
		ChatMessage msg = new ChatMessage("null", "");
		msg.setText("DELETED:" + groupId);
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		msg.setTimestamp(Utilities.getCurrentISODate());
		
		InformationMessage infMsg = new InformationMessage();
		infMsg.setInformation(Protocol.GROUP_DELETION);
		infMsg.setPacket(groupId);
		
		for(String receiver : partecipants) {
			ObjectOutputStream destOutput = server.getStream(receiver);
			if(destOutput == null) {
				DatabaseHandler.getInstance().addPendingMessage(msg, receiver);
			}
			else {
				destOutput.writeObject(Protocol.GROUP_DELETION);
				destOutput.writeObject(infMsg);
			}
		}
	}
	
	//questo metodo restituisce le info su un gruppo, tipo nome, creazione, foto, ecc
	private void handleGroupInformation() throws ClassNotFoundException, IOException, SQLException {
		int groupId = Integer.parseInt((String) inputStream.readObject());
		
		if(!DatabaseHandler.getInstance().checkGroupExists(groupId))
			return;
		
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
			
			if(!fullInfo) 
				msg.setInformation(Protocol.CONTACT_INFORMATION_REQUEST);
			else 
				msg.setInformation(Protocol.CONTACT_FULL_INFORMATION_REQUEST);
				
			
			sendMessage(msg.getInformation());
			sendObject(msg);
		}
		
	}
		
	private void handleOnlineStatusRequest() throws IOException, SQLException, ClassNotFoundException {
		String userToCheck = (String) inputStream.readObject();
		try {
			InformationMessage info = new InformationMessage();
			info.setInformation(Protocol.ONLINE_STATUS_REQUEST);
			
			if(server.checkIsUserLogged(userToCheck)) 
				info.setPacket(userToCheck + ";" + Protocol.USER_ONLINE);	
			else {
				String date = DatabaseHandler.getInstance().getLastAccess(userToCheck);
				if(date == null) 
					info.setPacket(userToCheck + ";" + "null");		
				else {
					String dayDate = Utilities.getDateFromString(date);
					String hour = Utilities.getHourFromStringTrimmed(date);
					info.setPacket(userToCheck + ";" + dayDate + " " + hour);
				}
			}
			
			sendMessage(Protocol.ONLINE_STATUS_REQUEST);
			sendObject(info);
		} catch (NullPointerException e) {
			return;
		}
	}
	
	private void handleContactSearch() throws ClassNotFoundException, IOException, SQLException {
		try {
			String subUsername = (String) inputStream.readObject();
			ArrayList <LongUser> listaRicercati = DatabaseHandler.getInstance().searchUsers(subUsername);
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
		LongUser utente = (LongUser) inputStream.readObject();
		
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

