package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.ChatPaneController;
import application.logic.ChatLogic;
import application.logic.messages.ChatMessage;
import application.logic.messages.InformationMessage;
import application.logic.messages.Message;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SceneHandler {

	private Stage windowFrame;
	private Scene scene;
	private AnchorPane mainChat;
	private BorderPane chatPane;
	private BorderPane contactsPane;
	private AnchorPane loginPane;
	private AnchorPane registerPane;
	
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
		ChatLogic.getInstance();
		
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
	
	public void setChatScene() {
		scene.setRoot(mainChat);
		windowFrame.hide();
		windowFrame.setMinHeight(800);
		windowFrame.setMinWidth(1200);
		windowFrame.setResizable(true);
		windowFrame.show();
	}
	
	public BorderPane getChatPane() {
		return chatPane;
	}		
	
	public BorderPane getContactsPane() {
		return contactsPane;
	}
}
