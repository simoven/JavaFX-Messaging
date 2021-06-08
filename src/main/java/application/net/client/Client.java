package application.net.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.misc.LongUser;
import application.net.misc.Protocol;
import application.net.misc.User;
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
			System.out.println("Client online");
			
		} catch (IOException e) {
			System.out.println("Errore di connessione");
			e.printStackTrace();
			//TODO mostra errore connessione
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
		} catch (IOException e) {
			e.printStackTrace();
			//TODO show Error
		}
		
		return false;
	}
	
	public LongUser requestLogin(String username, String password) {
		sendMessage(Protocol.REQUEST_LOGIN);
		sendMessage(username);
		sendMessage(password);
		
		LongUser utente = null;
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			
			String response = (String) inputStream.readObject();

			if(response.equals(Protocol.REQUEST_SUCCESSFUL))
				utente = (LongUser) inputStream.readObject();
			else {
				//showError(response)
			}
		} catch (IOException | ClassNotFoundException e) {
			//showError(Protocol.COMMUNICATION_ERROR)
			e.printStackTrace();
		}
		
		return utente;
	}
	
	public boolean requestRegistration(User utente) {
		sendMessage(Protocol.REQUEST_REGISTRATION);
		sendObject(utente);
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			String response = (String) inputStream.readObject();
			
			if(response.equals(Protocol.REQUEST_SUCCESSFUL))
				return true;
		} catch(IOException | ClassNotFoundException e) {
			//TODO show error
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean sendChatMessage(Message msg) {
		if(msg.isAGroupMessage())
			sendMessage(Protocol.GROUP_MESSAGE_SEND_REQUEST);
		else 
			sendMessage(Protocol.MESSAGE_SEND_REQUEST);
		
		
		if(sendObject(msg)) {
			try {
				LocalDatabaseHandler.getInstance().addMessage((ChatMessage) msg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}
	
	public void requestSearch(String subUsername) {
		sendMessage(Protocol.CONTACTS_SEARCH);
		sendMessage(subUsername);
	}
	
	public void requestOnlineStatus(String userToCheck) {
		sendMessage(Protocol.ONLINE_STATUS_REQUEST);
		sendMessage(userToCheck);
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
					
					else if(requestIncoming.equals(Protocol.CONTACTS_SEARCH))
						msg = (InformationMessage) inputStream.readObject();
					
					else if(requestIncoming.equals(Protocol.ONLINE_STATUS_REQUEST)) 
						msg = (InformationMessage) inputStream.readObject();
										
					else if(requestIncoming.equals(Protocol.MESSAGES_RETRIEVED))
						msg = (InformationMessage) inputStream.readObject();
					
				} catch(ClassNotFoundException e) {
					return null;
				}
				
				return msg;
			}
		};
	}
}
