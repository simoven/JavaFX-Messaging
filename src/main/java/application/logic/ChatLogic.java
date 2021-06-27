package application.logic;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import application.graphics.ChatDialog;
import application.graphics.ChatView;
import application.graphics.ContactInfoView;
import application.graphics.CreateChatView;
import application.graphics.MyProfileView;
import application.graphics.SceneHandler;
import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import application.misc.SoundEffectsHandler;
import application.net.client.Client;
import application.net.client.LocalDatabaseHandler;
import application.net.misc.Protocol;
import application.net.misc.Utilities;

//Questa classe gestisce la parte logica della chat
public class ChatLogic {

	private static ChatLogic instance = null;
	private SingleContact myInformation;
	private String fullName;
	private Chat activeChat;
	private Contact activeContact;
	//è l'ultima immagine che ho selezionato per inviare
	private File attachedImage;
	private Vector <Contact> contactList;
	private Vector <Chat> chatList;
	//Sono gli ultimi contatti usciti da una ricerca globale
	private ArrayList <SingleContact> lastSearchContacts;
	private boolean needsUpdate = false;
	
	private ChatLogic() {
		chatList = new Vector <Chat> ();
		contactList = new Vector <Contact> ();
	}
	
	public static ChatLogic getInstance() {
		if(instance == null)
			instance = new ChatLogic();
		
		return instance;
	}
	
	public void resetLogic() {
		myInformation = null;
		fullName = null;
		contactList.clear();
		chatList.clear();
		lastSearchContacts = null;
		activeChat = null;
		activeContact = null;
		SceneHandler.getInstance().setOpenLoginScene();
	}
	
	public File getAttachedImage() {
		return attachedImage;
	}
	
	public void setAttachedImage(File attachedImage) {
		this.attachedImage = attachedImage;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName; }
	
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
	
	public void displayMyInformation() {
		SceneHandler.getInstance().setMyProfilePane();
		MyProfileView.getInstance().displayMyInformation(myInformation, fullName);
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
		
		//Aggiungo uno spacer così il bottone blu delle chat non si sovrappone al testo della chat
		ChatView.getInstance().appendSpacerInChatpanel();
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
		if(activeContact == null || activeChat == null)
			return;
		
		boolean isGroupChat = false;
		SceneHandler.getInstance().setChatPane(false);
		ChatView.getInstance().getChatPaneController().getChatVbox().getChildren().clear();
		ChatView.getInstance().getChatPaneController().removeImage();
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
		if(username.equals(myInformation.getUsername())) {
			displayMyInformation();
			return;
		}
		
		ChatView.getInstance().getChatPaneController().getLastAccessLabel().setText("");
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
						LocalDatabaseHandler.getInstance().registerUser(contact, false);
						contact.setVisible(false);
					}
					break;
				}
			}
		}
		
		needsUpdate = true;
		//alla fine richiedo l'online status
		if(SceneHandler.getInstance().isChatPaneActive())
			updateStuff();
		
		displayCurrentChat();
	}
	
	//L'animazione è finita, ora posso richiedere le info che mi servono senza bloccare l'animazione
	//Oppure le richiedo manualmente se non c'è stata alcuna animazione
	public void updateStuff() {
		if(needsUpdate) {
			if(activeContact != null && activeContact instanceof SingleContact) {
				Client.getInstance().requestOnlineStatus(activeContact.getUsername());
				Client.getInstance().requestContactInformation(activeContact.getUsername(), false);
			}
			needsUpdate = false;
		}
	}
	
	public void sendMessage(String text) {
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
   
    	if(Client.getInstance().sendChatMessage(msg)) {
    		//Aggiungo il messaggio alla chat
    		if(msg.getMessageDateStamp() > activeChat.getLastMessageDateStamp()) 
				ChatView.getInstance().appendMessageInChat(createInformationMessage(msg.getSentDate()), false, "");
    		
    		activeChat.addMessage(msg);
    		ChatView.getInstance().appendMessageInChat(msg, true, "");
    		if(activeChat instanceof GroupChat)
    			((GroupChat) activeChat).setUserOfLastMessage(msg.getSender());
    		
    		displayAllChat();
    		SoundEffectsHandler.getInstance().playSentMessage();
    	}
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
		msg.setText(text);
		return msg;
	}
	
	//Mostro tutti i miei contatti quando clicco sul bottone per una nuova chat
	public void showContactsChoice() {
		Collections.sort(contactList);
		CreateChatView.getInstance().getChatChooserController().getAllUsersVbox().getChildren().clear();
		CreateChatView.getInstance().changeButtonUse(false);
		for(Contact contact : ChatLogic.getInstance().getContactList()) {
			if(!contact.isVisible())
				continue;
			
			if(contact instanceof SingleContact && !contact.getUsername().equals(myInformation.getUsername())) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, false, false);
		}
		
		SceneHandler.getInstance().setAllContactsPane();
	}
	
	//questo metodo mostra tutti i miei contatti prima della creazione di un gruppo
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
		Client.getInstance().requestContactInformation(sender, true);
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
			
			SoundEffectsHandler.getInstance().playIncomingMessageActiveChat();
		}
		else
			SoundEffectsHandler.getInstance().playIncomingMessage();
		
		if(chat instanceof GroupChat)
			((GroupChat) chat).setUserOfLastMessage(msg.getSender());		
		
		displayAllChat();
	}

	//Controllo se nei messaggi che mi sono arrivati ci sono messaggi dal server, tipo "utente x è stato rimosso dal gruppo"
	//La struttura del messaggio è OPERAZIONE:username
	//Cambio il testo del messaggio che, in seguito, verrà salvato nel db locale dopo aver finito
	private void checkGroupMsgText(ChatMessage msg) {
		GroupContact contact = searchContact(msg.getGroupId());
		if(contact == null)
			return;
		
		GroupChat chat = (GroupChat) searchChat(contact);
		boolean createdNow = false;
		if(chat == null) {
			chat = (GroupChat) createChat(contact);
			createdNow = true;
		}
		
		if(msg.getText().contains(":")) {
			String [] split = msg.getText().split(":");
			if(split [0].equals("REMOVED")) {
				//Qualcuno è stato rimosso da un gruppo
				if(split [1].equals(ChatLogic.getInstance().getMyInformation().getUsername())) {
					msg.setText("Sei stato rimosso");
					removeFromGroup(chat, myInformation.getUsername(), msg);
				}
				else {
					msg.setText(split [1] + " è stato rimosso");
					removeFromGroup(chat, split [1], msg);
				}
			}
			else if(split [0].equals("ADDED")) {
				//Qualcuno è stato aggiunto ad un gruppo
				if(split [1].equals(ChatLogic.getInstance().getMyInformation().getUsername())) {
					msg.setText("Sei stato aggiunto");
					addedToGroup(myInformation.getUsername(), chat, msg);
				}
				else {
					msg.setText(split [1] + " è stato aggiunto");
					addedToGroup(split [1], chat, msg);
				}
			}
			else if(split [0].equals("LEFT")) {
				//qualcuno ha abbandonato un gruppo
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
				//qualche gruppo è stato eliminato
				int groupId = Integer.parseInt(split [1]);
				msg.setText("Il gruppo è stato eliminato");
				//Lo rimuovo subito perché non esiste piu
				if(createdNow)
					chatList.remove(chat);
				
				//Significa che il gruppo nemmeno esisteva nelle mie chat (Ero offline tra la creazione e l'eliminazione)
				//E quindi scarto questo messaggio, altrimenti chatlogic avrebbe richiesto al server info sul gruppo (che non esiste piu)
				if(!handleGroupDeletion(groupId))
					msg.setGroupId(-2);
			}
			else if(split [0].equals("PIC_CHANGED")) {
				//La foto profilo di qualche gruppo è stata cambiata
				int groupId = Integer.parseInt(split [1]);
				msg.setText("L' immagine di profilo è stata cambiata");
				//Richiedo info al server su questo gruppo e quindi riceverò l'immagine aggiornata
				Client.getInstance().requestGroupInformation(groupId);
			}
			else if(split [0].equals("NAME_CHANGED")) {
				int groupId = msg.getGroupId();
				GroupContact gpContact = searchContact(groupId);
				msg.setText("Il nome del gruppo è stato cambiato");
				gpContact.setUsername(split [1]);
				LocalDatabaseHandler.getInstance().updateGroup(groupId, split [1]);
			}
		}
	}
	
	//recupero i messaggi che mi sono arrivati quando ero offline
	public void retrievePendingMessage(ArrayList <Message> listMessaggi) {
		for(Message msg : listMessaggi) {
			Chat chat;
			Contact msgContact;
			
			//Questo messaggio contiene i dati di un messaggio che deve essere eliminato
			if(msg.isDeleted()) {
				removeMsg((ChatMessage) msg);
				continue;
			}
			
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
			LocalDatabaseHandler.getInstance().createGroup(gpContact);
			LocalDatabaseHandler.getInstance().addPartecipantsToGroup(groupID, ((GroupChat) searchChat(gpContact)).getListUtentiusername());
			
			activeChat = (GroupChat) searchChat(gpContact);
			activeContact = gpContact;
			sendInformationMessage(groupID, "Il gruppo è stato creato");
			
			displayAllChat();
			displayCurrentChat();
		}
	}

	//Questo metodo imposta la chat di gruppo come chat attiva
	public void setGroupChatActive(int groupId) {
		GroupContact gpContact = searchContact(groupId);		
		GroupChat gpChat = (GroupChat) searchChat(gpContact);
		activeContact = gpContact;
		activeChat = gpChat;
		ChatView.getInstance().getChatPaneController().getLastAccessLabel().setText("");
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
		
		if(LocalDatabaseHandler.getInstance().groupExists(groupId))
			LocalDatabaseHandler.getInstance().updateGroup(groupId, username, proPic);
		else
			LocalDatabaseHandler.getInstance().createGroup(gpContact);
	}

	//Questo metodo aggiorna l'utente e viene chiamato quando riceviamo un messaggio da lui per la prima volta
	//Viene inoltre aggiunto ai nostri contatti
	//Oppure viene chiamato quando clicchiamo sulla sua chat e aggiorniamo le sue info
	public void registerUser(String username, String name, String lastName, String status, byte[] proPic) {
		SingleContact user = searchContact(username);
		user.setStatus(status);
		user.setProfilePic(proPic);
		user.setName(name);
		user.setLastName(lastName);
		user.setVisible(false);
		LocalDatabaseHandler.getInstance().registerUser(user, false);

		displayAllChat();
	}
	
	//Questo metodo viene chiamato quando richiedo le info aggiornate su un contatto, tipo immagine profilo o stato
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
		
		LocalDatabaseHandler.getInstance().addPartecipantsToGroup(groupID, chat.getListUtentiusername());
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
	
	//Richiede informazioni per mostrare il contatto sulla schermata di info
	public void requestInfoForContactPane() {
		if(activeChat == null) 
			return;
		
		if(activeContact instanceof SingleContact) 
			showUserInfo((SingleContact) activeContact);
		else {
			boolean iAmRemoved = false;
			if(!((GroupChat) activeChat).getListUtenti().contains(myInformation))
				iAmRemoved = true;
			
			boolean iAmOwner = true;
			if(!((GroupContact) activeContact).getOwner().equals(myInformation.getUsername()))
				iAmOwner = false;
			
			ContactInfoView.getInstance().showGroupInfo((GroupChat) activeChat, iAmOwner, iAmRemoved);
			SceneHandler.getInstance().setContactInformationPane();
		}		
	}

	public void showUserInfo(SingleContact contact) {
		if(contact == null)
			return;
			
		ContactInfoView.getInstance().showInfo(contact);
		SceneHandler.getInstance().setContactInformationPane();
	}

	//questo metodo cambia la visibilità di un contatto, in modo da vederlo o no tra i miei contatti
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

	//Questo metodo gestisce la rimozione dal gruppo vera e propria
	public boolean removeFromGroup(GroupChat chat, String username, ChatMessage msg) {
		SingleContact contact = searchContact(username);
		if(contact == null)
			return false;
		
		if(chat.getListUtenti() == null)
			return false;
		
		int idx = chat.getListUtenti().indexOf(contact);
		if(idx != -1)
			chat.getListUtenti().remove(idx);
		
		LocalDatabaseHandler.getInstance().addMessage(msg);
		LocalDatabaseHandler.getInstance().removeFromGroup(username, chat.getGroupInfo().getGroupId());
		
		if(username.equals(myInformation.getUsername())) {
			chat.getGroupInfo().setDeleted(true);
			LocalDatabaseHandler.getInstance().setGroupDeletion(chat.getGroupInfo().getGroupId(), true);
		}
		
		return true;
	}
	
	//Questo metodo controlla se il gruppo è attualmente il contatto attivo e appende il messaggio informativo nella chat
	private void appendLastinfoMessageInChat(GroupChat chat, ChatMessage chatMsg) {
		if(chat.equals(activeChat)) {
			if(chatMsg.getMessageDateStamp() > chat.getLastMessageDateStamp()) 
				ChatView.getInstance().appendMessageInChat(createInformationMessage(chatMsg.getSentDate()), false, "");
			
			ChatView.getInstance().appendMessageInChat(chatMsg, false, "null");
			ChatView.getInstance().showContactInformation(activeContact, chat.getListUtenti().size());
		}
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
			appendLastinfoMessageInChat(chat, msg);
			
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
			if(contact instanceof SingleContact && chat.getListUtenti().indexOf(contact) == -1 && contact.isVisible())
				contactsToAdd.add((SingleContact) contact);
		
		CreateChatView.getInstance().setUserNotInGroup(contactsToAdd);
		
		CreateChatView.getInstance().clearContactVBox();
		for(SingleContact contact : contactsToAdd)
			CreateChatView.getInstance().appendContactInChoiceScreen(contact, false, true, true);
	}

	public void addContactsToGroup(Vector <String> selectedContacts, int groupIdForAdd) {
		Client.getInstance().requestGroupAdd(myInformation.getUsername(), selectedContacts, groupIdForAdd);
		SceneHandler.getInstance().setChatPane(true);
	}
	
	//Questo metodo gestisce l'aggiunta vera e propria nella chat
	private boolean addedToGroup(String user, GroupChat chat, ChatMessage msg) {
		SingleContact contactToAdd = searchContact(user);
		if(contactToAdd == null) {
			contactToAdd = createContact(user);
			LocalDatabaseHandler.getInstance().registerUser(contactToAdd, false);
		}
		
		if(chat == null)
			return false;
		
		chat.getListUtenti().add(contactToAdd);
		
		if(user.equals(myInformation.getUsername())) {
			chat.getGroupInfo().setDeleted(false);
			LocalDatabaseHandler.getInstance().setGroupDeletion(chat.getGroupInfo().getGroupId(), false);
			Client.getInstance().requestGroupInformation(chat.getGroupInfo().getGroupId());
			Client.getInstance().requestGroupMembers(chat.getGroupInfo().getGroupId());
		}
		
		LocalDatabaseHandler.getInstance().addMessage(msg);
		LocalDatabaseHandler.getInstance().addPartecipantToGroup(chat.getGroupInfo().getGroupId(), user);
		
		return true;
	}

	//Questo metodo gestisce l'aggiunta di un membro in un gruppo, che sia io o un'altra persona
	//Crea il messaggio informativo e lo appende nella chat
	public void handleGroupAdd(String userAdded, Integer groupId) {
		CreateChatView.getInstance().changeButtonUse(false);
		GroupContact contact = searchContact(groupId);
		if(contact == null)
			contact = createGroupContact(groupId);
		
		GroupChat chat = (GroupChat) searchChat(contact);
		if(chat == null)
			chat = (GroupChat) createChat(contact);
		
		ChatMessage msg;
		if(!userAdded.equals(myInformation.getUsername()))
			msg = createInformationMessage(userAdded + " è stato aggiunto");
		else
			msg = createInformationMessage("Sei stato aggiunto");
		
		msg.setGroupId(groupId);
		msg.setGroupMessage(true);
		
		if(addedToGroup(userAdded, chat, msg)) {
			appendLastinfoMessageInChat(chat, msg);
			
			chat.addNewMessage(msg);
			displayAllChat();
		}
	}
	
	//Questo metodo trova i gruppi in comune tra me e un altro utente
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

	//Questo metodo gestisce l'eliminazione di un gruppo
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
		contact.setDeleted(true);
		LocalDatabaseHandler.getInstance().setGroupDeletion(chat.getGroupInfo().getGroupId(), true);
		
		displayAllChat();
		
		appendLastinfoMessageInChat(chat, msg);
		
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
	
	//Questo metodo aggiorna l'immagine di profilo del gruppo dopo la risposta del server
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
		
		appendLastinfoMessageInChat(chat, chatMsg);
		
		LocalDatabaseHandler.getInstance().updateGroup(groupId, contact.getUsername(), Utilities.getByteArrFromFile(pic));
		LocalDatabaseHandler.getInstance().addMessage(chatMsg);
	}

	public void groupNameChanged(String name) {
		if(activeChat instanceof GroupChat) {
			if(((GroupChat) activeChat).getGroupInfo().getUsername().equals(name))
				return;
			
			if(((GroupChat) activeChat).getListUtenti().contains(myInformation))
				Client.getInstance().updateGroupName(myInformation.getUsername(), name, ((GroupChat) activeChat).getGroupInfo().getGroupId());
		}
	}

	public void updateGroupName(Integer groupId, String newName) {
		GroupContact contact = searchContact(groupId);
		if(contact == null)
			return;
		
		GroupChat chat = (GroupChat) searchChat(contact);
		if(chat == null)
			return;
		
		ChatMessage chatMsg = createInformationMessage("Il nome del gruppo è stato cambiato");
		chatMsg.setGroupId(groupId);
		chatMsg.setGroupMessage(true);
		contact.setUsername(newName);
		
		chat.addNewMessage(chatMsg);
		displayAllChat();
		
		appendLastinfoMessageInChat(chat, chatMsg);
		
		LocalDatabaseHandler.getInstance().updateGroup(groupId, newName);
		LocalDatabaseHandler.getInstance().addMessage(chatMsg);
		
	}

	public void changePassword(String oldPassword, String newPassword) {
		Client.getInstance().requestPasswordChange(myInformation.getUsername(), oldPassword, newPassword);
	}

	public void handlePasswordChange(InformationMessage msg) {
		String response = (String) msg.getPacket();
		if(!response.equals(Protocol.REQUEST_SUCCESSFUL)) {
			String error = checkPasswordErrorText(response);
			int res = ChatDialog.getInstance().showErrorDialog(error);
			if(res == ChatDialog.RETRY_OPTION)
				displayMyInformation();
		}
		else
			ChatDialog.getInstance().showResponseDialog("La password è stata cambiata con successo");
		
	}

	public String checkPasswordErrorText(String response) {
		switch(response) {
		    case Utilities.PASSWORD_TOO_SHORT:
		    	return "La nuova password deve essere lunga almeno 8 caratteri";
		    	
		    case Utilities.PASSWORD_TOO_LONG:
		    	return "La nuova password deve essere lunga al massimo 20 caratteri";
		    	
		    case Utilities.PASSWORD_NOT_VALID:
		    	return "I caratteri della nuova password non sono validi";
		    
		    case Protocol.INVALID_CREDENTIAL:
		    	return "La vecchia password non è corretta";
		    	
		    case Protocol.SERVER_ERROR:
		    	return "C'è stato un errore nel server";
		}
		
		return "Impossibile cambiare la password";
	}

	public void updateMyPhoto(File photo) {
		Client.getInstance().updateProPic(myInformation.getUsername(), photo);
	}

	public void updateMyStatus(String text) {
		Client.getInstance().updateStatus(myInformation.getUsername(), text);
	}

	//Questo metodo aggiorna la mia immagine di profilo
	public void handleMyPhotoUpdate(File packet) {
		byte [] img = Utilities.getByteArrFromFile(packet);
		myInformation.setProfilePic(img);
		if(LocalDatabaseHandler.getInstance().modifyUser(myInformation)) {
			MyProfileView.getInstance().displayMyInformation(myInformation, fullName);
			ChatDialog.getInstance().showResponseDialog("Foto aggiornata con successo");
			ChatView.getInstance().updateInformation();
		}
	}

	public void handleMyStatusChanged(String status) {
		myInformation.setStatus(status);
		if(LocalDatabaseHandler.getInstance().modifyUser(myInformation)) {
			MyProfileView.getInstance().displayMyInformation(myInformation, status);
			ChatDialog.getInstance().showResponseDialog("Stato aggiornato con successo");
		}
	}

	public void removeMsg(ChatMessage chatMessage) {
		Contact contact;
		Chat chat;
		try {
			if(chatMessage.isAGroupMessage()) 
				contact = searchContact(chatMessage.getGroupId());
	
			else {
				String user = chatMessage.getSender();
				if(user.equals(myInformation.getUsername()))
					user = chatMessage.getReceiver();
			
				contact = searchContact(user);
			}
			
			chat = searchChat(contact);
		} catch (NullPointerException e) {
			e.printStackTrace();
			return;
		}
		
		int idx = chat.getListMessaggi().indexOf(chatMessage);
		if(idx != -1) {
			chat.getListMessaggi().remove(idx);
			LocalDatabaseHandler.getInstance().removeMessage(chatMessage);
		}
		
		//Se ho eliminato l'ultimo messaggio, devo aggiornare il pannello a sinistra
		if(idx == chat.getListMessaggi().size())
			displayAllChat();
		
		if(chat.equals(activeChat))
			displayCurrentChat();
	}

	public void removeMessageForAll(ChatMessage chatMsg) {
		Client.getInstance().removeMessage(chatMsg);
	}
}
