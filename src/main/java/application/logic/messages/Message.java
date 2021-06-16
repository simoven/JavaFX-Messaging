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
	
	public Message(String sender, String receiver) {
		this.sender = sender;
		this.receiver = receiver;
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
}
