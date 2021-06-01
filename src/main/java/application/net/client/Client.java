package application.net.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import application.logic.ImageMessage;
import application.logic.InformationMessage;
import application.logic.Message;
import application.logic.TextMessage;
import application.net.misc.Protocol;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class Client extends Service <Message> {
	
	private Socket socket;
	private static Client instance = null;
	
	private BufferedReader reader;
	
	private Client() {
		try {
			socket = new Socket("localhost", 8000);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
		} catch (IOException e) {
			//TODO mostra errore connessione
		}
	}
	
	public static Client getInstance() {
		if(instance == null)
			instance = new Client();
		
		return instance;
	}
	
	@Override
	protected Task <Message> createTask() {
		return new Task<Message>() {
			
			@Override
			protected Message call() throws Exception {
				String request = reader.readLine();
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Message msg = null;
				
				try {
					if(request.equals(Protocol.MESSAGE_SEND_REQUEST)) {
						msg = (TextMessage) input.readObject();
					}
					else if(request.equals(Protocol.IMAGE_SEND_REQUEST)) {
						msg = (ImageMessage) input.readObject();
					}
					else if(request.equals(Protocol.ONLINE_STATUS_REQUEST)) {
						msg = (InformationMessage) input.readObject();
					}
				} catch(ClassNotFoundException e) {
					return null;
				}
				
				return msg;
			}
		};
	}

}
