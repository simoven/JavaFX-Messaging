package application.net.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import application.graphics.ChatDialog;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.logic.chat.GroupChat;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.misc.LongUser;
import application.net.misc.Protocol;
import application.net.misc.User;
import application.net.misc.Utilities;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class Client extends Service <Message> {
	
	private Socket socket;
	private static Client instance = null;
	
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	
	private Client() {
		try {
			socket = new Socket("localhost", 8500);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			
		} catch (IOException e) {
			int res = ChatDialog.getInstance().showErrorDialog("Impossibile connettersi al server");
			if(res == ChatDialog.RETRY_OPTION) {
				resetClient();
				getInstance();
			}
			else 
				SceneHandler.getInstance().getWindowFrame().close();
		}
	}
	
	public static Client getInstance() {
		if(instance == null)
			instance = new Client();
		
		return instance;
	}
	
	private boolean sendMessage(String message) {
		return sendObject(message);
	}
	
	public void resetClient() {
		try {
			if(inputStream != null)
				inputStream.close();
			
			if(outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			Utilities.getInstance().logToFile(e.getMessage() + "\n");
		}
		
		inputStream = null;
		outputStream = null;
		instance = null;
	}
	
	private boolean sendObject(Object obj) {
		try {
			outputStream.writeObject(obj);
			outputStream.flush();
			return true;
		} catch (Exception e) {
			ChatDialog.getInstance().showResponseDialog("Impossibile stabilire una connessione con il server");
			ChatLogic.getInstance().resetLogic();
		}
		
		return false;
	}
	
	public LongUser requestLogin(String username, String password) {
		if(!sendMessage(Protocol.REQUEST_LOGIN))
			return null;
		
		sendMessage(username);
		sendMessage(password);
		
		LongUser utente = null;
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			
			String response = (String) inputStream.readObject();

			if(response.equals(Protocol.REQUEST_SUCCESSFUL))
				utente = (LongUser) inputStream.readObject();
			else if(response.equals(Protocol.WRONG_CREDENTIAL)) 
				 ChatDialog.getInstance().showResponseDialog("La combinazione username/password è sbagliata");
			
			else if (response.equals(Protocol.USER_ALREADY_LOGGED)) 
				ChatDialog.getInstance().showResponseDialog("L'account è già attivo su un altro dispositivo");
			
			else {
				int res = ChatDialog.getInstance().showErrorDialog("C'è stato un errore durante l'accesso");
				if(res != ChatDialog.RETRY_OPTION)
					SceneHandler.getInstance().getWindowFrame().close();
			}
		} catch (IOException | ClassNotFoundException e) {
			int res = ChatDialog.getInstance().showErrorDialog("Errore durante la comunicazione con il server");
			if(res == ChatDialog.RETRY_OPTION)
				resetClient();
			else
				SceneHandler.getInstance().getWindowFrame().close();
		}
		
		return utente;
	}
	
	public boolean requestRegistration(User utente) {
		if(!sendMessage(Protocol.REQUEST_REGISTRATION))
			return false;
		
		sendObject(utente);
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			String response = (String) inputStream.readObject();
			
			if(response.equals(Protocol.REQUEST_SUCCESSFUL))
				return true;
			else if(response.equals(Protocol.USER_ALREADY_EXIST))
				ChatDialog.getInstance().showResponseDialog("Questo username esiste già, provane un altro");
			else if(response.equals(Protocol.INVALID_CREDENTIAL))
				ChatDialog.getInstance().showResponseDialog("Le credenziali non sono valide, riprova");
			
		} catch(IOException | ClassNotFoundException e) {
			int res = ChatDialog.getInstance().showErrorDialog("C'è stato un errore di comunicazione con il server");
			if(res == ChatDialog.RETRY_OPTION)
				resetClient();
			else
				SceneHandler.getInstance().getWindowFrame().close();
		}
		
		return false;
	}
	
	public boolean sendChatMessage(Message msg) {
		if(!sendMessage(Protocol.MESSAGE_SEND_REQUEST))
			return false;
		
		if(sendObject(msg) && !msg.getSender().equals("null")) {
			LocalDatabaseHandler.getInstance().addMessage((ChatMessage) msg);
			return true;
		}
		
		return false;
	}
	
	public void requestSearch(String subUsername) {
		if(!sendMessage(Protocol.CONTACTS_SEARCH))
			return;
		
		sendMessage(subUsername);
	}
	
	public void requestOnlineStatus(String userToCheck) {
		if(!sendMessage(Protocol.ONLINE_STATUS_REQUEST))
			return;
		
		sendMessage(userToCheck);
	}
	
	public boolean createGroup(GroupChat groupChat) {
		if(!sendMessage(Protocol.GROUP_CREATION))
			return false;
		
		sendMessage(groupChat.getGroupInfo().getUsername());
		sendMessage(groupChat.getGroupInfo().getOwner());
		byte [] arr = groupChat.getGroupInfo().getProfilePic();
		try {
			if(arr == null)
				sendMessage(Protocol.IMAGE_NULL);
			else {
				sendMessage(Protocol.IMAGE_NOT_NULL);
				outputStream.write(groupChat.getGroupInfo().getProfilePic());
			}
		} catch (IOException e) {
			return false;
		}
		
		for(SingleContact user : groupChat.getListUtenti()) {
			sendMessage(Protocol.GROUP_PARTECIPANT);
			sendMessage(user.getUsername());
		}
		
		sendMessage(Protocol.GROUP_CREATION_DONE);
		return true;
	}
	
	public void requestContactInformation(String user, boolean fullInfo) {
		if(!fullInfo) 
			if(!sendMessage(Protocol.CONTACT_INFORMATION_REQUEST))
				return;
		else 
			if(!sendMessage(Protocol.CONTACT_FULL_INFORMATION_REQUEST))
				return;
	
		sendMessage(user);
	}

	public void requestGroupInformation(int groupId) {
		if(!sendMessage(Protocol.GROUP_INFORMATION_REQUEST))
			return;
		
		sendMessage(Integer.toString(groupId));
	}
	
	public void requestGroupMembers(int groupId) {
		if(!sendMessage(Protocol.GROUP_PARTECIPANT_REQUEST))
			return;
		
		sendMessage(Integer.toString(groupId));
	}
	
	public void removeFromGroup(int groupId, String requester, String username) {
		if(!sendMessage(Protocol.GROUP_MEMBER_RIMOTION))
			return;
		
		sendMessage(requester);
		sendMessage(Integer.toString(groupId));
		sendMessage(username);
	}
	
	public void requestGroupAdd(String requester, Vector <String> users, int groupIdForAdd) {
		if(!sendMessage(Protocol.GROUP_MEMBER_ADD))
			return;
		
		sendMessage(requester);
		sendMessage(Integer.toString(groupIdForAdd));
		sendObject(users);
	}
	
	public void requestGroupQuit(String username, int groupID) {
		if(!sendMessage(Protocol.GROUP_MEMBER_LEFT))
			return;
		
		sendMessage(Integer.toString(groupID));
		sendMessage(username);
	}
	
	public void requestGroupDeletion(int groupId, String requester) {
		if(!sendMessage(Protocol.GROUP_DELETION))
			return;
		
		sendMessage(requester);
		sendMessage(Integer.toString(groupId));
	}
	
	public void updateGroupPicture(File selectedPhoto, int groupId, String requester) {
		if(!sendMessage(Protocol.GROUP_PICTURE_CHANGED))
			return;
		
		sendMessage(requester);
		sendMessage(Integer.toString(groupId));
		sendObject(selectedPhoto);
	}
	
	public void updateGroupName(String requester, String gpName, int groupId) {
		if(!sendMessage(Protocol.GROUP_NAME_CHANGED))
			return;
		
		sendMessage(requester);
		sendMessage(Integer.toString(groupId));
		sendMessage(gpName);
	}
	
	public void requestPasswordChange(String username, String oldPassword, String newPassword) {
		if(!sendMessage(Protocol.PASSWORD_CHANGE))
			return;
		
		sendMessage(username);
		sendMessage(oldPassword);
		sendMessage(newPassword);
	}
	
	public void updateProPic(String username, File img) {
		if(!sendMessage(Protocol.PHOTO_CHANGE))
			return;
		
		sendMessage(username);
		sendObject(img);
	}

	public void updateStatus(String username, String status) {
		if(!sendMessage(Protocol.STATUS_CHANGE))
			return;
		
		sendMessage(username);
		sendMessage(status);
	}
	
	public void removeMessage(ChatMessage msg) {
		if(!sendMessage(Protocol.REMOVE_MESSAGE))
			return;
		
		sendObject(msg);
	}

	@Override
	protected Task <Message> createTask() {
		return new Task<Message>() {
			
			@Override
			protected Message call() throws Exception {
				String requestIncoming = (String) inputStream.readObject();
				Message msg = null;
				
				try {
					if(requestIncoming.equals(Protocol.MESSAGE_SEND_REQUEST)) 
						msg = (ChatMessage) inputStream.readObject();
					else if(requestIncoming.equals(Protocol.SERVER_ERROR))
						ChatDialog.getInstance().showResponseDialog("C'è stato un errore del server");
					else if(requestIncoming.equals(Protocol.BAD_REQUEST))
						ChatDialog.getInstance().showResponseDialog("C'è stato un errore sulla richiesta");
					
					else if(requestIncoming.equals(Protocol.CONTACTS_SEARCH) ||
							requestIncoming.equals(Protocol.ONLINE_STATUS_REQUEST) ||
							requestIncoming.equals(Protocol.MESSAGES_RETRIEVED) ||
							requestIncoming.equals(Protocol.GROUP_CREATION_DONE) ||
							requestIncoming.equals(Protocol.CONTACT_INFORMATION_REQUEST) ||
							requestIncoming.equals(Protocol.GROUP_INFORMATION_REQUEST) ||
							requestIncoming.equals(Protocol.GROUP_PARTECIPANT_REQUEST) ||
							requestIncoming.equals(Protocol.CONTACT_FULL_INFORMATION_REQUEST) || 
							requestIncoming.equals(Protocol.GROUP_MEMBER_RIMOTION) ||
							requestIncoming.equals(Protocol.GROUP_MEMBER_ADD) ||
							requestIncoming.equals(Protocol.GROUP_MEMBER_LEFT) ||
							requestIncoming.equals(Protocol.GROUP_DELETION) ||
							requestIncoming.equals(Protocol.GROUP_PICTURE_CHANGED) ||
							requestIncoming.equals(Protocol.GROUP_NAME_CHANGED) ||
							requestIncoming.equals(Protocol.PASSWORD_CHANGE) ||
							requestIncoming.equals(Protocol.STATUS_CHANGE) ||
							requestIncoming.equals(Protocol.PHOTO_CHANGE) || 
							requestIncoming.equals(Protocol.REMOVE_MESSAGE))
						msg = (InformationMessage) inputStream.readObject();
					
				} catch(ClassNotFoundException e) {
					return null;
				}
				
				return msg;
			}
		};
	}
}

