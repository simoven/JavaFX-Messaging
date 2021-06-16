package application.logic.contacts;

public class GroupContact extends Contact {

	private int groupId;
	private String owner;
	private String creationDate;
	
	public GroupContact(String groupName, int id) {
		super(groupName);
		this.groupId = id;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getCreationDate() {
		return creationDate;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if(obj == null)
			return false;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		GroupContact contact = (GroupContact) obj;
		
		return groupId == contact.getGroupId();
	}
}
