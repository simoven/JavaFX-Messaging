package application.controller;

import application.graphics.CreateChatView;
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
    private TextField searchUserField;

    @FXML
    private VBox allUsersVbox;

    @FXML
    private Button searchUserButton;
    
    @FXML
    void initialize() {
    	CreateChatView.getInstance().setChatChooserController(this);
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
    	ChatLogic.getInstance().setSingleActiveChat(username.getText());
    }
    
    @FXML
    void searchUser(ActionEvent event) {
    	//Questo metodo cerca gli utenti globali e quelli che sono già miei contatti
    	String subUsername = searchUserField.getText();
    	if(subUsername.isBlank())
    		return;
    	
    	ChatLogic.getInstance().showContactsChoiceFiltered(subUsername);
    	Client.getInstance().requestSearch(subUsername);
    }

}
