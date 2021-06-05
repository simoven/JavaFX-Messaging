package application.logic.chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import application.logic.messages.ChatMessage;
import application.logic.messages.Message;

public class Chat implements Comparable <Chat>{

	protected Vector <Message> listMessaggi;
	protected boolean unreadedMessage;
	
	public Chat() {
		listMessaggi = new Vector <Message>();
		unreadedMessage = false;
	}
	
	public void setListMessaggi(Vector<Message> listMessaggi) {
		this.listMessaggi = listMessaggi;
	}
	
	public Vector<Message> getListMessaggi() {
		return listMessaggi;
	}
	
	public boolean getUnreadedMessage() {
		return unreadedMessage;
	}
	
	public void setUnreadedMessage(boolean unreadMessage) {
		this.unreadedMessage = unreadMessage;
	}
	
	public String getLastMessage() {
		if(listMessaggi.lastElement() instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) listMessaggi.lastElement();
			
			if(msg.getImage() != null)
				return "Image..";
			else 
				return msg.getText();
		}
		
		return "";
	}
	
	public void addNewMessage(Message m) {
		listMessaggi.add(m);
		unreadedMessage = true;
	}
	
	public long getLastMessageTimeStamp() {
		if(listMessaggi.isEmpty())
			return 0;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateStr = listMessaggi.lastElement().getSentDate() + " " + listMessaggi.lastElement().getSentHour();
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
	public int compareTo(Chat o) {
		if(getLastMessageTimeStamp() > o.getLastMessageTimeStamp())
			return 1;
		
		if(getLastMessageTimeStamp() < o.getLastMessageTimeStamp())
			return -1;
		
		return 0;
	}
}
