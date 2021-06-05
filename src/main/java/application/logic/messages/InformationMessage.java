package application.logic.messages;

public class InformationMessage extends Message {
	
	private static final long serialVersionUID = 602336402326350309L;
	
	private Object packet;
	private String information;
	
	public InformationMessage() {
		super("SERVER", "");
	}
	
	public void setInformation(String information) {
		this.information = information;
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
