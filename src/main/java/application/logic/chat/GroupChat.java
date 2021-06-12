package application.logic.chat;

import java.util.Vector;

import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;

public class GroupChat extends Chat {

	private GroupContact groupInfo;
	private Vector <SingleContact> listUtenti;
	private String userOfLastMessage = "";
	
	public GroupChat(GroupContact group) {
		this.groupInfo = group;
	}
	
	public GroupContact getGroupInfo() {
		return groupInfo;
	}
	
	public Vector<SingleContact> getListUtenti() {
		return listUtenti;
	}
	
	public Vector <String> getListUtentiusername() {
		Vector <String> vec = new Vector <String>(listUtenti.size());
		for(SingleContact contact : listUtenti)
			vec.add(contact.getUsername());
		
		return vec;
	}
	
	public void setListUtenti(Vector<SingleContact> listUtenti) {
		this.listUtenti = listUtenti;
	}
	
	public String getUserOfLastMessage() {
		return userOfLastMessage;
	}
	
	public void setUserOfLastMessage(String userOfLastMessage) {
		this.userOfLastMessage = userOfLastMessage;
	}
	
	@Override
	public String getLastMessage() {
		if(listMessaggi.isEmpty())
			return "";
		
		if(listMessaggi.lastElement() instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) listMessaggi.lastElement();
			
			if(msg.getImage() != null)
				return msg.getSender() + " : Image..";
			else {
				if(msg.getSender().equals("null"))
					return msg.getText();
				
				return msg.getSender() + " : " + msg.getText();
			}
		}
		
		return "";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(obj == null)
			return false;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		GroupChat chat = (GroupChat) obj;
		
		return groupInfo.equals(chat.getGroupInfo());
	}
	
}
