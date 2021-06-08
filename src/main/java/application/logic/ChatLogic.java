package application.logic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import application.graphics.ChatView;
import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.User;

public class ChatLogic {

	private static ChatLogic instance = null;
	private User myInformation;
	private Chat activeChat;
	private Contact activeContact;
	private Vector <Contact> contactList;
	private Vector <Chat> chatList;
	private ArrayList <SingleContact> lastSearchContacts;
	
	private ChatLogic() {
		chatList = new Vector <Chat> ();
	}
	
	public static ChatLogic getInstance() {
		if(instance == null)
			instance = new ChatLogic();
		
		return instance;
	}
	
	public void setMyInformation(User myUser) {
		this.myInformation = myUser;
		
		try {
			LocalDatabaseHandler.getInstance().setUsername(myInformation.getUsername());
			LocalDatabaseHandler.getInstance().createLocalDB();
			contactList = LocalDatabaseHandler.getInstance().retrieveContacts();
			chatList.addAll(LocalDatabaseHandler.getInstance().retrieveSingleChatInfo(contactList));
			chatList.addAll(LocalDatabaseHandler.getInstance().retriveGroupChat(contactList));
			ChatView.getInstance().updateInformation();
			displayAllChat();
		} catch (SQLException e) {
			contactList = new Vector <Contact>();
			e.printStackTrace();
		}
	}
	
	public User getMyInformation() {
		return myInformation;
	}
	
	public String getMyUsername() {
		return myInformation.getUsername();
	}
	
	public Contact getActiveContact() {
		return activeContact;
	}
	
	public Vector<Contact> getContactList() {
		return contactList;
	}
	
	public void displayAllChat() {
		Collections.sort(chatList);
		ChatView.getInstance().getChatMainController().getAllChatVbox().getChildren().clear();
		for(Chat chat : chatList) {
			if(!chat.getListMessaggi().isEmpty())
				ChatView.getInstance().appendChatInMainPanel(chat);
		}
	}
	
	private void displayCurrentChat() {
		ChatView.getInstance().getChatMainController().setChatPane();
		ChatView.getInstance().getChatPaneController().getChatVbox().getChildren().clear();
		ChatView.getInstance().showContactInformation(activeContact);
		for(Message msg : activeChat.getListMessaggi()) {
			boolean isMyMessage = true;
			if(!msg.getSender().equals(myInformation.getUsername()))
				isMyMessage = false;
			
			ChatView.getInstance().appendMessageInChat(msg, isMyMessage);
		}
	}
	
	public void setSingleActiveChat(String username) {
		boolean chatFound = false;
		boolean contactFound = false;
		for(Contact c : contactList) {
			if(c instanceof SingleContact && c.getUsername().equals(username)) {
				contactFound = true;
				activeContact = c;
				for(Chat chat : chatList) {
					if(chat instanceof SingleChat && ((SingleChat) chat).getChattingWith().equals(c)) {
						activeChat = chat;
						chatFound = true;
						break;
					}
				}
				if(chatFound)
					break;
				else {
					activeChat = createChat(activeContact);
					chatList.add(activeChat);
				}
			}
		}
		
		//Se il contatto non è presente nei miei contatti, allora significa che ho cliccato su un contatto da una ricerca globale 
		if(!contactFound) {
			if(lastSearchContacts == null)
				return; 
			
			for(SingleContact contact : lastSearchContacts) {
				if(contact.getUsername().equals(username)) {
					activeContact = contact;
					contactList.add(activeContact);
					activeChat = createChat(activeContact);
					chatList.add(activeChat);
					try {
						LocalDatabaseHandler.getInstance().registerUser(contact);
					} catch (SQLException e) {
						//TODO Mostra errore 
					}
					break;
				}
			}
		}
		
		displayCurrentChat();
	}
	
	public void addMessageInChat(ChatMessage msg) {
		activeChat.addMessage(msg);
		ChatView.getInstance().appendMessageInChat(msg, true);
		displayAllChat();
	}
	
	public void showContactsChoice() {
		ChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				ChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false);
		}
	}
	
	public void showContactsChoiceFiltered(String subUsername) {
		ChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && contact.getUsername().contains(subUsername)) 
				ChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false);
		}
	}
	
	public void showGlobalContact(ArrayList <SingleContact> globalContact) {
		lastSearchContacts = globalContact;
		filterMyContacts();
		for(SingleContact contact : globalContact)
			ChatView.getInstance().appendContactInChoiceScreen(contact, true);
	}
	
	private void filterMyContacts() {
		//Rimuovo dalla ricerca i contatti già miei
		for(Contact c : contactList) {
			int idx = lastSearchContacts.indexOf(c);
			if(idx != -1)
				lastSearchContacts.remove(idx);
		}
	}
	
	private SingleContact searchContact(String username) {
		for(Contact c : contactList) 
			if(c instanceof SingleContact && c.getUsername().equals(username))
				return (SingleContact) c;
		
		return null;
	}
	
	private GroupContact searchContact(int groupId) {
		for(Contact c : contactList) 
			if(c instanceof GroupContact && ((GroupContact) c).getGroupId() == groupId)
				return (GroupContact) c;
		
		return null;
	}
	
	private SingleContact createContact(String sender) {
		//Poichè non è nei miei contatti, richiedo informazioni su di esso
		//Client.getInstance().requestContactInformation(sender);
		SingleContact c = new SingleContact(sender);
		contactList.add(c);
		return c;
	}
	
	private Chat searchChat(Contact c) {
		for(Chat chat : chatList) {
			if(c instanceof SingleContact && chat instanceof SingleChat) {
				SingleChat sChat = (SingleChat) chat;
				SingleContact sC = (SingleContact) c;
				if(sChat.getChattingWith().getUsername().equals(sC.getUsername()))
					return chat;
			}
			else if (c instanceof GroupContact && chat instanceof GroupChat){
				GroupChat gChat = (GroupChat) chat;
				GroupContact sC = (GroupContact) c;
				if(gChat.getGroupInfo().getGroupId() == sC.getGroupId())
					return chat;
			}
		}
		
		return null;
	}
	
	private Chat createChat(Contact contact) {
		Chat chat;
		if(contact instanceof SingleContact) 
			chat = new SingleChat((SingleContact) contact);
		else
			chat = new GroupChat((GroupContact) contact);
		
		return chat;
	}
	
	public void addIncomingMessage(ChatMessage msg) {
		if(!msg.getReceiver().equals(myInformation.getUsername())) {
			return;
		}
		
		Chat chat = null;
		if(!msg.isAGroupMessage()) {
			String sender = msg.getSender();
			SingleContact contact = searchContact(sender);
			if(contact == null) {
				contact = createContact(sender);
				contactList.add(contact);
			}
			
			chat = searchChat(contact);
			if(chat == null) {
				chat = createChat(contact);
				chatList.add(0, chat);
			}
			
			chat.addNewMessage(msg);
		}			
		else {
			int idGroup = msg.getGroupId();
			GroupContact contact = searchContact(idGroup);
			
			chat = searchChat(contact);
			chat.addNewMessage(msg);
		}
		
		if(activeChat == chat)
			ChatView.getInstance().appendMessageInChat(msg, false);
		
		displayAllChat();
	}

	public void retrievePendingMessage(ArrayList<Message> listMessaggi) {
		for(Message msg : listMessaggi) {
			if(!msg.isAGroupMessage()) {
				SingleContact sender = searchContact(msg.getSender());
				if(sender == null) {
					sender = createContact(msg.getSender());
					contactList.add(sender);
				}
				
				SingleChat chat = (SingleChat) searchChat(sender);
				if(chat == null) {
					chat = (SingleChat) createChat(sender);
					chatList.add(chat);
				}
				
				chat.addNewMessage(msg);
			}
			else {
				//TODO
			}
		}
		
		displayAllChat();
		
	}

	public void updateOnlineStatus(String username, String status) {
		if(activeContact.getUsername().equals(username))
			ChatView.getInstance().updateOnlineStatus(status);
		
	}
}
