package application.logic;

public class ImageMessage extends Message {

	private static final long serialVersionUID = 8991325608219317955L;
	
	private byte [] image;
	
	public ImageMessage(String sender, String receiver) {
		super(sender, receiver);
	}
	
	public byte[] getImage() {
		return image;
	}
	
	public void setImage(byte[] image) {
		this.image = image;
	}
}
