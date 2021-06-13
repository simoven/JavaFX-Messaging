package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.ChatMainController;
import application.controller.ChatPaneController;
import application.controller.ImageViewController;
import application.logic.ChatLogic;
import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.misc.Protocol;
import application.net.misc.Utilities;
import javafx.event.Event;
import javafx.event.EventHandler;
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

//Questa classe si occupa di gestire la visualizzazione di tutte le chat a sinistra e della singola chat a destra
public class ChatView {

	private static ChatView instance = null;
	private ChatPaneController chatPaneController = null;
	private ChatMainController chatMainController = null;
	private ImageViewController imageViewController = null;
	
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
	
	public void setImageViewController(ImageViewController imageViewController) {
		this.imageViewController = imageViewController;
	}
	
	public ChatMainController getChatMainController() { return chatMainController; }
	
	public ChatPaneController getChatPaneController() { return chatPaneController; }
	
	public void updateInformation() {
		//Questo metodo aggiorna la mia immagine di profilo nel pannello principale
		Image img;
		if(ChatLogic.getInstance().getMyInformation().getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(ChatLogic.getInstance().getMyInformation().getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
		chatMainController.getMyPropicCircle().setFill(new ImagePattern(img));
	}
	
	public void appendMessageInChat(Message msg, boolean isMyMessage) {
		//Questo metodo aggiunge un messaggio alla schermata della chat
		if(!(msg instanceof ChatMessage))
			return;
		
		ChatMessage chatMsg = (ChatMessage) msg;
		
		HBox container = new HBox();
    	container.prefWidthProperty().bind(chatPaneController.getChatVbox().widthProperty());
    	VBox box = new VBox();
    	box.setMaxWidth(chatPaneController.getChatVbox().getPrefWidth() * 0.8);
    	
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
    		img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
    			public void handle(Event event) {
    				imageViewController.handleClick(chatMsg.getImage()); };
			});
    		box.getChildren().add(img);
    		VBox.setMargin(img, new Insets(10, 10, 0, 10));
    	}
  
    	if(chatMsg.getText() != null) {
    		Label field = new Label(chatMsg.getText()); 
    		field.setWrapText(true);
    		field.getStyleClass().add("messageText");
    		box.getChildren().add(field);
    		VBox.setMargin(field, new Insets(5, 10, 5, 10));
    	}
    	
    	Text time = new Text(Utilities.getHourFromStringTrimmed(msg.getTimestamp()));
    	time.getStyleClass().add("messageTime");
    	box.getChildren().add(time);
    	VBox.setMargin(time, new Insets(0, 10, 5, 10));
    	
    	container.getChildren().add(box);
    	chatPaneController.getChatVbox().getChildren().add(container);
    	if(isMyMessage) 
    		VBox.setMargin(container, new Insets(2, 5, 0, 0));
    	else
    		VBox.setMargin(container, new Insets(2, 0, 0, 5));
	}
	
	//TODO fixare il bug che assegna il leftStyle ai messaggi miei
	public void appendGroupMessageInChat(Message msg, boolean isMyMessage, String lastUser) {
		if(!(msg instanceof ChatMessage))
			return;
		
		ChatMessage chatMsg = (ChatMessage) msg;
		
		if(chatMsg.getSender().equals("null")) {
			//Il messaggio è informativo
			Label information = new Label(chatMsg.getText());
			information.getStyleClass().add("genericChatInformation");
			information.setPadding(new Insets(5));
			//TODO
			chatPaneController.getChatVbox().getChildren().add(information);
			VBox.setMargin(information, new Insets(2));
			return;
		}
	
		HBox container = new HBox();
    	container.prefWidthProperty().bind(chatPaneController.getChatVbox().widthProperty());
    	VBox box = new VBox();
    	box.setMaxWidth(chatPaneController.getChatVbox().getPrefWidth() * 0.8);
    	
    	if(isMyMessage) {
    		container.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    		box.getStyleClass().add("rightMessageVBox");
    	}
    	else {
    		container.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    		box.getStyleClass().add("leftMessageVBox");
    		if(lastUser.equals(msg.getSender())) {
    			int actualBox = chatPaneController.getChatVbox().getChildren().size();
    			VBox box2 = (VBox) ((HBox) chatPaneController.getChatVbox().getChildren().get(actualBox - 1)).getChildren().get(0); 
    			box2.getStyleClass().remove(0);
    			box2.getStyleClass().add("leftMessageVBoxFull");
    			//((HBox) chatPaneController.getChatVbox().getChildren().get(actualBox - 1)).setMaxWidth(chatPaneController.getChatVbox().getPrefWidth() * 0.8);
    		}
    	}
    	
    	if(!lastUser.equals(msg.getSender()) && !isMyMessage) {
    		Label name = new Label(msg.getSender());
    		name.getStyleClass().add("groupUserLabel");
    		box.getChildren().add(name);
    		VBox.setMargin(name, new Insets(5, 10, 2, 10));
    	}
    	
    	if(chatMsg.getImage() != null) {
    		ImageView img = new ImageView(new Image(new ByteArrayInputStream(chatMsg.getImage()), 250, 250, true, true));
    		img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
    			public void handle(Event event) {
    				imageViewController.handleClick(chatMsg.getImage()); };
			});
    		box.getChildren().add(img);
    		VBox.setMargin(img, new Insets(5, 10, 5, 10));
    	}
  
    	if(chatMsg.getText() != null) {
    		Label field = new Label(chatMsg.getText()); 
    		field.setWrapText(true);
    		field.getStyleClass().add("messageText");
    		box.getChildren().add(field);
    		VBox.setMargin(field, new Insets(5, 10, 4, 10));
    	}
    	
    	Text time = new Text(Utilities.getHourFromStringTrimmed(msg.getTimestamp()));
    	time.getStyleClass().add("messageTime");
    	box.getChildren().add(time);
    	VBox.setMargin(time, new Insets(0, 10, 5, 10));
    	
    	container.getChildren().add(box);
    	chatPaneController.getChatVbox().getChildren().add(container);
    	
    	int topMargin = 2;
    	if(!lastUser.equals(msg.getSender()) && !isMyMessage)
    		topMargin = 4;
    	
    	if(isMyMessage) 
    		VBox.setMargin(container, new Insets(topMargin, 5, 0, 0));
    	else
    		VBox.setMargin(container, new Insets(topMargin, 0, 0, 5));
	}

	public void showContactInformation(Contact activeContact, int groupMemberNumber) {
		//Questo metodo mostra le informazioni del contatto con cui sto chattando
		Image img;
		if(activeContact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(activeContact.getProfilePic()), 100, 100, true, true);
		else {
			if(activeContact instanceof SingleContact)
				img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
			else 
				img = new Image(getClass().getResource("/application/images/defaultGroup.png").toExternalForm(), 100, 100, true, true);
		}
		
		chatPaneController.getPropicCircle().setFill(new ImagePattern(img));
		chatPaneController.getUsernameLabel().setText(activeContact.getUsername());
		
		if(groupMemberNumber != -1)
			chatPaneController.getLastAccessLabel().setText("Membri : " + groupMemberNumber);
	}

	public void appendChatInMainPanel(Chat chat) {
		//Questo metodo aggiunge una chat nela pannello sulla sinistra con tutte le chat
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
		Label groupId = new Label("-1");
		groupId.setMaxSize(1, 1);
		groupId.setVisible(false);
		
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
			if(groChat.getGroupInfo().getGroupId() == -1)
				return;
			
			if(groChat.getGroupInfo().getProfilePic() != null)
				img = new Image(new ByteArrayInputStream(groChat.getGroupInfo().getProfilePic()), 100, 100, true, true);
			else
				img = new Image(getClass().getResource("/application/images/defaultGroup.png").toExternalForm(), 100, 100, true, true);
			
			chatName = new Label(groChat.getGroupInfo().getUsername());
			lastMessage = new Label(groChat.getLastMessage());
			groupId.setText(Integer.toString(groChat.getGroupInfo().getGroupId()));
			groupId.setMaxSize(1, 1);
			groupId.setVisible(false);
		}
		
		lastMessage.getStyleClass().add("lastMessageInChat");
		textContainer.getChildren().add(chatName);
		textContainer.getChildren().add(lastMessage);
		textContainer.getChildren().add(groupId);
		VBox.setMargin(textContainer.getChildren().get(0), new Insets(10, 10, 5, 0));
		VBox.setMargin(textContainer.getChildren().get(1), new Insets(0, 10, 5, 0));
		
		shape.setFill(new ImagePattern(img));
		String data = "null";
		if(!chat.getListMessaggi().isEmpty()) {
			if(chat.getListMessaggi().lastElement().getSentDate().equals(Utilities.getTodayDate()))
				data = Utilities.getHourFromStringTrimmed(chat.getListMessaggi().lastElement().getTimestamp());
			else
				data = chat.getListMessaggi().lastElement().getSentDate();
		}
		
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

	public void updateOnlineStatus(String status) {
		//Questo metodo aggiorna l'ultimo accesso
		if(status.equals("null"))
			status = "Mai";
		
		if(status.equals(Protocol.USER_ONLINE))
			chatPaneController.getLastAccessLabel().setText("Online");
		else 
			chatPaneController.getLastAccessLabel().setText("Ultimo accesso : " + status);
		
	}
	
}
