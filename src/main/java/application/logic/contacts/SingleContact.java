package application.logic.contacts;

public class SingleContact extends Contact {

	private String status;
	
	public SingleContact(String user) {
		super(user);
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
}
