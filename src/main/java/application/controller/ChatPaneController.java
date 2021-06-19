package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import application.graphics.ChatDialog;
import application.graphics.ChatView;
import application.graphics.ImageViewer;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ChatPaneController {

	@FXML
    private VBox chatVbox;

    @FXML
    private Circle propicCircle;

    @FXML
    private MenuButton settingsButton;

    @FXML
    private HBox chatProfileHBox;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Circle attachImageButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label lastAccessLabel;

    @FXML
    private HBox bottomHBox;
    
    @FXML
    private StackPane chatStackPane;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private Circle sendButton;
    
    private Image blackDot;
    
    @FXML
    void initialize() {
    	blackDot = new Image(getClass().getResource("/application/images/3dot_2.png").toExternalForm(), 30, 30, true, true);
    	settingsButton.setGraphic(new ImageView(blackDot));
    	settingsButton.getItems().get(0).setOnAction(ev -> {
    		if(ChatDialog.getInstance().showConfirmDialog("Stai per eliminare tutti i messaggi. Sei sicuro ?") == ChatDialog.APPROVE_OPTION) {
	    		settingsButton.hide();
	    		ChatLogic.getInstance().clearCurrentChat();
    		}
    	});
    	
    	chatVbox.setAlignment(Pos.CENTER);
    	chatVbox.heightProperty().addListener(observable -> chatScrollPane.setVvalue(1D));
    	chatProfileHBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	chatScrollPane.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	chatVbox.prefWidthProperty().bind(chatScrollPane.prefWidthProperty());
    	bottomHBox.prefHeightProperty().bind(chatProfileHBox.heightProperty().multiply(0.7));
    	
    	sendButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	attachImageButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	
    	sendButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/arrow3.png"), 100, 100, true, true)));
    	attachImageButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/attachIcon.png"), 100, 100, true, true)));
    	
    	SceneHandler.getInstance().setChatPaneStackPane(chatStackPane);
    	SceneHandler.getInstance().setChatPaneScrollPane(chatScrollPane);
    	ChatView.getInstance().setChatPaneController(this);
    }
    
    public ScrollPane getChatScrollPane() { return chatScrollPane; }
    
    public VBox getChatVbox() { return chatVbox; }
    
    public Circle getPropicCircle() { return propicCircle; }
    
    public Label getUsernameLabel() { return usernameLabel; }
    
    public Label getLastAccessLabel() { return lastAccessLabel; }
    
    @FXML
    void sendMessage(MouseEvent event) {
    	String text = messageTextArea.getText();
    	if((text == null || text.isBlank()) && ChatLogic.getInstance().getAttachedImage() == null)
    		return;
    	
    	ChatLogic.getInstance().sendMessage(text);
    	if(ChatLogic.getInstance().getAttachedImage() != null) 
    		removeImage();

    	messageTextArea.setText("");
    }
    
    @FXML
    void onKeyPressed(KeyEvent event) {
    	if(event.getCode().equals(KeyCode.ENTER))
    		sendMessage(null);
    }
    
    @FXML
    void attachImage(MouseEvent event) {
    	if(ChatLogic.getInstance().getAttachedImage() != null) {
    		removeImage();
    		return;
    	}
    	
    	File file = FXUtilities.chooseImage();
    	if(file != null)
    		ImageViewer.getInstance().displayImageChooser(chatStackPane, this, file);
    }
    
    public void attachImage(File file) {
    	try
		{
			Image img2 = new Image(new FileInputStream(file), 100, 100, true, true);
			attachImageButton.setFill(new ImagePattern(img2));
			ChatLogic.getInstance().setAttachedImage(file);;
		} catch (FileNotFoundException e) {
			//TODO show error
		}
    }
    
    public void removeImage() {
    	ChatLogic.getInstance().setAttachedImage(null);;
		attachImageButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/attachIcon.png"), 100, 100, true, true)));
    }
    
    @FXML
    void openContactPane(MouseEvent event) {
    	ChatLogic.getInstance().requestInfoForContactPane();
    	SceneHandler.getInstance().setContactInformationPane();
    }
}
