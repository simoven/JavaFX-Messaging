package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.ChatChooserController;
import application.controller.ChatMainController;
import application.controller.ChatPaneController;
import application.logic.ChatLogic;
import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.misc.Utilities;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class ChatView {

	private static ChatView instance = null;
	private ChatPaneController chatPaneController = null;
	private ChatMainController chatMainController = null;
	private ChatChooserController chatChooserController = null;
	
	private ChatView() {}
	
	public static ChatView getInstance() {
		if(instance == null)
			instance = new ChatView();
		
		return instance;
	}
	
	public void setChatPaneController(ChatPaneController chatPaneController) {
		this.chatPaneController = chatPaneController; }
	
	public void setChatMainController(ChatMainController chatMainController) {
		this.chatMainController = chatMainController; }
	
	public void setChatChooserController(ChatChooserController chatChooserController) { 
		this.chatChooserController = chatChooserController; }
	
	public ChatMainController getChatMainController() { return chatMainController; }
	
	public ChatChooserController getChatChooserController() { return chatChooserController; }
	
	public ChatPaneController getChatPaneController() { return chatPaneController; }
	
	public void updateInformation() {
		Image img;
		if(ChatLogic.getInstance().getMyInformation().getProPic() != null)
			img = new Image(new ByteArrayInputStream(ChatLogic.getInstance().getMyInformation().getProPic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
		chatMainController.getMyPropicCircle().setFill(new ImagePattern(img));
	}
	
	public void appendMessageInChat(Message msg, boolean isMyMessage) {
		if(!(msg instanceof ChatMessage))
			return;
		
		ChatMessage chatMsg = (ChatMessage) msg;
		
		HBox container = new HBox();
    	container.prefWidthProperty().bind(chatPaneController.getChatScrollPane().widthProperty());
    	VBox box = new VBox();
    	box.setMaxWidth(chatPaneController.getChatScrollPane().getWidth() * 0.8);
    	
    	if(isMyMessage) {
    		container.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    		box.getStyleClass().add("rightMessageVBox");
    	}
    	else {
    		container.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    		box.getStyleClass().add("leftMessageVBox");
    	}
    	
    	if(chatMsg.getImage() != null) {
    		ImageView img = new ImageView(new Image(new ByteArrayInputStream(chatMsg.getImage()), 250, 250, true, true));
    		box.getChildren().add(img);
    		VBox.setMargin(img, new Insets(5, 10, 5, 10));
    	}
  
    	if(chatMsg.getText() != null) {
    		Label field = new Label(chatMsg.getText()); 
    		field.setWrapText(true);
    		field.getStyleClass().add("messageText");
    		box.getChildren().add(field);
    		VBox.setMargin(field, new Insets(5, 10, 5, 10));
    	}
    	
    	Text time = new Text(msg.getSentHourTrimmed());
    	time.getStyleClass().add("messageTime");
    	box.getChildren().add(time);
    	VBox.setMargin(time, new Insets(0, 10, 5, 10));
    	
    	container.getChildren().add(box);
    	chatPaneController.getChatVbox().getChildren().add(container);
    	//if(isMyMessage)
    		//VBox.setMargin(container, new Insets(20, 5, 0, 20));
    	//else
    		//VBox.setMargin(container, new Insets(5, 0, 5, 0));
	}
	
	public void appendContactInChoiceScreen(SingleContact contact) {
		HBox container = new HBox();
		container.prefWidthProperty().bind(chatChooserController.getAlluserScrollpane().widthProperty());
		Circle shape = new Circle();
		shape.setRadius(30);
		Image img;
		if(contact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(contact.getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
			
		shape.setFill(new ImagePattern(img));
		container.getChildren().add(shape);
		HBox.setMargin(shape, new Insets(10));
		
		VBox textContainer = new VBox();
		Label username = new Label(contact.getUsername());
		textContainer.getChildren().add(username);
		VBox.setMargin(username, new Insets(10, 10, 5, 0));
		if(contact.getStatus() != null) {
			Label status = new Label(contact.getStatus());
			textContainer.getChildren().add(status);
			VBox.setMargin(status, new Insets(0, 10, 5, 0));
		}
		
		container.getChildren().add(textContainer);
		chatChooserController.getAllUsersVbox().getChildren().add(container);
		
		container.addEventHandler(MouseEvent.MOUSE_CLICKED, chatChooserController);
	}

	public void showContactInformation(Contact activeContact) {
		Image img;
		if(activeContact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(activeContact.getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
		
		chatPaneController.getPropicCircle().setFill(new ImagePattern(img));
		chatPaneController.getUsernameLabel().setText(activeContact.getUsername());
		//chatPaneController.getLastAccessLabel().setText(Client.getInstance().getLastAccess(activeContact.getUsername()));
	}

	public void appendChatInMainPanel(Chat chat) {
		HBox container = new HBox();

		if(chat.getUnreadedMessage()) {
			Circle notification = new Circle();
			notification.setFill(Paint.valueOf("#1e90ff"));
			notification.setRadius(5);
			container.getChildren().add(notification);
			HBox.setMargin(notification, new Insets(25, 2, 25, 5));
		}
		
		VBox textContainer = new VBox();
		container.prefWidthProperty().bind(chatMainController.getLeftVbox().widthProperty());
		Circle shape = new Circle();
		shape.setRadius(20);
		Image img;
		Label chatName;
		Label lastMessage;
		
		if(chat instanceof SingleChat) {
			SingleChat sinChat = (SingleChat) chat;
			if(sinChat.getChattingWith().getProfilePic() != null)
				img = new Image(new ByteArrayInputStream(sinChat.getChattingWith().getProfilePic()), 100, 100, true, true);
			else
				img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
			
			chatName = new Label(sinChat.getChattingWith().getUsername());
			lastMessage = new Label(sinChat.getLastMessage());
		}
		//istanza di groupChat
		else {
			GroupChat groChat = (GroupChat) chat;
			if(groChat.getGroupInfo().getProfilePic() != null)
				img = new Image(new ByteArrayInputStream(groChat.getGroupInfo().getProfilePic()), 100, 100, true, true);
			else
				img = new Image(getClass().getResource("/application/images/defaultgroup").toExternalForm(), 100, 100, true, true);
			
			chatName = new Label(groChat.getGroupInfo().getUsername());
			lastMessage = new Label(groChat.getLastMessage());
		}
		
		lastMessage.getStyleClass().add("lastMessageInChat");
		textContainer.getChildren().add(chatName);
		textContainer.getChildren().add(lastMessage);
		VBox.setMargin(textContainer.getChildren().get(0), new Insets(10, 10, 5, 0));
		VBox.setMargin(textContainer.getChildren().get(1), new Insets(0, 10, 5, 0));
		
		shape.setFill(new ImagePattern(img));
		String data;
		if(chat.getListMessaggi().lastElement().getSentDate().equals(Utilities.getTodayDate()))
			data = chat.getListMessaggi().lastElement().getSentHourTrimmed();
		else
			data = chat.getListMessaggi().lastElement().getSentDate();
		
		Text dataUltimoMessaggio = new Text(data);
		Pane spacer = new Pane();
		container.getChildren().add(shape);
		container.getChildren().add(textContainer);
		container.getChildren().add(spacer);
		container.getChildren().add(dataUltimoMessaggio);
		HBox.setMargin(shape, new Insets(10));
		HBox.setMargin(dataUltimoMessaggio, new Insets(20, 10, 10, 10));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		chatMainController.getAllChatVbox().getChildren().add(container);
		container.addEventHandler(MouseEvent.MOUSE_CLICKED, chatMainController);
	}
	
}
