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
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(obj == null)
			return false;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		SingleChat chat = (SingleChat) obj;
		
		return chattingWith.equals(chat.getChattingWith());
	}
}
