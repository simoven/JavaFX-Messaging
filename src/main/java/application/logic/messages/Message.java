package application.logic.messages;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import application.net.misc.Utilities;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 8665807362461072828L;
	
	protected String sender;
	protected String receiver;
	//Timestamp in formato : YYYY-MM-DDTHH:MM:SS:mmmmmmm
	protected String timestamp;
	protected int groupId;
	protected boolean isGroupMessage;
	protected int messageId;
	protected boolean deleted;
	
	public Message(String sender, String receiver) {
		this.sender = sender;
		this.receiver = receiver;
		messageId = -1;
		groupId = -1;
		deleted = false;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	
	public int getMessageId() {
		return messageId;
	}
	
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getSentDate() {
		return Utilities.getDateFromString(timestamp);
	}
	
	public String getSentHour() {
		return Utilities.getHourFromString(timestamp);
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
	
	public boolean isAGroupMessage() {
		return isGroupMessage;
	}
	
	public long getMessageDateStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = getSentDate();
		Date date;
		
		try {
			date = sdf.parse(dateStr);
			long millis = date.getTime();
			return millis;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(this == obj)
			return true;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		Message msg = (Message) obj;
		
		if(isGroupMessage)
			return sender.equals(msg.getSender()) && groupId == (msg.getGroupId()) && timestamp.equals(msg.getTimestamp());
		else 
			return sender.equals(msg.getSender()) && receiver.equals(msg.getReceiver()) && timestamp.equals(msg.getTimestamp());
	}
}
