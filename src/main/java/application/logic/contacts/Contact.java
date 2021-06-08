package application.logic.contacts;

public class Contact {
	
	protected String username;
	protected byte [] profilePic;
	
	public Contact(String username) {
		this.username = username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setProfilePic(byte[] profilePic) {
		this.profilePic = profilePic;
	}
	
	public String getUsername() {
		return username;
	}
	
	public byte[] getProfilePic() {
		return profilePic;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if(obj == null)
			return false;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		Contact contact = (Contact) obj;
		
		return username.equals(contact.getUsername());
	}
}
