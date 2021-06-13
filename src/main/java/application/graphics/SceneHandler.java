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
	private BorderPane contactsPane;
	private AnchorPane loginPane;
	private AnchorPane registerPane;
	private AnchorPane imageViewPane;
	private StackPane chatPaneStackPane;
	private AnchorPane createGroupPane;
	private ScrollPane chatPaneScrollPane;
	
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
	
	public ScrollPane getChatPaneScrollPane() {
		return chatPaneScrollPane;
	}
	
	public void setChatPaneStackPane(StackPane chatPaneStackPane) {
		this.chatPaneStackPane = chatPaneStackPane;
	}
	
	public Stage getWindowFrame() {
		return windowFrame;
	}
	
	public AnchorPane getImageViewPane() {
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
		imageViewPane = (AnchorPane) loader.load();
		loader = new FXMLLoader(getClass().getResource("/application/fxml/CreateGroup.fxml"));
		createGroupPane = (AnchorPane) loader.load();
		
		scene = new Scene(loginPane, 800, 600);
		windowFrame.setMinHeight(600);
		windowFrame.setMinWidth(800);
		scene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
		windowFrame.setTitle("JavaFX Messaging");
		windowFrame.setScene(scene);
		windowFrame.setResizable(false);
		windowFrame.show();
	}
	
	public void setLoginScene() {
		scene.setRoot(loginPane);
	}
	
	public void setRegisterScene() {
		scene.setRoot(registerPane);
	}
	
	public void setImageScene() {
		chatPaneStackPane.getChildren().add(imageViewPane);
	}
	
	public void closeImageScene() {
		chatPaneStackPane.getChildren().remove(chatPaneStackPane.getChildren().size() - 1);
	}
	
	//Questo metodo controlla se il pannello per la visualizzazione dell'imagine è rimasto aperto e, in caso, lo chiude
	public void checkImageSceneActive() {
		if(chatPaneStackPane.getChildren().get(chatPaneStackPane.getChildren().size() - 1) instanceof AnchorPane)
			closeImageScene();
	}
	
	public void setChatScene() {
		windowFrame.hide();
		windowFrame.setMinHeight(640);
		windowFrame.setMinWidth(1080);
		windowFrame.setResizable(true);
		scene.setRoot(mainChat);
		windowFrame.show();
	}

	public void showGroupCreationPane() {
		HBox layoutHBox = (HBox) mainChat.getChildren().get(0);
		if(layoutHBox.getChildren().get(1).equals(SceneHandler.getInstance().getCreateGroupPane()))
    		return;
    	
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(SceneHandler.getInstance().getCreateGroupPane());
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
		
	}
}
