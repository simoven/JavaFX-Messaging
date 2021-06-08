package application.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import application.logic.ChatLogic;
import application.logic.contacts.Contact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.client.Client;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.Protocol;
import application.net.misc.User;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientSucceedController implements EventHandler<WorkerStateEvent> {

	@Override
	public void handle(WorkerStateEvent event) {
		Message packet = (Message) event.getSource().getValue();
		try {
			if(packet instanceof ChatMessage) {
				ChatLogic.getInstance().addIncomingMessage((ChatMessage) packet);
				LocalDatabaseHandler.getInstance().addMessage((ChatMessage) packet);
			}
			else if(packet instanceof InformationMessage) {
				switch (((InformationMessage) packet).getInformation()) {
					case Protocol.ONLINE_STATUS_REQUEST:
						handleOnlineRequest((InformationMessage) packet);
						break;
	
					case Protocol.MESSAGES_LIST:
						handleMessageRetrieved((InformationMessage) packet);
						break;
						
					case Protocol.CONTACTS_SEARCH:
						handleContactSearch((InformationMessage) packet);
						break;
						
					default:
						break;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Client.getInstance().restart();
	}

	@SuppressWarnings("unchecked")
	//Poiche la classe user è un mezzo di scambio client-server, converto gli user in contatti
	private void handleContactSearch(InformationMessage message) {
		ArrayList <User> listUser = (ArrayList <User>) message.getPacket();
		ArrayList <SingleContact> listContatti = new ArrayList <SingleContact>();
		
		for(User user : listUser) {
			SingleContact contact = new SingleContact(user.getUsername());
			contact.setProfilePic(user.getProPic());
			contact.setStatus(user.getStatus());
			listContatti.add(contact);
		}
		
		ChatLogic.getInstance().showGlobalContact(listContatti);
	}

	@SuppressWarnings("unchecked")
	private void handleMessageRetrieved(InformationMessage message) {
		ArrayList <Message> listMessaggi = (ArrayList<Message>) message.getPacket();
		ChatLogic.getInstance().retrievePendingMessage(listMessaggi);
	}

	private void handleOnlineRequest(InformationMessage message) {
		String status = (String) message.getPacket();
		String [] split = status.split(";");
		ChatLogic.getInstance().updateOnlineStatus(split [0], split [1]);
	}
	
}
