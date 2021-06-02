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
	
	public String getSentDate() {
		return sentDate;
	}
	
	public String getSentHour() {
		return sentHour;
	}
	
	public void setGroupMessage(boolean isGroupMessage) {
		this.isGroupMessage = isGroupMessage;
	}
	
	public void setSentDate(String sentDate) {
		this.sentDate = sentDate;
	}
	
	public void setSentHour(String sentHour) {
		this.sentHour = sentHour;
	}
	
	public boolean isAGroupMessage() {
		return isGroupMessage;
	}
}
