package application.net.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import application.logic.ImageMessage;
import application.logic.InformationMessage;
import application.logic.Message;
import application.logic.TextMessage;
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
		if(outputStream == null)
			return false;
		
		try {
			outputStream.writeObject(message);
			outputStream.flush();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	public void reset() {
		try {
			if(inputStream != null)
				inputStream.close();
			
			if(outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			
		}
		
		instance = null;
	}
	
	private boolean sendObject(Object obj) {
		try {
			outputStream.writeObject(obj);
			outputStream.flush();
			return true;
		} catch (IOException e) {
			//TODO show Error
		}
		
		return false;
	}
	
	public User requestLogin(String username, String password) {
		sendMessage(Protocol.REQUEST_LOGIN);
		sendMessage(username);
		sendMessage(password);
		
		User utente = null;
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			
			String response = (String) inputStream.readObject();

			if(response.equals(Protocol.REQUEST_SUCCESSFUL))
				utente = (User) inputStream.readObject();
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
	
	public boolean sendChatMessage(Message msg, boolean isTextMessage) {
		if(isTextMessage) {
			if(msg.isAGroupMessage())
				sendMessage(Protocol.GROUP_MESSAGE_SEND_REQUEST);
			else 
				sendMessage(Protocol.MESSAGE_SEND_REQUEST);
		}
		else {
			if(msg.isAGroupMessage())
				sendMessage(Protocol.GROUP_IMAGE_SEND_REQUEST);
			else 
				sendMessage(Protocol.IMAGE_SEND_REQUEST);
		}
		
		sendObject(msg);
		
		try {
			if(inputStream == null)
				inputStream = new ObjectInputStream(socket.getInputStream());
			
			String response = (String) inputStream.readObject();
			
			if(!response.equals(Protocol.REQUEST_SUCCESSFUL))
				return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected Task <Message> createTask() {
		return new Task<Message>() {
			
			@Override
			protected Message call() throws Exception {
				if(inputStream == null)
					inputStream = new ObjectInputStream(socket.getInputStream());
				
				String request = (String) inputStream.readObject();
				Message msg = null;
				
				try {
					if(request.equals(Protocol.MESSAGE_SEND_REQUEST)) {
						msg = (TextMessage) inputStream.readObject();
					}
					else if(request.equals(Protocol.IMAGE_SEND_REQUEST)) {
						msg = (ImageMessage) inputStream.readObject();
					}
					else if(request.equals(Protocol.ONLINE_STATUS_REQUEST)) {
						msg = (InformationMessage) inputStream.readObject();
					}
				} catch(ClassNotFoundException e) {
					return null;
				}
				
				return msg;
			}
		};
	}

}
