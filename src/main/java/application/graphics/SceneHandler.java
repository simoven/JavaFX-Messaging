package application.graphics;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
	
	private static SceneHandler instance = null;
	
	private SceneHandler() {}
	
	public static SceneHandler getInstance() {
		if(instance == null)
			instance = new SceneHandler();
		
		return instance;
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
		chatPaneStackPane = (StackPane) chatPane.getCenter();
		chatPane.setCenter(imageViewPane);
	}
	
	public void closeImageScene() {
		chatPane.setCenter(chatPaneStackPane);
	}
	
	public void setChatScene() {
		windowFrame.hide();
		windowFrame.setMinHeight(640);
		windowFrame.setMinWidth(1080);
		windowFrame.setResizable(true);
		scene.setRoot(mainChat);
		windowFrame.show();
	}
}
