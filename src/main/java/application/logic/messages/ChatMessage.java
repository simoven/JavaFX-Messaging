package application.logic.messages;

public class ChatMessage extends Message {

	private static final long serialVersionUID = -4459877764597328680L;
	
	private String text;
	private byte [] image;
	
	public ChatMessage(String sender, String receiver) {
		super(sender, receiver);
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public byte[] getImage() {
		return image;
	}
	
	public void setImage(byte[] image) {
		this.image = image;
	}
}
