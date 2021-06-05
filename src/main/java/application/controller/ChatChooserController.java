package application.controller;

import java.util.zip.CRC32;

import application.graphics.ChatView;
import application.logic.ChatLogic;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

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
    	ChatView.getInstance().setChatChooserController(this);
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
    	VBox vBox = (VBox) box.getChildren().get(1);
    	Label username = (Label) vBox.getChildren().get(0);
    	ChatLogic.getInstance().setSingleActiveChat(username.getText());
    	
    }

}
