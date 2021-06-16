package application.controller;

import java.util.ArrayList;
import java.util.Vector;

import application.graphics.CreateChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.net.client.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    
    //Questo flag dice se i bottoni servono per creare un gruppo o aggiungere i partecipanti
    private boolean buttonsForGroupAdd = false;
    
    private int groupIdForAdd = -1;
    
    public void setGroupIdForAdd(int groupIdForAdd) {
		this.groupIdForAdd = groupIdForAdd;
	}
    
    public Button getNewGroupButton() {
		return newGroupButton;
	}
    
    public void setButtonsForGroupAdd(boolean buttonsForGroupAdd) {
		this.buttonsForGroupAdd = buttonsForGroupAdd;
		
		if(buttonsForGroupAdd)
			newGroupButton.setText("Aggiungi");
		else
			newGroupButton.setText("Nuovo gruppo");
	}
    
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
    //Questo metodo cerca gli utenti globali e quelli che sono già miei contatti
    void searchUser(ActionEvent event) {
    	String subUsername = searchField.getText();
    	if(subUsername.isBlank()) {
    		ChatLogic.getInstance().showContactsChoice();
    		return;
    	}
    	
    	ChatLogic.getInstance().showContactsChoiceFiltered(subUsername, buttonsForGroupAdd);
    	
    	if(!buttonsForGroupAdd)
    		Client.getInstance().requestSearch(subUsername);
    }
    
    @FXML
    void createGroup(MouseEvent event) {
    	if(!buttonsForGroupAdd) {
	    	SceneHandler.getInstance().showGroupCreationPane();
	    	ChatLogic.getInstance().showContactForGroupCreation();
    	}
    	else {
    		Vector <String> selectedContacts = new Vector <String>();
    		for(int i = 0; i < allUsersVbox.getChildren().size();) {
    			if(allUsersVbox.getChildren().get(i) instanceof HBox) {
        			HBox container = (HBox) allUsersVbox.getChildren().get(i);
        			CheckBox checkBox = (CheckBox) container.getChildren().get(container.getChildren().size() - 1);
        			if(checkBox.isSelected()) {
        				Label usernameLabel = (Label) ((VBox) container.getChildren().get(1)).getChildren().get(0);
        				selectedContacts.add(usernameLabel.getText());
        			}
        		}
    			
    			i = i + 2;
    		}
    		
    		if(!selectedContacts.isEmpty())
    			ChatLogic.getInstance().addContactsToGroup(selectedContacts, groupIdForAdd);
    		
    		SceneHandler.getInstance().setChatPane();
    	}
    }

}
