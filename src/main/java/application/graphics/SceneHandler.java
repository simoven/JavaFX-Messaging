package application.graphics;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneHandler {

	private Stage windowFrame;
	private Scene scene;
	
	private AnchorPane mainChat;
	private BorderPane chatPane;
	private StackPane chatPaneStackPane;
	private ScrollPane chatPaneScrollPane;
	
	private BorderPane contactsPane;
	private AnchorPane loginPane;
	private AnchorPane registerPane;
	private StackPane imageViewPane;
	private AnchorPane createGroupPane;
	private BorderPane contactInformationPane;
	private BorderPane myProfilePane;
	
	private static SceneHandler instance = null;
	
	private SceneHandler() {}
	
	public static SceneHandler getInstance() {
		if(instance == null)
			instance = new SceneHandler();
		
		return instance;
	}
	
	public void setChatPaneScrollPane(ScrollPane chatPaneScrollPane) {
		this.chatPaneScrollPane = chatPaneScrollPane;
	}
	
	public StackPane getChatPaneStackPane() {
		return chatPaneStackPane;
	}
	
	public ScrollPane getChatPaneScrollPane() {
		return chatPaneScrollPane;
	}
	
	public void setChatPaneStackPane(StackPane chatPaneStackPane) {
		this.chatPaneStackPane = chatPaneStackPane;
	}
	
	public Stage getWindowFrame() {
		return windowFrame;
	}
	
	public StackPane getImageViewPane() {
		return imageViewPane;
	}
	
	public BorderPane getChatPane() {
		return chatPane;
	}		
	
	public BorderPane getContactsPane() {
		return contactsPane;
	}
	
	public AnchorPane getCreateGroupPane() {
		return createGroupPane;
	}
	
	public void init(Stage primaryStage) throws Exception {
		windowFrame = primaryStage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/fxml/ChatPane.fxml"));
		chatPane = (BorderPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/ChatMain.fxml"));
		mainChat = (AnchorPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/ChatChooser.fxml"));
		contactsPane = (BorderPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/Login.fxml"));
		loginPane = (AnchorPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/Registration.fxml"));
		registerPane = (AnchorPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/ImageViewer.fxml"));
		imageViewPane = (StackPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/CreateGroup.fxml"));
		createGroupPane = (AnchorPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/ContactInformation.fxml"));
		contactInformationPane = (BorderPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/MyProfile.fxml"));
		myProfilePane = (BorderPane) loader.load();
		
		scene = new Scene(loginPane, 800, 600);
		windowFrame.setMinHeight(600);
		windowFrame.setMinWidth(800);
		scene.getStylesheets().add(getClass().getResource("/application/loginRegistrationStyle.css").toExternalForm());
		windowFrame.setTitle("JavaFX Messaging");
		windowFrame.setScene(scene);
		windowFrame.setResizable(false);
		windowFrame.show();
	}
	
	public void setLoginScene() {
		windowFrame.hide();
		scene.setRoot(loginPane);
		windowFrame.setMinHeight(600);
		windowFrame.setMinWidth(800);
		windowFrame.setHeight(600);
		windowFrame.setWidth(800);
		windowFrame.setResizable(false);
		scene.getStylesheets().clear();
		scene.getStylesheets().add(getClass().getResource("/application/loginRegistrationStyle.css").toExternalForm());
		windowFrame.show();
	}
	
	public void setRegisterScene() {
		scene.setRoot(registerPane);
	}
	
	public void setImageScene() {
		chatPaneStackPane.getChildren().add(imageViewPane);
	}
	
	public void setContactInformationPane() {
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
		layoutHBox.getChildren().remove(1);
		layoutHBox.getChildren().add(contactInformationPane);
	}
	
	public void setChatPane() {	
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(chatPane);
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
    }
	
	public void setAllContactsPane() {
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
		if(layoutHBox.getChildren().get(1).equals(contactsPane))
    		return;
    	
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(contactsPane);
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
	}
	
	public void setMyProfilePane() {
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
		if(layoutHBox.getChildren().get(1).equals(myProfilePane))
    		return;
		
		layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(myProfilePane);
    	HBox.setHgrow(myProfilePane, Priority.ALWAYS);
	}
	
	//Questo metodo controlla se il pannello per la visualizzazione dell'imagine è rimasto aperto e, in caso, lo chiude
	public void checkImageSceneActive() {
		if(chatPaneStackPane.getChildren().indexOf(imageViewPane) != -1)
			chatPaneStackPane.getChildren().remove(imageViewPane);
	}
	
	public void setChatScene() {
		windowFrame.hide();
		windowFrame.setMinHeight(640);
		windowFrame.setMinWidth(1080);
		windowFrame.setResizable(true);
		scene.getStylesheets().clear();
		scene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
		scene.setRoot(mainChat);
		windowFrame.show();
	}

	public void showGroupCreationPane() {
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
		if(layoutHBox.getChildren().get(1).equals(SceneHandler.getInstance().getCreateGroupPane()))
    		return;
    	
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(createGroupPane);
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
		
	}
}
