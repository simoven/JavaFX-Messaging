package application.logic;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import application.graphics.ChatView;
import application.graphics.ContactInfoView;
import application.graphics.CreateChatView;
import application.graphics.SceneHandler;
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
	
	public Chat getActiveChat() {
		return activeChat;
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
	
	//Pulisco la chat corrente
	public void clearCurrentChat() {
		if(activeChat == null)
			return;
		
		activeChat.clearChat();
		if(activeChat instanceof GroupChat) {
			if(((GroupChat) activeChat).getGroupInfo().isDeleted()) {
				LocalDatabaseHandler.getInstance().deleteGroup(((GroupChat) activeChat).getGroupInfo().getGroupId());
				contactList.remove(((GroupChat) activeChat).getGroupInfo());
				chatList.remove(activeChat);
				activeChat = null;
			}
			else
				LocalDatabaseHandler.getInstance().clearGroupChat(((GroupChat) activeChat).getGroupInfo().getGroupId());
		}
		else
			LocalDatabaseHandler.getInstance().clearChat(((SingleChat) activeChat).getChattingWith().getUsername());
		
		displayCurrentChat();
		displayAllChat();
	}
	
	//Mostro la chat selezionata sul pannello a destra
	private void displayCurrentChat() {
		if(activeChat == null)
			return;
		
		boolean isGroupChat = false;
		SceneHandler.getInstance().setChatPane();
		ChatView.getInstance().getChatPaneController().getChatVbox().getChildren().clear();
		if(activeChat instanceof SingleChat)
			ChatView.getInstance().showContactInformation(activeContact, -1);
		else {
			if(((GroupChat) activeChat).getListUtenti() == null)
				ChatView.getInstance().showContactInformation(activeContact, 0);
			else
				ChatView.getInstance().showContactInformation(activeContact, ((GroupChat) activeChat).getListUtenti().size());
			isGroupChat = true;
		}
		
		activeChat.setUnreadedMessage(false);
		String lastUser = "";
		long lastMessageDateStamp = 0;
		for(Message msg : activeChat.getListMessaggi()) {
			boolean isMyMessage = true;
			if(!msg.getSender().equals(myInformation.getUsername()))
				isMyMessage = false;
				
			if(msg.getMessageDateStamp() > lastMessageDateStamp) {
				ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
				lastMessageDateStamp = msg.getMessageDateStamp();
			}
			
			if(!isGroupChat)	
				ChatView.getInstance().appendMessageInChat(msg, isMyMessage, "");
			else {
				ChatView.getInstance().appendMessageInChat(msg, isMyMessage, lastUser);
				lastUser = msg.getSender();
				((GroupChat) activeChat).setUserOfLastMessage(lastUser);
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
					activeContact.setVisible(false);
					SingleChat sChat = (SingleChat) searchChat(contact);
					if(sChat == null) 
						activeChat = createChat(activeContact);
					else
						activeChat = createChat(activeContact);
					
					if(contactList.indexOf(contact) == -1) {
						contactList.add(contact);
						registerUser(contact, false);
						contact.setVisible(false);
					}
					break;
				}
			}
		}
		
		Client.getInstance().requestOnlineStatus(username);
		displayCurrentChat();
	}
	
	public void sendMessage(String text, File attachedImage) {
		if(activeChat == null)
			return;
		
		ChatMessage msg = new ChatMessage(myInformation.getUsername(), activeContact.getUsername());
		if(activeContact instanceof SingleContact)
			msg.setGroupMessage(false);
		else {
			msg.setGroupMessage(true);
			msg.setGroupId(((GroupContact) activeContact).getGroupId());
			
			if(!((GroupChat) activeChat).getListUtenti().contains(myInformation))
				return;
			
			if(((GroupChat) activeChat).getGroupInfo().isDeleted())
				return;
		}
    	
    	msg.setText(text);
    	msg.setTimestamp(Utilities.getCurrentISODate());
    	if(attachedImage != null) 
    		msg.setImage(Utilities.getByteArrFromFile(attachedImage));
   
    	//Aggiungo il messaggio alla chat
    	if(Client.getInstance().sendChatMessage(msg)) {
    		if(msg.getMessageDateStamp() > activeChat.getLastMessageDateStamp()) 
				ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
    		
    		activeChat.addMessage(msg);
    		ChatView.getInstance().appendMessageInChat(msg, true, "");
    		if(activeChat instanceof GroupChat)
    			((GroupChat) activeChat).setUserOfLastMessage(msg.getSender());
    		
    		displayAllChat();
    	}
    	//else
    		//TODO show error
	}
	
	//Questo metodo manda i messaggi informativi di una chat di gruppo, tipo : "si è unito"...
	private void sendInformationMessage(int groupId, String text) {
		ChatMessage msg = createInformationMessage(text);
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		Client.getInstance().sendChatMessage(msg);
	}
	
	private ChatMessage createInformationMessage(String text) {
		ChatMessage msg = new ChatMessage("null", "null");
		msg.setTimestamp(Utilities.getCurrentISODate());
		msg.setSender("null");
		msg.setText(text);
		return msg;
	}
	
	//Mostro tutti i miei contatti 
	public void showContactsChoice() {
		CreateChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(!contact.isVisible())
				continue;
			
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, false, false);
		}
	}
	
	public void showContactForGroupCreation() {
		CreateChatView.getInstance().getCreateGroupController().getPartecipantsVBox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(!contact.isVisible())
				continue;
			
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, true, false);
		}
	}
	
	//Mostro i contatti, filtrati per pezzi di username
	public void showContactsChoiceFiltered(String subUsername, boolean isForGroupAdd) {
		CreateChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(!contact.isVisible())
				continue;
			
			if(contact instanceof SingleContact && contact.getUsername().contains(subUsername)) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, false, isForGroupAdd);
		}
	}
	
	//Mostro i contatti presi da una ricerca globale
	public void showGlobalContact(ArrayList <SingleContact> globalContact) {
		lastSearchContacts = globalContact;
		filterMyContacts();
		for(SingleContact contact : globalContact)
			CreateChatView.getInstance().appendContactInChoiceScreen(contact, true, false, false);
	}
	
	private void filterMyContacts() {
		//Rimuovo dalla ricerca i contatti già miei
		for(Contact c : contactList) {
			if(!c.isVisible())
				continue;
			
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
		Client.getInstance().requestContactInformation(sender, false);
		SingleContact c = new SingleContact(sender);
		contactList.add(c);
		return c;
	}
	
	private GroupContact createGroupContact(int groupId) {
		GroupContact gp = new GroupContact("Gruppo", groupId);
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
		
		Contact contact;
		if(!msg.isAGroupMessage()) {
			if(!msg.getReceiver().equals(myInformation.getUsername()))
				return;
			
			contact = searchContact(msg.getSender());
			if(contact == null)
				contact = createContact(msg.getSender());
		}
		else {
			contact = searchContact(msg.getGroupId());
			if(contact == null)
				contact = createGroupContact(msg.getGroupId());
		}
			
		chat = searchChat(contact);
		if(chat == null) 
			chat = createChat(contact);
			
		if(msg.getMessageDateStamp() > chat.getLastMessageDateStamp()) 
			if(activeChat == chat)
				ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
		
			
		chat.addNewMessage(msg);
			
		if(activeChat == chat) {
			if(chat instanceof SingleChat)
				ChatView.getInstance().appendMessageInChat(msg, false, "");
			else
				ChatView.getInstance().appendMessageInChat(msg, false, ((GroupChat) chat).getUserOfLastMessage());
		}
		
		if(chat instanceof GroupChat)
			((GroupChat) chat).setUserOfLastMessage(msg.getSender());		
		
		displayAllChat();
	}

	//Controllo se nei messaggi che mi sono arrivati ci sono messaggi dal server, tipo "utente x è stato rimosso dal gruppo"
	//La struttura del messaggio è OPERAZIONE:username
	private void checkGroupMsgText(ChatMessage msg) {
		GroupContact contact = searchContact(msg.getGroupId());
		if(contact == null)
			return;
		
		GroupChat chat = (GroupChat) searchChat(contact);
		
		if(msg.getText().contains(":")) {
			String [] split = msg.getText().split(":");
			if(split [0].equals("REMOVED") && chat != null) {
				if(split [1].equals(ChatLogic.getInstance().getMyInformation().getUsername())) {
					msg.setText("Sei stato rimosso");
					removeFromGroup(chat, myInformation.getUsername(), msg);
				}
				else {
					msg.setText(split [1] + " è stato rimosso");
					removeFromGroup(chat, split [1], msg);
				}
			}
			else if(split [0].equals("ADDED") && chat != null) {
				if(split [1].equals(ChatLogic.getInstance().getMyInformation().getUsername())) {
					msg.setText("Sei stato aggiunto");
					addedToGroup(myInformation.getUsername(), chat, msg);
				}
				else {
					msg.setText(split [1] + " è stato aggiunto");
					addedToGroup(split [1], chat, msg);
				}
			}
			else if(split [0].equals("LEFT") && chat != null) {
				if(split [1].equals(ChatLogic.getInstance().getMyInformation().getUsername())) {
					msg.setText("Hai abbandonato");
					removeFromGroup(chat, myInformation.getUsername(), msg);
				}
				else {
					msg.setText(split [1] + " ha abbandonato");
					removeFromGroup(chat, split [1], msg);
				}
			}
			else if(split [0].equals("DELETED")) {
				int groupId = Integer.parseInt(split [1]);
				msg.setText("Il gruppo è stato eliminato");
				//Significa che il gruppo nemmeno esisteva nelle mie chat (Ero offline tra la creazione e l'eliminazione)
				if(!handleGroupDeletion(groupId))
					msg.setGroupId(-2);
			}
			else if(split [0].equals("PIC_CHANGED")) {
				int groupId = Integer.parseInt(split [1]);
				msg.setText("L' immagine di profilo è stata cambiata");
				Client.getInstance().requestGroupInformation(groupId);
				LocalDatabaseHandler.getInstance().addMessage(msg);
			}
		}
	}
	
	//recupero i messaggi che mi sono arrivati quando ero offline
	public void retrievePendingMessage(ArrayList <Message> listMessaggi) {
		for(Message msg : listMessaggi) {
			Chat chat;
			Contact msgContact;
			
			if(!msg.isAGroupMessage()) {
				msgContact = searchContact(msg.getSender());
				if(msgContact == null)
					msgContact = createContact(msg.getSender());
			}
			else {
				msgContact = searchContact(msg.getGroupId());
				if(msgContact == null)
					msgContact = createGroupContact(msg.getGroupId());
				
				if(msg instanceof ChatMessage && msg.getSender().equals("null"))
					checkGroupMsgText((ChatMessage) msg);
			}
			
			//Significa che ho ricevuto un messaggio di eliminazione gruppo riferito a un gruppo che da me nemmeno esiste, quindi lo skippo
			if(msg.isAGroupMessage() && msg.getGroupId() == -2)
				continue;
			
			chat = searchChat(msgContact);
			if(chat == null) 
				chat = createChat(msgContact);
			
			chat.addNewMessage(msg);
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
		contact.setCreationDate(Utilities.getDateFromString(Utilities.getCurrentISODate()));
		
		GroupChat groupChat = new GroupChat(contact);
		chatList.add(groupChat);
		groupChat.setListUtenti(getPartecipants(selectedContacts));
		groupChat.getListUtenti().add(myInformation);
		Client.getInstance().createGroup(groupChat);
	}

	//Questo metodo aggiorna il groupId del gruppo che ho creato, dopo la risposta del server
	public void updateGroup(String name, Integer groupID) {
		GroupContact gpContact = null;
		for(int i = 0; i < contactList.size(); ++i) {
			if(contactList.get(i) instanceof GroupContact && contactList.get(i).getUsername().equals(name)) {
				if(((GroupContact) contactList.get(i)).getGroupId() == -1) {
					gpContact = (GroupContact) contactList.get(i);
					break;
				}
			}
		}
		
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
	//Oppure viene chiamato quando devo aggiornare l'immagine di profilo di un gruppo
	public void updateGroupInfo(int groupId, String username, String gpOwner, byte[] proPic, String creationDate) {
		GroupContact gpContact = searchContact(groupId);
		if(gpContact == null)
			gpContact = createGroupContact(groupId);
		
		gpContact.setUsername(username);
		gpContact.setOwner(gpOwner);
		gpContact.setProfilePic(proPic);
		gpContact.setCreationDate(creationDate);
		displayAllChat();
		
		try {
			if(LocalDatabaseHandler.getInstance().groupExists(groupId))
				LocalDatabaseHandler.getInstance().updateGroup(groupId, proPic);
			else
				LocalDatabaseHandler.getInstance().createGroup(gpContact);
		} catch (SQLException e) {
			//TODO show error
			e.printStackTrace();
		}
	}

	//Questo metodo aggiorna l'utente e viene chiamato quando riceviamo un messaggio da lui per la prima volta
	//Viene inoltre aggiunto ai nostri contatti
	public void firstRegisterUser(String username, String status, byte[] proPic) {
		SingleContact user = searchContact(username);
		if(user == null) 
			user = createContact(username);
		
		user.setStatus(status);
		user.setProfilePic(proPic);
		user.setVisible(false);
		registerUser(user, false);
		displayAllChat();
	}
	
	public void updateUser(String username, String status, byte[] proPic) {
		SingleContact user = searchContact(username);
		if(user == null) 
			return;
		
		user.setStatus(status);
		user.setProfilePic(proPic);
		LocalDatabaseHandler.getInstance().modifyUser(user);
		if(user.equals(activeContact))
			ChatView.getInstance().showContactInformation(user, -1);
		displayAllChat();
	}

	private void registerUser(SingleContact user, boolean visible) {
		try {
			LocalDatabaseHandler.getInstance().registerUser(user, visible);
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

	public void showChatFiltered(String subText) {
		for(Chat chat : chatList) {
			String name;
			
			if(chat instanceof SingleChat)
				name = ((SingleChat) chat).getChattingWith().getUsername();
			else
				name = ((GroupChat) chat).getGroupInfo().getUsername();
			
			if(name.toLowerCase().contains(subText.toLowerCase()))
				ChatView.getInstance().appendChatInMainPanel(chat);
		}
	}
	
	//Richiede informazioni per mostrare il contatto sulla schermata ddi info
	public void requestInfoForContactPane() {
		if(activeContact instanceof SingleContact)
			Client.getInstance().requestContactInformation(activeContact.getUsername(), true);
		else {
			boolean iAmRemoved = false;
			if(!((GroupChat) activeChat).getListUtenti().contains(myInformation))
				iAmRemoved = true;
			
			boolean iAmOwner = true;
			if(!((GroupContact) activeContact).getOwner().equals(myInformation.getUsername()))
				iAmOwner = false;
			
			ContactInfoView.getInstance().showGroupInfo((GroupChat) activeChat, iAmOwner, iAmRemoved);
		}		
	}

	public void showUserInfo(String username, String name, String lastName) {
		boolean isSavedContact = true;
		SingleContact contact = searchContact(username);
		
		if(contact == null)
			return;
		
		if(!contact.isVisible())
			isSavedContact = false;
			
		ContactInfoView.getInstance().showInfo(username, name, lastName, contact.getStatus(), contact.getProfilePic(), isSavedContact);
	}

	public void setContactVisibility(String substring, boolean isVisible) {
		SingleContact contact = searchContact(substring);
		contact.setVisible(isVisible);
		if(isVisible)
			LocalDatabaseHandler.getInstance().setVisible(substring);
		else
			LocalDatabaseHandler.getInstance().setInvisible(substring);
	}
	
	public void deleteGroup(int groupId) {
		Client.getInstance().requestGroupDeletion(groupId, myInformation.getUsername());
	}
	
	public void requestRimotion(GroupChat chat, SingleContact contact) {
		if(chat.getListUtenti().indexOf(contact) != -1) {
			if(chat.getGroupInfo().getOwner().equals(contact.getUsername()))
				deleteGroup(chat.getGroupInfo().getGroupId());
			else
				Client.getInstance().removeFromGroup(chat.getGroupInfo().getGroupId(), myInformation.getUsername(), contact.getUsername());
		}
	}

	public boolean removeFromGroup(GroupChat chat, String username, ChatMessage msg) {
		SingleContact contact = searchContact(username);
		if(contact == null)
			return false;
		
		if(chat.getListUtenti() == null)
			return false;
		
		int idx = chat.getListUtenti().indexOf(contact);
		if(idx != -1)
			chat.getListUtenti().remove(idx);
		
		try {
			LocalDatabaseHandler.getInstance().addMessage(msg);
			LocalDatabaseHandler.getInstance().removeFromGroup(username, chat.getGroupInfo().getGroupId());
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	//Questo metodo gestisce il messaggio di rimozione da parte del server
	public void handleGroupRimotion(String userRemoved, Integer groupId, boolean autoRimotion) {
		GroupContact contact = searchContact(groupId);
		GroupChat chat = (GroupChat) searchChat(contact);
		
		//In base a chi è stato rimosso, preparo il messaggio da mostrare
		ChatMessage msg;
		if(userRemoved.equals(myInformation.getUsername())) {
			if(autoRimotion)
				msg = createInformationMessage("Hai abbandonato");
			else
				msg = createInformationMessage("Sei stato rimosso");
		}
		else {
			if(autoRimotion)
				msg = createInformationMessage(userRemoved + " ha abbandonato");
			else
				msg = createInformationMessage(userRemoved + " è stato rimosso");
		}
		
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		
		//Se la rimozione è avvenuta correttamente, mostro il messaggio 
		if(removeFromGroup(chat, userRemoved, msg)) {
			if(activeChat.equals(chat)) {
				if(msg.getMessageDateStamp() > activeChat.getLastMessageDateStamp()) 
					ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
				
				ChatView.getInstance().appendMessageInChat(msg, false, "null");
				ChatView.getInstance().showContactInformation(contact, chat.getListUtenti().size());
			}
			
			chat.addNewMessage(msg);
			displayAllChat();
		}
	}

	//Questo metodo mostra i contatti che non sono in un gruppo nella schermata per aggiungerli
	public void requestAddToGroup(GroupChat chat) {
		SceneHandler.getInstance().setAllContactsPane();
		CreateChatView.getInstance().changeButtonUse(true);
		CreateChatView.getInstance().setGroupIdForAdd(chat.getGroupInfo().getGroupId());
		ArrayList <SingleContact> contactsToAdd = new ArrayList <SingleContact>();
		for(Contact contact : contactList)
			if(contact instanceof SingleContact && chat.getListUtenti().indexOf(contact) == -1)
				contactsToAdd.add((SingleContact) contact);
		
		CreateChatView.getInstance().clearContactVBox();
		for(SingleContact contact : contactsToAdd)
			CreateChatView.getInstance().appendContactInChoiceScreen(contact, false, true, true);
	}

	public void addContactsToGroup(Vector <String> selectedContacts, int groupIdForAdd) {
		Client.getInstance().requestGroupAdd(myInformation.getUsername(), selectedContacts, groupIdForAdd);
	}
	
	private boolean addedToGroup(String user, GroupChat chat, ChatMessage msg) {
		SingleContact contactToAdd = searchContact(user);
		if(contactToAdd == null) {
			contactToAdd = createContact(user);
			registerUser(contactToAdd, false);
		}
		
		if(chat == null)
			return false;
		
		if(chat.getListUtenti() == null)
			return false;
		
		chat.getListUtenti().add(contactToAdd);
		
		try {
			LocalDatabaseHandler.getInstance().addMessage(msg);
			LocalDatabaseHandler.getInstance().addPartecipantToGroup(chat.getGroupInfo().getGroupId(), user);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void handleGroupAdd(String userAdded, Integer groupId) {
		CreateChatView.getInstance().changeButtonUse(false);
		GroupContact contact = searchContact(groupId);
		GroupChat chat = (GroupChat) searchChat(contact);
		
		ChatMessage msg;
		if(!userAdded.equals(myInformation.getUsername()))
			msg = createInformationMessage(userAdded + " è stato aggiunto");
		else
			msg = createInformationMessage("Sei stato aggiunto");
		
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		
		if(addedToGroup(userAdded, chat, msg)) {
			if(activeChat.equals(chat)) {
				if(msg.getMessageDateStamp() > chat.getLastMessageDateStamp()) 
					ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
				
				ChatView.getInstance().appendMessageInChat(msg, false, "null");
				ChatView.getInstance().showContactInformation(contact, chat.getListUtenti().size());
			}
			
			chat.addNewMessage(msg);
			displayAllChat();
		}
	}
	
	public Vector <GroupChat> getCommonGroups(String username) {
		Vector <GroupChat> commonChats = new Vector <GroupChat>();
		SingleContact contact = searchContact(username);
		if(contact == null)
			return commonChats;
		
		for(Chat chat : chatList) {
			if(chat instanceof GroupChat && ((GroupChat) chat).getListUtenti().contains(myInformation)) {
				if(((GroupChat) chat).getListUtenti().contains(contact))
					commonChats.add((GroupChat) chat);
			}
		}
		
		return commonChats;
	}

	//Questo metodo serve ad abbandonare un gruppo
	public void leftGroup(GroupChat chat) {
		if(chat.getListUtenti().contains(myInformation))
			Client.getInstance().requestGroupQuit(myInformation.getUsername(), chat.getGroupInfo().getGroupId());
	}

	public boolean handleGroupDeletion(int groupId) {
		GroupContact contact = searchContact(groupId);
		if(contact == null)
			return false;
		
		GroupChat chat = (GroupChat) searchChat(contact);
		if(chat == null)
			return false;
		
		contact.setUsername("Gruppo eliminato");
		contact.setProfilePic(null);
		ChatMessage msg = createInformationMessage("Il gruppo è stato eliminato");
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		chat.addNewMessage(msg);
		
		displayAllChat();
		
		if(chat.equals(activeChat)) {
			if(msg.getMessageDateStamp() > chat.getLastMessageDateStamp()) 
				ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
			
			ChatView.getInstance().appendMessageInChat(msg, false, "null");
			ChatView.getInstance().showContactInformation(contact, chat.getListUtenti().size());
		}
		
		LocalDatabaseHandler.getInstance().setGroupDeleted(groupId);
		LocalDatabaseHandler.getInstance().addMessage(msg);
		
		return true;
	}

	//Questo metodo richiede di cambiare immagine di profilo del gruppo
	public void groupPictureChanged(File selectedPhoto) {
		if(activeChat instanceof GroupChat) {
			if(((GroupChat) activeChat).getGroupInfo().getProfilePic() == null && selectedPhoto == null)
				return;
			
			Client.getInstance().updateGroupPicture(selectedPhoto, ((GroupChat) activeChat).getGroupInfo().getGroupId(), myInformation.getUsername());
		}
	}

	public void updateGroupImage(Integer groupId, File pic) {
		GroupContact contact = searchContact(groupId);
		if(contact == null)
			return;
		
		GroupChat chat = (GroupChat) searchChat(contact);
		if(chat == null)
			return;
		
		ChatMessage chatMsg = createInformationMessage("L'immagine di profilo è stata cambiata");
		chatMsg.setGroupId(groupId);
		chatMsg.setGroupMessage(true);
		contact.setProfilePic(Utilities.getByteArrFromFile(pic));
		
		chat.addNewMessage(chatMsg);
		displayAllChat();
		
		if(chat.equals(activeChat)) {
			if(chatMsg.getMessageDateStamp() > chat.getLastMessageDateStamp()) 
				ChatView.getInstance().appendMessageInChat(createInformationMessage(chatMsg.getSentDate()), false, "");
			
			ChatView.getInstance().appendMessageInChat(chatMsg, false, "null");
			ChatView.getInstance().showContactInformation(contact, chat.getListUtenti().size());
		}
		
		LocalDatabaseHandler.getInstance().updateGroup(groupId, Utilities.getByteArrFromFile(pic));
		LocalDatabaseHandler.getInstance().addMessage(chatMsg);
	}
}
