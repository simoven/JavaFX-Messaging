package application.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import application.logic.ChatLogic;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.net.client.Client;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.LongUser;
import application.net.misc.Protocol;
import application.net.misc.User;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.util.Pair;

public class ClientSucceedController implements EventHandler<WorkerStateEvent> {

	@Override
	public void handle(WorkerStateEvent event) {
		Message packet = (Message) event.getSource().getValue();
		try {
			if(packet instanceof ChatMessage) {
				ChatLogic.getInstance().addIncomingMessage((ChatMessage) packet);
				if(!packet.getSender().equals("null"))
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
						
					case Protocol.GROUP_CREATION_DONE:
						handleGroupCreation((InformationMessage) packet);
						break;
						
					case Protocol.CONTACT_INFORMATION_REQUEST:
						handleContactInformation((InformationMessage) packet, false);
						break;
						
					case Protocol.CONTACT_FULL_INFORMATION_REQUEST:
						handleContactInformation((InformationMessage) packet, true);
						break;
						
					case Protocol.GROUP_INFORMATION_REQUEST:
						handleGroupInfo((InformationMessage) packet);
						break;
						
					case Protocol.GROUP_PARTECIPANT_REQUEST:
						handleGroupPartecipants((InformationMessage) packet);
						break;
						
					case Protocol.GROUP_MEMBER_RIMOTION:
						handlegroupRimotion((InformationMessage) packet);
						break;
						
					case Protocol.GROUP_MEMBER_ADD:
						handleGroupMemberAdd((InformationMessage) packet);
						
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
	private void handleGroupMemberAdd(InformationMessage packet) {
		Pair <String, Integer> pair = (Pair<String, Integer>) packet.getPacket();
		ChatLogic.getInstance().handleGroupAdd(pair.getKey(), pair.getValue());
	}

	@SuppressWarnings("unchecked")
	private void handlegroupRimotion(InformationMessage packet) {
		Pair <String, Integer> pair = (Pair<String, Integer>) packet.getPacket();
		ChatLogic.getInstance().handleGroupRimotion(pair.getKey(), pair.getValue());
	}

	@SuppressWarnings("unchecked")
	private void handleGroupPartecipants(InformationMessage packet) {
		ArrayList <String> utenti = (ArrayList<String>) packet.getPacket();
		ChatLogic.getInstance().updateGroupPartecipants(utenti, packet.getGroupId());
	}

	private void handleGroupInfo(InformationMessage packet) {
		User group;
	
		if(packet.getPacket() instanceof User) {
			group = (User) packet.getPacket();
			ChatLogic.getInstance().updateGroupInfo(packet.getGroupId(), group.getUsername(), group.getGpOwner(), group.getProPic(), group.getCreationDate());
		}
	}
		

	private void handleContactInformation(InformationMessage packet, boolean fullInfo) {
		User utente = (User) packet.getPacket();
		if(!fullInfo)
			ChatLogic.getInstance().updateUser(utente.getUsername(), utente.getStatus(), utente.getProPic());
		else {
			LongUser user = (LongUser) utente;
			ChatLogic.getInstance().showUserInfo(user.getUsername(), user.getName(), user.getLastName(), user.getStatus(), user.getProPic());
		}
	}

	@SuppressWarnings("unchecked")
	private void handleGroupCreation(InformationMessage packet) {
		Pair <String, Integer> pair = (Pair<String, Integer>) packet.getPacket();
		ChatLogic.getInstance().updateGroup(pair.getKey(), pair.getValue());
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
		
		try {
			for(Message msg : listMessaggi) {
				LocalDatabaseHandler.getInstance().addMessage((ChatMessage) msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void handleOnlineRequest(InformationMessage message) {
		String status = (String) message.getPacket();
		String [] split = status.split(";");
		ChatLogic.getInstance().updateOnlineStatus(split [0], split [1]);
	}
	
}
