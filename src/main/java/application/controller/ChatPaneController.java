package application.controller;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.net.client.Client;
import application.net.misc.User;
import application.net.misc.Utilities;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ChatPaneController {

    @FXML
    private Circle propicCircle;

    @FXML
    private ImageView settingsButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label lastAccessLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private ScrollPane chatScrollPane;
    
    @FXML
    private VBox chatVbox;

    @FXML
    private Button sendButton;
    
    @FXML
    private HBox bottomHBox;
    
    @FXML
    void initialize() {
    	Image img = new Image(getClass().getResource("/application/images/3dot_2.png").toExternalForm(), 
    			settingsButton.getFitWidth(), settingsButton.getFitHeight(), true, true);
    	settingsButton.setImage(img);
    	chatVbox.setSpacing(2);
    	chatVbox.heightProperty().addListener(observable -> chatScrollPane.setVvalue(1D));
    	chatVbox.prefWidthProperty().bind(chatScrollPane.widthProperty());
    	bottomHBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	ChatView.getInstance().setChatPaneController(this);
    }
    
    public ScrollPane getChatScrollPane() { return chatScrollPane; }
    
    public VBox getChatVbox() { return chatVbox; }
    
    public Circle getPropicCircle() { return propicCircle; }
    
    public Label getUsernameLabel() { return usernameLabel; }
    
    public Label getLastAccessLabel() { return lastAccessLabel; }
    
    @FXML
    void sendMessage(ActionEvent event) {
    	String text = messageTextArea.getText();
    	if(text == null || text.equals(""))
    		return;
    	
    	ChatMessage msg = null;
    	if(ChatLogic.getInstance().getActiveContact() instanceof SingleContact) {
    		msg = new ChatMessage(ChatLogic.getInstance().getMyUsername(), ChatLogic.getInstance().getActiveContact().getUsername());
    		msg.setGroupMessage(false);
    	}
    	msg.setGroupMessage(false);
    	msg.setText(messageTextArea.getText());
    	msg.setSentDate(Utilities.getDateFromString(Utilities.getCurrentISODate()));
    	msg.setSentHour(Utilities.getHourFromString(Utilities.getCurrentISODate()));
    	messageTextArea.setText("");
    	if(Client.getInstance().sendChatMessage(msg)) 
    		ChatLogic.getInstance().addMessageInChat(msg);
    }

}
