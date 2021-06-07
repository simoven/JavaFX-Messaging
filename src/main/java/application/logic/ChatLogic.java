package application.logic;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Vector;

import application.controller.ChatMainController;
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
import application.net.misc.Utilities;

public class ChatLogic {

	private static ChatLogic instance = null;
	private User myInformation;
	private Chat activeChat;
	private Contact activeContact;
	private Vector <Contact> contactList;
	private Vector <Chat> chatList;
	
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
			ChatView.getInstance().updateInformation();
			displayAllChat();
			Collections.sort(chatList);
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
		boolean found = false;
		for(Contact c : contactList) {
			if(c instanceof SingleContact && c.getUsername().equals(username)) {
				for(Chat chat : chatList) {
					if(chat instanceof SingleChat && ((SingleChat) chat).getChattingWith().equals(c)) {
						activeChat = chat;
						activeContact = c;
						found = true;
						break;
					}
				}
				if(found)
					break;
			}
		}
		
		if(found)
			displayCurrentChat();
	}
	
	public void showContactsChoice() {
		ChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				ChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact);
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
}
