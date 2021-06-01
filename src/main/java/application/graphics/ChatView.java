package application.graphics;

public class ChatView {

	private static ChatView instance = null;
	
	private ChatView() {
		
	}
	
	public static ChatView getInstance() {
		if(instance == null)
			instance = new ChatView();
		
		return instance;
	}
}
