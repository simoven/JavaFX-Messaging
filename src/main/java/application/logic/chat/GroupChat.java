package application.logic.chat;

import java.util.Vector;

import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;

public class GroupChat extends Chat {

	private GroupContact groupInfo;
	private Vector <SingleContact> listUtenti;
	
	public GroupChat(GroupContact group) {
		this.groupInfo = group;
	}
	
	public GroupContact getGroupInfo() {
		return groupInfo;
	}
	
	public Vector<SingleContact> getListUtenti() {
		return listUtenti;
	}
	
	public void setListUtenti(Vector<SingleContact> listUtenti) {
		this.listUtenti = listUtenti;
	}
	
	@Override
	public String getLastMessage() {
		if(listMessaggi.lastElement() instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) listMessaggi.lastElement();
			
			if(msg.getImage() != null)
				return msg.getSender() + " : Image..";
			else 
				return msg.getSender() + " : " + msg.getText();
		}
		
		return "";
	}
	
}
