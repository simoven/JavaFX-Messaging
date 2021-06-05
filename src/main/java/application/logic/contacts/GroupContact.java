package application.logic.contacts;

public class GroupContact extends Contact {

	private int groupId;
	
	public GroupContact(String groupName, int id) {
		super(groupName);
		this.groupId = id;
	}
	
	public int getGroupId() {
		return groupId;
	}
}
