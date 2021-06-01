package application.logic;

import java.util.Vector;

public class GroupChat extends Chat {

	private int groupId;
	private Vector <String> listUtenti;
	
	public GroupChat(int id) {
		this.groupId = id;
	}
	
	
}
