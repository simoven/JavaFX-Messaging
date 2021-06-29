package application.graphics;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneHandler {

	private Stage windowFrame;
	private Scene scene;
	
	private AnchorPane mainChatPane;
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
	
	//é lo stackPnae principale sulla destra, dove poi cambio chikdren in base al pannello che mi serve
	private StackPane mainStackPane;
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
	
	public void setMainStackPane(StackPane mainStackPane) {
		this.mainStackPane = mainStackPane; 
		mainStackPane.prefWidthProperty().bind(windowFrame.widthProperty().multiply(0.8));
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
		mainChatPane = (AnchorPane) loader.load();
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
		scene.getStylesheets().add(getClass().getResource("/application/styles/loginRegistrationStyle.css").toExternalForm());
		windowFrame.setTitle("JavaFX Messaging");
		windowFrame.setScene(scene);
		windowFrame.setResizable(false);
		windowFrame.getIcons().add(new Image(getClass().getResourceAsStream("/application/images/chatHome.png"), 142, 142, true, true));
		windowFrame.show();
		
		ChatDialog.getInstance();
	}
	
	public void setLoginScene() {
		scene.setRoot(loginPane);
	}
	
	public void setOpenLoginScene() {
		if(scene.getRoot().equals(loginPane) || scene.getRoot().equals(registerPane))
			return;
		
		windowFrame.hide();
		scene.setRoot(loginPane);
		windowFrame.setMinHeight(600);
		windowFrame.setMinWidth(800);
		windowFrame.setHeight(600);
		windowFrame.setWidth(800);
		windowFrame.setResizable(false);
		scene.getStylesheets().clear();
		scene.getStylesheets().add(getClass().getResource("/application/styles/loginRegistrationStyle.css").toExternalForm());
		windowFrame.show();
	}
	
	public void setRegisterScene() {
		scene.setRoot(registerPane);
	}
	
	public void setImageScene() {
		chatPaneStackPane.getChildren().add(imageViewPane);
	}
	
	public void setContactInformationPane() {
		if(mainStackPane.getChildren().size() > 0) {
			if(mainStackPane.getChildren().get(0).equals(contactInformationPane))
				return;
			else
				ChatAnimation.doSlideInFromBottom((Pane) mainStackPane.getChildren().get(0), contactInformationPane, mainStackPane);
		}
		else
			ChatAnimation.doSlideInFromBottom(null, contactInformationPane, mainStackPane);
	}
	
	//se non deve scorrere da sinistra, allora deve arrivare da sopra
	public void setChatPane(boolean fromLeft) {	
		if(mainStackPane.getChildren().size() > 0) {
			if(mainStackPane.getChildren().get(0).equals(chatPane))
				return;
			else {
				if(fromLeft)
					ChatAnimation.doSlideInFromLeft((Pane) mainStackPane.getChildren().get(0), chatPane, mainStackPane);
				else 
					ChatAnimation.doSlideInFromTop((Pane) mainStackPane.getChildren().get(0), chatPane, mainStackPane);
			}
		}
		else
			ChatAnimation.doSlideInFromTop(null, chatPane, mainStackPane);
    }
	
	public void setAllContactsPane() {
		if(mainStackPane.getChildren().size() > 0) {
			if(mainStackPane.getChildren().get(0).equals(contactsPane))
				return;
			else 
				ChatAnimation.doSlideInFromBottom((Pane) mainStackPane.getChildren().get(0), contactsPane, mainStackPane);
		}
		else 
			ChatAnimation.doSlideInFromBottom(null, contactsPane, mainStackPane);
	}
	
	public void showGroupCreationPane() {
		if(mainStackPane.getChildren().size() > 0) {
			if(mainStackPane.getChildren().get(0).equals(createGroupPane))
				return;
			else 
				ChatAnimation.doSlideInFromBottom((Pane) mainStackPane.getChildren().get(0), createGroupPane, mainStackPane);
		}
		else
			ChatAnimation.doSlideInFromBottom(null, createGroupPane, mainStackPane);
	}
	
	public void setMyProfilePane() {
		if(mainStackPane.getChildren().size() > 0) {
			if(mainStackPane.getChildren().get(0).equals(myProfilePane))
				return;
			else 
				ChatAnimation.doSlideInFromBottom((Pane) mainStackPane.getChildren().get(0), myProfilePane, mainStackPane);
		}
		else
			ChatAnimation.doSlideInFromBottom(null, myProfilePane, mainStackPane);
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
		scene.getStylesheets().add(getClass().getResource("/application/styles/style.css").toExternalForm());
		scene.setRoot(mainChatPane);
		windowFrame.show();
	}

	public boolean isChatPaneActive() {
		if(mainStackPane.getChildren().size() == 0)
			return false;
		
		return mainStackPane.getChildren().get(mainStackPane.getChildren().size() - 1).equals(chatPane);
	}
}
