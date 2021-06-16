package application.logic.messages;

public class InformationMessage extends Message {
	
	private static final long serialVersionUID = 602336402326350309L;
	
	private Object packet;
	private String information;
	private boolean addToContacts;
	
	public InformationMessage() {
		super("SERVER", "");
		addToContacts = false;
	}
	
	public void setInformation(String information) {
		this.information = information;
	}
	
	public void setAddToContacts(boolean addToContacts) {
		this.addToContacts = addToContacts;
	}
	
	public boolean getAddToContacts() {
		return addToContacts;
	}
	
	public void setPacket(Object packet) {
		this.packet = packet;
	}
	
	public String getInformation() {
		return information;
	}
	
	public Object getPacket() {
		return packet;
	}
}
