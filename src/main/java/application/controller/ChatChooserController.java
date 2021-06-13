package application.controller;

import application.graphics.CreateChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.net.client.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;

public class ChatChooserController implements EventHandler <MouseEvent> {

    @FXML
    private ScrollPane alluserScrollpane;

    @FXML
    private TextField searchField;

    @FXML
    private VBox allUsersVbox;

    @FXML
    private Button searchUserButton;
    
    @FXML
    private Button newGroupButton;
    
    @FXML 
    private VBox topVbox;
    
    @FXML 
    private HBox topHbox;
    
    @FXML
    void initialize() {
    	CreateChatView.getInstance().setChatChooserController(this);
    	topHbox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	searchField.getStyleClass().add("searchTextField");
    	newGroupButton.getStyleClass().add("newChatButtons");
    }
    
    public ScrollPane getAlluserScrollpane() {
		return alluserScrollpane;
	}
    
    public VBox getAllUsersVbox() {
		return allUsersVbox;
	}
    
    @Override
    public void handle(MouseEvent event) {
    	HBox box = (HBox) event.getSource();
    	//Questa vbox contiene username e stato
    	VBox vBox = (VBox) box.getChildren().get(1);
    	Label username = (Label) vBox.getChildren().get(0);
    	SceneHandler.getInstance().checkImageSceneActive();
    	ChatLogic.getInstance().setSingleActiveChat(username.getText());
    }
    
    @FXML
    void searchUser(ActionEvent event) {
    	//Questo metodo cerca gli utenti globali e quelli che sono già miei contatti
    	String subUsername = searchField.getText();
    	if(subUsername.isBlank()) {
    		ChatLogic.getInstance().showContactsChoice();
    		return;
    	}
    	
    	ChatLogic.getInstance().showContactsChoiceFiltered(subUsername);
    	Client.getInstance().requestSearch(subUsername);
    }
    
    @FXML
    void createGroup(MouseEvent event) {
    	SceneHandler.getInstance().showGroupCreationPane();
    	ChatLogic.getInstance().showContactForGroupCreation();
    }

}
