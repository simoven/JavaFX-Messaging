package application.logic.contacts;

public class SingleContact extends Contact {

	private String status;
	private String name;
	private String lastName;
	
	public SingleContact(String user) {
		super(user);
		name = "";
		lastName = "";
	}
	
	public String getName() {
		return name;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
}
