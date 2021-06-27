package application.controller;

import java.io.File;
import java.util.ArrayList;

import application.graphics.ChatDialog;
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
					handlegroupRimotion((InformationMessage) packet, false);
					break;
					
				case Protocol.GROUP_MEMBER_ADD:
					handleGroupMemberAdd((InformationMessage) packet);
					break;
					
				case Protocol.GROUP_MEMBER_LEFT:
					handlegroupRimotion((InformationMessage) packet, true);
					break;
					
				case Protocol.GROUP_DELETION:
					handleGroupDeletion((InformationMessage) packet);
					break;
					
				case Protocol.GROUP_PICTURE_CHANGED:
					handleGroupInfoChanged((InformationMessage) packet, true);
					break;
					
				case Protocol.GROUP_NAME_CHANGED:
					handleGroupInfoChanged((InformationMessage) packet, false);
					break;
					
				case Protocol.PASSWORD_CHANGE:
					ChatLogic.getInstance().handlePasswordChange(((InformationMessage) packet));
					break;
					
				case Protocol.PHOTO_CHANGE:
					handleMyInfoChanged((InformationMessage) packet, true);
					break;
					
				case Protocol.STATUS_CHANGE:
					handleMyInfoChanged((InformationMessage) packet, false);
					break;
					
				case Protocol.REMOVE_MESSAGE:
					handleMessageRimotion((InformationMessage) packet);
					break;
					
				default:
					break;
			}
		}
		
		Client.getInstance().restart();
	}
	
	private void handleMessageRimotion(InformationMessage packet) {
		ChatMessage msg = (ChatMessage) packet.getPacket();
		msg.setMessageId(-1);
		ChatLogic.getInstance().removeMsg(msg);
	}
	
	private void handleMyInfoChanged(InformationMessage packet, boolean isProPic) {
		if(isProPic) {
			if(packet.getPacket() instanceof String && packet.getPacket().equals("null")) {
				ChatDialog.getInstance().showResponseDialog("C'è stato un problema nel cambiare l'immagine");
			}
			else if(packet.getPacket() instanceof File || packet.getPacket() == null) 
				ChatLogic.getInstance().handleMyPhotoUpdate((File) packet.getPacket());
		}
		else {
			if(packet.getPacket() == null) {
				ChatDialog.getInstance().showResponseDialog("C'è stato un problema nel cambiare lo stato");
			}
			else {
				ChatLogic.getInstance().handleMyStatusChanged((String) packet.getPacket());
			}
		}	
	}

	@SuppressWarnings("unchecked")
	private void handleGroupInfoChanged(InformationMessage packet, boolean isProPic) {
		if(isProPic) {
			Pair <Integer, File> pair = (Pair<Integer, File>) packet.getPacket();
			ChatLogic.getInstance().updateGroupImage(pair.getKey(), pair.getValue());
		}
		else {
			Pair <Integer, String> pair = (Pair<Integer, String>) packet.getPacket();
			ChatLogic.getInstance().updateGroupName(pair.getKey(), pair.getValue());
		}
	}

	private void handleGroupDeletion(InformationMessage packet) {
		int groupId = (int) packet.getPacket();
		ChatLogic.getInstance().handleGroupDeletion(groupId);
	}

	@SuppressWarnings("unchecked")
	private void handleGroupMemberAdd(InformationMessage packet) {
		Pair <String, Integer> pair = (Pair<String, Integer>) packet.getPacket();
		ChatLogic.getInstance().handleGroupAdd(pair.getKey(), pair.getValue());
	}

	@SuppressWarnings("unchecked")
	private void handlegroupRimotion(InformationMessage packet, boolean autoRimotion) {
		Pair <String, Integer> pair = (Pair<String, Integer>) packet.getPacket();
		ChatLogic.getInstance().handleGroupRimotion(pair.getKey(), pair.getValue(), autoRimotion);
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
			ChatLogic.getInstance().registerUpdateUser(utente.getUsername(), utente.getStatus(), utente.getProPic());
		else {
			ChatLogic.getInstance().updateUser(utente.getUsername(), utente.getStatus(), utente.getProPic());
			LongUser user = (LongUser) utente;
			ChatLogic.getInstance().showUserInfo(user.getUsername(), user.getName(), user.getLastName());
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
		
		for(Message msg : listMessaggi) {
			if(msg.isDeleted())
				continue;
			
			LocalDatabaseHandler.getInstance().addMessage((ChatMessage) msg);
		}
	}

	private void handleOnlineRequest(InformationMessage message) {
		String status = (String) message.getPacket();
		String [] split = status.split(";");
		ChatLogic.getInstance().updateOnlineStatus(split [0], split [1]);
	}
	
}
