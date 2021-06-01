package application.logic;

public class TextMessage extends Message {

	private static final long serialVersionUID = -4459877764597328680L;
	
	private String text;
	
	public TextMessage(String sender, String receiver) {
		super(sender, receiver);
	}
	
	public String getText() {
		return text;
	}
}
