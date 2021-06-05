package application.logic.messages;

import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 8665807362461072828L;
	
	protected String sender;
	protected String receiver;
	protected String sentDate;
	protected String sentHour;
	protected int groupId;
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
	
	public int getGroupId() {
		return groupId;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
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

	public String getSentHourTrimmed() {
		String [] split = sentHour.split(":");
		return split [0] + ":" + split [1];
	}
}
