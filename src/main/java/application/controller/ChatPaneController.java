package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ImageView settingsButton;

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
    
    private File attachedImage = null;
    
    @FXML
    void initialize() {
    	Image img = new Image(getClass().getResource("/application/images/3dot_2.png").toExternalForm(), 
    			settingsButton.getFitWidth(), settingsButton.getFitHeight(), true, true);
    	settingsButton.setImage(img);
    	chatVbox.setAlignment(Pos.CENTER);
    	chatVbox.heightProperty().addListener(observable -> chatScrollPane.setVvalue(1D));
    	chatProfileHBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	chatScrollPane.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	chatVbox.prefWidthProperty().bind(chatScrollPane.prefWidthProperty());
    	bottomHBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	sendButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	attachImageButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	sendButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/arrow2.png"), 100, 100, true, true)));
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
    	if((text == null || text.equals("")) && attachedImage == null)
    		return;
    	
    	ChatLogic.getInstance().sendMessage(text, attachedImage);
    	if(attachedImage != null) 
    		removeImage();

    	messageTextArea.setText("");
    }
    
    @FXML
    void attachImage(MouseEvent event) {
    	if(attachedImage != null) {
    		removeImage();
    		return;
    	}
    	
    	File file = FXUtilities.chooseImage();
		
		if(file != null) {
			try
			{
				Image img2 = new Image(new FileInputStream(file.getAbsolutePath()), 100, 100, true, true);
				attachImageButton.setFill(new ImagePattern(img2));
				attachedImage = file;
			} catch (FileNotFoundException e) {
				//TODO show error
			}
		}
    }
    
    private void removeImage() {
    	attachedImage = null;
		attachImageButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/attachIcon.png"), 100, 100, true, true)));
    }
}
