package application.graphics;

import java.io.ByteArrayInputStream;

import com.pavlobu.emojitextflow.EmojiTextFlow;

import application.MainApplication;
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
import application.net.misc.Protocol;
import application.net.misc.Utilities;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
import javafx.scene.text.TextFlow;

//Questa classe si occupa di gestire la visualizzazione di tutte le chat a sinistra e della singola chat a destra
public class ChatView {

	private static ChatView instance = null;
	private ChatPaneController chatPaneController = null;
	private ChatMainController chatMainController = null;
	
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
	
	public ChatMainController getChatMainController() { return chatMainController; }
	
	public ChatPaneController getChatPaneController() { return chatPaneController; }
	
	public void updateInformation() {
		//Questo metodo aggiorna la mia immagine di profilo nel pannello principale
		Image img;
		if(ChatLogic.getInstance().getMyInformation().getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(ChatLogic.getInstance().getMyInformation().getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.png").toExternalForm(), 100, 100, true, true);
		chatMainController.getMyPropicCircle().setFill(new ImagePattern(img));
	}
	
	//Questo metodo aggiunge un messaggio alla schermata della chat
	public void appendMessageInChat(Message msg, boolean isMyMessage, String lastUser) {
		if(!(msg instanceof ChatMessage))
			return;
		
		ChatMessage chatMsg = (ChatMessage) msg;
		
		if(chatMsg.getSender().equals("null")) {
			//Il messaggio è informativo
			Label information = new Label(chatMsg.getText());
			information.getStyleClass().add("genericChatInformation");
			information.setPadding(new Insets(5, 8, 5, 8));
			chatPaneController.getChatVbox().getChildren().add(information);
			VBox.setMargin(information, new Insets(2));
			return;
		}
		
		HBox container = new HBox();
    	container.prefWidthProperty().bind(chatPaneController.getChatVbox().widthProperty());
    	VBox box = new VBox();
    	box.setMaxWidth(chatPaneController.getChatVbox().getPrefWidth() * 0.8);
    	boolean labelFound = false;
    	
    	if(isMyMessage) {
    		//Lo faccio per spostare il messaggio a destra
    		Pane spacer = new Pane();
    		spacer.setPrefHeight(1);
    		container.getChildren().add(spacer);
    		HBox.setHgrow(spacer, Priority.ALWAYS);
    		box.getStyleClass().add("rightMessageVBox");
    	}
    	else {
    		box.getStyleClass().add("leftMessageVBox");
    		
    		//Se il messaggio è di gruppo e l'ultima persona che ha inviato il messaggio è uguale al sender di ora, cambio lo stile del precedente messaggio
			if(msg.isAGroupMessage() && lastUser.equals(msg.getSender())) {
    			int actualBox = chatPaneController.getChatVbox().getChildren().size();
    			//Se c'è una label, la salto
    			if (chatPaneController.getChatVbox().getChildren().get(actualBox - 1) instanceof Label)
    				labelFound = true;
    			
    			if(!labelFound) {
	    			VBox box2 = (VBox) ((HBox) chatPaneController.getChatVbox().getChildren().get(actualBox - 1)).getChildren().get(0);
	    			box2.getStyleClass().remove(0);
	    			box2.getStyleClass().add("leftMessageVBoxFull");
    			}
       		}
    	}
    	
    	//Se la persona che ha inviato l'ultimo messaggio di gruppo è diversa dal sender ora, allora aggiungo l'username di chi ha inviato il messaggio
		if((msg.isAGroupMessage() && !lastUser.equals(msg.getSender()) && !isMyMessage) || labelFound) {
    		Label name = new Label(msg.getSender());
    		name.getStyleClass().add("groupUserLabel");
    		name.setStyle("-fx-text-fill:" + ((GroupChat) ChatLogic.getInstance().getActiveChat()).getColorOf(msg.getSender()) + ";");
    		box.getChildren().add(name);
    		VBox.setMargin(name, new Insets(5, 10, 0, 10));
    	}
   
    	
    	if(chatMsg.getImage() != null) {
    		ImageView img = new ImageView(new Image(new ByteArrayInputStream(chatMsg.getImage()), 250, 250, true, true));
    		img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
    			public void handle(Event event) {
    				ImageViewer.getInstance().displayImageInPane(SceneHandler.getInstance().getChatPaneStackPane(), chatMsg.getImage()); };
			});
    		box.getChildren().add(img);
    		VBox.setMargin(img, new Insets(10, 10, 0, 10));
    	}
  
    	if(chatMsg.getText() != null) {
    		EmojiTextFlow textFlow = new EmojiTextFlow(MainApplication.emojiTextFlowParameters);
    		textFlow.setPrefHeight(TextFlow.USE_COMPUTED_SIZE);
        	textFlow.parseAndAppend(chatMsg.getText());
        	box.getChildren().add(textFlow);
    		VBox.setMargin(textFlow, new Insets(8, 10, 6, 10));
    	}
    	
    	Text time = new Text(Utilities.getHourFromStringTrimmed(msg.getTimestamp()));
    	time.getStyleClass().add("messageTime");
    	if(isMyMessage) {
    		HBox textContainer = new HBox();
    		Pane spacer = new Pane();
    		spacer.setPrefHeight(1);
    		textContainer.getChildren().add(spacer);
    		HBox.setHgrow(spacer, Priority.ALWAYS);
    		textContainer.getChildren().add(time);
    		box.getChildren().add(textContainer);
    		HBox.setMargin(time, new Insets(0, 10, 5, 10));
    	}
    	else {
    		box.getChildren().add(time);
    		VBox.setMargin(time, new Insets(0, 10, 5, 10));
    	}
    	
    	container.getChildren().add(box);
    	chatPaneController.getChatVbox().getChildren().add(container);
    	
    	int topMargin = 0;
    	//se sono in un gruppo, l'utente che ha inviato questo messaggio è diverso dall'ultimo utente e questo messaggio non è il mio
    	if(msg.isAGroupMessage() && !lastUser.equals(msg.getSender()) && !isMyMessage)
    		topMargin = 2;
    	
    	if(isMyMessage) 
    		VBox.setMargin(container, new Insets(topMargin, 5, 2, 0));
    	else
    		VBox.setMargin(container, new Insets(topMargin, 0, 2, 5));
    	
    	//Questo è il menu che esce con il doppio click sul messaggio
    	ContextMenu menu = new ContextMenu();
    	MenuItem remove = new MenuItem("Elimina messaggio");
    	menu.getItems().add(remove);
    	
    	remove.setOnAction(ev -> {
    		menu.hide();
    		int res;
    		if(!isMyMessage)
    			res = ChatDialog.getInstance().showConfirmDialog("Stai per eliminare il messaggio");
    		else
    			res = ChatDialog.getInstance().showCustomDialog(ChatDialog.CONFIRM_DIALOG_DELETE_MESSAGE);
    			
    		if(res == ChatDialog.REMOVE_FOR_ME_OPTION || res == ChatDialog.APPROVE_OPTION)
    			ChatLogic.getInstance().removeMsg(chatMsg);
    		else if(res == ChatDialog.REMOVE_FOR_ALL_OPTION)
    			ChatLogic.getInstance().removeMessageForAll(chatMsg);
    	});
    	
    	container.setOnContextMenuRequested(ev -> {
    		menu.show(container.getScene().getWindow(), ev.getScreenX(), ev.getScreenY());
    	});
	}

	public void showContactInformation(Contact activeContact, int groupMemberNumber) {
		//Questo metodo mostra le informazioni del contatto con cui sto chattando
		Image img;
		if(activeContact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(activeContact.getProfilePic()), 100, 100, true, true);
		else {
			if(activeContact instanceof SingleContact)
				img = new Image(getClass().getResource("/application/images/defaultSinglePic.png").toExternalForm(), 100, 100, true, true);
			else 
				img = new Image(getClass().getResource("/application/images/defaultGroup.png").toExternalForm(), 100, 100, true, true);
		}
		
		chatPaneController.getPropicCircle().setFill(new ImagePattern(img));
		chatPaneController.getUsernameLabel().setText(activeContact.getUsername());
		
		if(groupMemberNumber != -1)
			chatPaneController.getLastAccessLabel().setText("Membri : " + groupMemberNumber);
	}

	//Questo metodo aggiunge una chat nela pannello sulla sinistra con tutte le chat
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
		Label groupId = new Label("-1");
		groupId.setMaxSize(1, 1);
		groupId.setVisible(false);
		
		if(chat instanceof SingleChat) {
			SingleChat sinChat = (SingleChat) chat;
			if(sinChat.getChattingWith().getProfilePic() != null)
				img = new Image(new ByteArrayInputStream(sinChat.getChattingWith().getProfilePic()), 100, 100, true, true);
			else
				img = new Image(getClass().getResource("/application/images/defaultSinglePic.png").toExternalForm(), 100, 100, true, true);
			
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
		String data = "Mai";
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
	
	public void appendSpacerInChatpanel() {
		Pane spacer = new Pane();
		spacer.setPrefHeight(50);
		spacer.setMinHeight(50);
		spacer.setMaxHeight(50);
		spacer.setPrefWidth(1);
		chatMainController.getAllChatVbox().getChildren().add(spacer);
	}	

	public void updateOnlineStatus(String status) {
		//Questo metodo aggiorna l'ultimo accesso
		if(status.equals("null"))
			status = "Mai";
		
		if(status.equals(Protocol.USER_ONLINE))
			chatPaneController.getLastAccessLabel().setText("Online");
		else {
			String day = status.split(" ") [0];
			String hour = status.split(" ") [1];
			if(day.equals(Utilities.getDateFromString(Utilities.getCurrentISODate())))
				chatPaneController.getLastAccessLabel().setText("Ultimo accesso oggi alle " + hour);
			else
				chatPaneController.getLastAccessLabel().setText("Ultimo accesso il " + day + " alle " + hour);
		}
		
	}

	public void openEmojiBox(HBox emojiHBox) {
		chatPaneController.getChatMessageVBox().getChildren().add(emojiHBox);
    	VBox.setMargin(emojiHBox, new Insets(6, 4, 4, 4));	
	}
}
