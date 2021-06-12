package application.logic;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import application.graphics.ChatView;
import application.graphics.CreateChatView;
import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.client.Client;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.Utilities;

//Questa classe gestisce la parte logica della chat
public class ChatLogic {

	private static ChatLogic instance = null;
	private SingleContact myInformation;
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
	
	public void setMyInformation(SingleContact myUser) {
		this.myInformation = myUser;
		
		try {
			LocalDatabaseHandler.getInstance().setUsername(myInformation.getUsername());
			LocalDatabaseHandler.getInstance().createLocalDB();
			contactList = LocalDatabaseHandler.getInstance().retrieveContacts();
			contactList.add(myUser);
			chatList.addAll(LocalDatabaseHandler.getInstance().retrieveSingleChatInfo(contactList));
			chatList.addAll(LocalDatabaseHandler.getInstance().retriveGroupChat(contactList));
			ChatView.getInstance().updateInformation();
			displayAllChat();
		} catch (SQLException e) {
			contactList = new Vector <Contact>();
			e.printStackTrace();
		}
	}
	
	public Contact getMyInformation() {
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
	
	//Mostro tutte le chat a sinistra
	public void displayAllChat() {
		Collections.sort(chatList);
		ChatView.getInstance().getChatMainController().getAllChatVbox().getChildren().clear();
		for(Chat chat : chatList) {
			if(chat instanceof SingleChat && chat.getListMessaggi().isEmpty())
				continue;
			
			ChatView.getInstance().appendChatInMainPanel(chat);
		}
	}
	
	//Mostro la chat selezionata sul pannello a destra
	private void displayCurrentChat() {
		boolean isGroupChat = false;
		ChatView.getInstance().getChatMainController().setChatPane();
		ChatView.getInstance().getChatPaneController().getChatVbox().getChildren().clear();
		if(activeChat instanceof SingleChat)
			ChatView.getInstance().showContactInformation(activeContact, -1);
		else {
			ChatView.getInstance().showContactInformation(activeContact, ((GroupChat) activeChat).getListUtenti().size());
			isGroupChat = true;
		}
		
		String lastUser = "";
		for(Message msg : activeChat.getListMessaggi()) {
			boolean isMyMessage = true;
			if(!msg.getSender().equals(myInformation.getUsername()))
				isMyMessage = false;
			
			if(!isGroupChat)	
				ChatView.getInstance().appendMessageInChat(msg, isMyMessage);
			else {
				ChatView.getInstance().appendGroupMessageInChat(msg, isMyMessage, lastUser);
				lastUser = msg.getSender();
			}
		}
	}
	
	//Mi preparo a mostrare la chat selezionata cercando prima il contatto con cui voglio chattare e dopo la chat corrispondente
	public void setSingleActiveChat(String username) {
		SingleContact contatto = searchContact(username);
		SingleChat chat;
		if(contatto != null) {
			chat = (SingleChat) searchChat(contatto);
			if(chat == null) 
				chat = (SingleChat) createChat(contatto);
			
			activeChat = chat;
			activeContact = contatto;
		}
		//Se il contatto non è presente nei miei contatti, allora significa che ho cliccato su un contatto da una ricerca globale 
		else {
			if(lastSearchContacts == null)
				return; 
			
			for(SingleContact contact : lastSearchContacts) {
				if(contact.getUsername().equals(username)) {
					activeContact = contact;
					contactList.add(activeContact);
					activeChat = createChat(activeContact);
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
	
	public void sendMessage(String text, File attachedImage) {
		ChatMessage msg = new ChatMessage(myInformation.getUsername(), activeContact.getUsername());
		if(activeContact instanceof SingleContact)
			msg.setGroupMessage(false);
		else {
			msg.setGroupMessage(true);
			msg.setGroupId(((GroupContact) activeContact).getGroupId());
		}
    	
    	msg.setText(text);
    	msg.setTimestamp(Utilities.getCurrentISODate());
    	if(attachedImage != null) 
    		msg.setImage(Utilities.getByteArrFromFile(attachedImage));
   
    	//Aggiungo il messaggio alla chat
    	if(Client.getInstance().sendChatMessage(msg)) {
    		activeChat.addMessage(msg);
    		ChatView.getInstance().appendMessageInChat(msg, true);
    		displayAllChat();
    	}
    	//else
    		//TODO show error
	}
	
	//Questo metodo manda i messaggi informativi di una chat di gruppo, tipo : "si è unito"...
	private void sendInformationMessage(int groupId, String text) {
		ChatMessage msg = new ChatMessage("null", "null");
		msg.setTimestamp(Utilities.getCurrentISODate());
		msg.setSender("null");
		msg.setGroupId(groupId);
		msg.setText(text);
		msg.setGroupMessage(true);
		Client.getInstance().sendChatMessage(msg);
	}
	
	//Mostro tutti i miei contatti 
	public void showContactsChoice() {
		CreateChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, false);
		}
	}
	
	public void showContactForGroupCreation() {
		CreateChatView.getInstance().getCreateGroupController().getPartecipantsVBox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, true);
		}
	}
	
	//Mostro i contatti, filtrati per pezzi di username
	public void showContactsChoiceFiltered(String subUsername) {
		CreateChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(contact instanceof SingleContact && contact.getUsername().contains(subUsername)) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, false);
		}
	}
	
	//Mostro i contatti presi da una ricerca globale
	public void showGlobalContact(ArrayList <SingleContact> globalContact) {
		lastSearchContacts = globalContact;
		filterMyContacts();
		for(SingleContact contact : globalContact)
			CreateChatView.getInstance().appendContactInChoiceScreen(contact, true, false);
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
		Client.getInstance().requestContactInformation(sender);
		SingleContact c = new SingleContact(sender);
		contactList.add(c);
		return c;
	}
	
	private GroupContact createGroupContact(int groupId) {
		GroupContact gp = new GroupContact("", groupId);
		contactList.add(gp);
		Client.getInstance().requestGroupInformation(groupId);
		return gp;
	}
	
	//Cerco la chat con il contatto c
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
		else {
			chat = new GroupChat((GroupContact) contact);
			Client.getInstance().requestGroupMembers(((GroupContact) contact).getGroupId());
		}
		
		chatList.add(chat);
		
		return chat;
	}
	
	//Aggiungo alla chat un messaggio in arrivo
	public void addIncomingMessage(ChatMessage msg) {
		Chat chat = null;
		if(!msg.isAGroupMessage()) {
			
			if(!msg.getReceiver().equals(myInformation.getUsername()))
				return;
			
			String sender = msg.getSender();
			SingleContact contact = searchContact(sender);
			if(contact == null) 
				contact = createContact(sender);
			
			chat = searchChat(contact);
			if(chat == null) 
				chat = createChat(contact);
			
			chat.addNewMessage(msg);
			
			if(activeChat == chat)
				ChatView.getInstance().appendMessageInChat(msg, false);
		}			
		else {
			GroupContact contact = searchContact(msg.getGroupId());
			if(contact == null)
				contact = createGroupContact(msg.getGroupId());
			
			chat = searchChat(contact);
			if(chat == null)
				chat = createChat(contact);
			
			chat.addNewMessage(msg);
		
			if(activeChat == chat) {
				ChatView.getInstance().appendGroupMessageInChat(msg, false, ((GroupChat) chat).getUserOfLastMessage());
				((GroupChat) chat).setUserOfLastMessage(msg.getSender());
			}
		}
		
		displayAllChat();
	}

	//recupero i messaggi che mi sono arrivati quando ero offline
	public void retrievePendingMessage(ArrayList <Message> listMessaggi) {
		for(Message msg : listMessaggi) {
			if(!msg.isAGroupMessage()) {
				SingleContact sender = searchContact(msg.getSender());
				if(sender == null)
					sender = createContact(msg.getSender());
				
				SingleChat chat = (SingleChat) searchChat(sender);
				if(chat == null) 
					chat = (SingleChat) createChat(sender);
				
				chat.addNewMessage(msg);
			}
			else {
				GroupContact groupInfo = searchContact(msg.getGroupId());
				if(groupInfo == null)
					groupInfo = createGroupContact(msg.getGroupId());
				
				GroupChat chat = (GroupChat) searchChat(groupInfo);
				if(chat == null)
					chat = (GroupChat) createChat(groupInfo);
				
				chat.addNewMessage(msg);
			}
		}
		
		displayAllChat();
		
	}

	//Se l'online status che mi è arrivato dal server è quello del contatto con cui sto chattando, lo mostro
	public void updateOnlineStatus(String username, String status) {
		if(activeContact.getUsername().equals(username))
			ChatView.getInstance().updateOnlineStatus(status);
	}
	
	public Vector <SingleContact> getPartecipants(ArrayList <String> selectedUsername) {
		Vector <SingleContact> contacts = new Vector <SingleContact>();
		for(String username : selectedUsername)
			contacts.add(searchContact(username));
		
		return contacts;
	}

	public void createGroup(String name, File selectedImage, ArrayList<String> selectedContacts) {
		GroupContact contact = new GroupContact(name, -1);
		contactList.add(contact);
		contact.setProfilePic(Utilities.getByteArrFromFile(selectedImage));
		contact.setOwner(myInformation.getUsername());
		
		GroupChat groupChat = new GroupChat(contact);
		chatList.add(groupChat);
		groupChat.setListUtenti(getPartecipants(selectedContacts));
		groupChat.getListUtenti().add(myInformation);
		Client.getInstance().createGroup(groupChat);
	}

	//Questo metodo aggiorna il groupId del gruppo che ho creato, dopo la risposta del server
	public void updateGroup(String name, Integer groupID) {
		GroupContact gpContact = searchContact(groupID);
		
		if(groupID == -1) {
			//Significa che c'è stato un errore nella creazione del gruppo e lo devo rimuovere
			GroupChat chat = (GroupChat) searchChat(gpContact);
			chatList.remove(chat);
			contactList.remove(gpContact);
		}
		else {
			gpContact.setGroupId(groupID);
			//ora che il gruppo è stato correttamente aggiornato, lo posso salvare sul db
			try {
				LocalDatabaseHandler.getInstance().createGroup(gpContact);
				LocalDatabaseHandler.getInstance().addPartecipantsToGroup(groupID, ((GroupChat) searchChat(gpContact)).getListUtentiusername());
			} catch (SQLException e) {
				e.printStackTrace();
				//TODO show error
			}
			
			sendInformationMessage(groupID, Utilities.getDateFromString(Utilities.getCurrentISODate()));
			sendInformationMessage(groupID, "Il gruppo è stato creato");
		}
		
		displayAllChat();
	}

	//Questo metodo imposta la chat di gruppo come chat attiva
	public void setGroupChatActive(int groupId) {
		GroupContact gpContact = searchContact(groupId);
		GroupChat gpChat = (GroupChat) searchChat(gpContact);
		activeContact = gpContact;
		activeChat = gpChat;
		displayCurrentChat();
	}

	//Questo metodo aggiorna le informazioni su un gruppo e viene chiamato quando un utente riceve per la prima volta un messaggio
	//da un certo gruppo e quindi ha bisogno delle sue informazioni
	public void updateGroupInfo(int groupId, String username, String gpOwner, byte[] proPic) {
		GroupContact gpContact = searchContact(groupId);
		if(gpContact == null)
			gpContact = createGroupContact(groupId);
		
		gpContact.setUsername(username);
		gpContact.setOwner(gpOwner);
		gpContact.setProfilePic(proPic);
		displayAllChat();
		
		try {
			LocalDatabaseHandler.getInstance().createGroup(gpContact);
		} catch (SQLException e) {
			//TODO show error
			e.printStackTrace();
		}
	}

	//Questo metodo aggiorna l'utente e viene chiamato quando riceviamo un messaggio da lui per la prima volta
	//Viene inoltre aggiunto ai nostri contatti
	public void updateUser(String username, String status, byte[] proPic) {
		SingleContact user = searchContact(username);
		if(user == null)
			user = createContact(username);
		
		user.setStatus(status);
		user.setProfilePic(proPic);
		displayAllChat();
		
		try {
			LocalDatabaseHandler.getInstance().registerUser(user);
		} catch (SQLException e) {
			//TODO show error
			e.printStackTrace();
		}
	}

	//Questo metodo aggiorna la lista partecipanti di un gruppo
	public void updateGroupPartecipants(ArrayList<String> utenti, int groupID) {
		Vector <SingleContact> arrUsers = new Vector <SingleContact>();
		for(String user : utenti) {
			SingleContact c = searchContact(user);
			if(c == null)
				c = createContact(user);
			
			arrUsers.add(c);
		}
		
		GroupContact contact = searchContact(groupID);
		GroupChat chat = (GroupChat) searchChat(contact);
		if(chat == null)
			chat = (GroupChat) createChat(contact);
		
		chat.setListUtenti(arrUsers);
		
		try {
			LocalDatabaseHandler.getInstance().addPartecipantsToGroup(groupID, chat.getListUtentiusername());
		} catch (SQLException e) {
			//TODO show error
			e.printStackTrace();
		}
	}
}
