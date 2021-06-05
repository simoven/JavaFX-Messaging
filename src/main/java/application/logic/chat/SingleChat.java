package application.logic.chat;

import application.logic.contacts.Contact;

public class SingleChat extends Chat {

	private Contact chattingWith;
	
	public SingleChat(Contact altroUtente) {
		super();
		this.chattingWith = altroUtente;
	}
	
	public Contact getChattingWith() {
		return chattingWith;
	}
}
