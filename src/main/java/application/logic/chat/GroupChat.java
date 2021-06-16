package application.logic.chat;

import java.util.Random;
import java.util.Vector;

import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;

public class GroupChat extends Chat {

	private GroupContact groupInfo;
	private Vector <SingleContact> listUtenti;
	private Vector <String> colorList;
	private String userOfLastMessage = "";
	
	public GroupChat(GroupContact group) {
		this.groupInfo = group;
		this.colorList = new Vector<>();
	}
	
	public GroupContact getGroupInfo() {
		return groupInfo;
	}
	
	public void setRandomColors() {
		for(int i = 0; i < listUtenti.size(); ++i) 
			colorList.add(generateRandomColor());
	}
	
	public String getColorOf(String username) {
		for(int i = 0; i < listUtenti.size(); ++i)
			if(listUtenti.get(i).getUsername().equals(username))
				return colorList.get(i);
		
		return "white";
	}
	
	private String generateRandomColor() {
		Random rand = new Random();
		int choice = rand.nextInt(8);
		
		switch (choice) {
			case 0:
				return "#EC0101";
			
			case 1:
				return "#F8FF12";
				
			case 2:
				return "#A09CFF";
				
			case 3:
				return "#5BFF92";
				
			case 4:
				return "#60F4FF";
				
			case 5:
				return "#C998FA";
				
			case 6:
				return "#FF9494";
				
			case 7:
				return "#FFB112";
	
			default:
				break;
		}
		
		return "white";
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
