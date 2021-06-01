package application.logic;

import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 8665807362461072828L;
	
	protected String sender;
	protected String receiver;
	protected String sentDate;
	protected String sentHour;
	protected boolean isGroupMessage;
	
	public Message(String sender, String receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public boolean isAGroupMessage() {
		return isGroupMessage;
	}
}
