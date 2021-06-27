package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import application.graphics.ChatDialog;
import application.graphics.ChatView;
import application.graphics.EmojiLoader;
import application.graphics.ImageViewer;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import application.misc.SoundEffectsHandler;
import application.net.misc.Utilities;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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

	public static final String BACKGROUND_PATH_KEY = "BACKGROUND_PATH_KEY";
	
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
    private Circle showEmojiButton;

    @FXML
    private Circle sendButton;
    
    @FXML
    private MenuItem audioSwitcherButton;
    
    @FXML
    private VBox chatMessageVBox;
    
    private Image blackDot;
    
    private HBox emojiHBox;
    
    private String defaultBackgroundImgPath;
    
    private Preferences prefs;
    
    @FXML
    void initialize() {
    	prefs = Preferences.userRoot().node(this.getClass().getName());
    	
    	blackDot = new Image(getClass().getResource("/application/images/3dot_2.png").toExternalForm(), 30, 30, true, true);
    	settingsButton.setGraphic(new ImageView(blackDot));
    	
    	chatVbox.setAlignment(Pos.CENTER);
    	chatVbox.heightProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if((Double) newValue > (Double) oldValue)
					chatScrollPane.setVvalue(1D);
			}
		});

    	chatVbox.setSpacing(1);
    	chatProfileHBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	defaultBackgroundImgPath = getClass().getResource("/application/images/background.png").toExternalForm();
    	chatScrollPane.setStyle("\n-fx-background-image: url(" + prefs.get(BACKGROUND_PATH_KEY, defaultBackgroundImgPath) + ");");
    	chatVbox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	bottomHBox.prefHeightProperty().bind(chatProfileHBox.heightProperty().multiply(0.7));
    	sendButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	attachImageButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.45));
    	showEmojiButton.radiusProperty().bind(bottomHBox.prefHeightProperty().multiply(0.40));
    	
    	sendButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/arrow3.png"), 100, 100, true, true)));
    	attachImageButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/attachIcon.png"), 100, 100, true, true)));
    	showEmojiButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/emoji.png"), 100, 100, true, true)));
    	
    	SceneHandler.getInstance().setChatPaneStackPane(chatStackPane);
    	SceneHandler.getInstance().setChatPaneScrollPane(chatScrollPane);
    	ChatView.getInstance().setChatPaneController(this);
    	
    	emojiHBox = EmojiLoader.getInstance().getEmojiHBox();
    	emojiHBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	emojiHBox.maxHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.1));
    	
    	checkAudioButtontext();
    }
    
    public ScrollPane getChatScrollPane() { return chatScrollPane; }
    
    public VBox getChatVbox() { return chatVbox; }
    
    public Circle getPropicCircle() { return propicCircle; }
    
    public Label getUsernameLabel() { return usernameLabel; }
    
    public Label getLastAccessLabel() { return lastAccessLabel; }
    
    public TextArea getMessageTextArea() { return messageTextArea; }
    
    public VBox getChatMessageVBox() { return chatMessageVBox; }
    
    @FXML
    void sendMessage(MouseEvent event) {
    	String text = messageTextArea.getText();
    	messageTextArea.setText("");
    	if((text == null || text.isBlank()) && ChatLogic.getInstance().getAttachedImage() == null)
    		return;
    	
    	//Tolgo lo \n lasciato da ENTER
    	if(event == null)
    		text = text.substring(0, text.length() - 1);
    	
    	ChatLogic.getInstance().sendMessage(text);
    	if(ChatLogic.getInstance().getAttachedImage() != null) 
    		removeImage();
    }
    
    @FXML
    void onKeyPressed(KeyEvent event) {
    	if(event.getCode().equals(KeyCode.ENTER)) {
    		sendMessage(null);
    	}
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
    
    public void confirmAttachImage(File file) {
    	try
		{
			Image img2 = new Image(new FileInputStream(file), 100, 100, true, true);
			attachImageButton.setFill(new ImagePattern(img2));
			ChatLogic.getInstance().setAttachedImage(file);;
		} catch (FileNotFoundException e) {
			ChatDialog.getInstance().showResponseDialog("Errore durante il caricamento dell'immagine");
		}
    }
    
    public void removeImage() {
    	ChatLogic.getInstance().setAttachedImage(null);;
		attachImageButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/attachIcon.png"), 100, 100, true, true)));
    }
    
    @FXML
    void openContactPane(MouseEvent event) {
    	ChatLogic.getInstance().requestInfoForContactPane();
    }
    
    @FXML
    void showAllEmojis(MouseEvent event) {
    	if(chatMessageVBox.getChildren().size() > 1) {
    		chatMessageVBox.getChildren().remove(1);
    		return;
    	}
    	
    	ChatView.getInstance().openEmojiBox(emojiHBox);
    }
    
    @FXML
    void changeBackground(ActionEvent event) {
    	settingsButton.hide();
    	int res = ChatDialog.getInstance().showCustomDialog(ChatDialog.PHOTO_DIALOG_WALLPAPER);
    	if(res == ChatDialog.REMOVE_PHOTO_OPTION) {
    		chatScrollPane.setStyle("-fx-background-image: url(" + defaultBackgroundImgPath + ");");
    		prefs.put(BACKGROUND_PATH_KEY, defaultBackgroundImgPath);
    	}
    	else if(res == ChatDialog.NEW_PHOTO_OPTION) {
	    	File choosen = FXUtilities.chooseImage();
	    	if(choosen != null) {
	    		chatScrollPane.setStyle("-fx-background-image: url(file:" + choosen.getAbsolutePath() + ");");
	    		prefs.put(BACKGROUND_PATH_KEY, "file:" + choosen.getAbsolutePath());
	    	}
    	}
    	
    	try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Utilities.getInstance().logToFile(e.getMessage());
		}
    }

    @FXML
    void deleteChat(ActionEvent event) {
    	if(ChatDialog.getInstance().showConfirmDialog("Stai per eliminare tutti i messaggi. Sei sicuro ?") == ChatDialog.APPROVE_OPTION) {
    		settingsButton.hide();
    		ChatLogic.getInstance().clearCurrentChat();
		}
    }
    
    @FXML
    //Ho cliccato su attiva o disattiva audio
    void switchAudio(ActionEvent event) {
    	settingsButton.hide();
    	SoundEffectsHandler.getInstance().setVolume(!SoundEffectsHandler.getInstance().isVolumeOn());
    	checkAudioButtontext();
    }
    
    private void checkAudioButtontext() {
    	if(SoundEffectsHandler.getInstance().isVolumeOn())
    		audioSwitcherButton.setText("Disattiva audio");
    	else
    		audioSwitcherButton.setText("Attiva audio");
    }
}
