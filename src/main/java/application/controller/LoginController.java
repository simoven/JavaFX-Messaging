package application.controller;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.logic.contacts.Contact;
import application.logic.contacts.SingleContact;
import application.net.client.Client;
import application.net.misc.LongUser;
import application.net.misc.User;
import application.net.misc.Utilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label passwordLabel;
    
    @FXML
    void initialize() {
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    }
    
    @FXML
    void tryLogin(ActionEvent event) {
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    	boolean valid = true;
    	
    	String usernameAnswer = Utilities.checkIfUsernameValid(usernameField.getText());
    	if(!usernameAnswer.equals(Utilities.USERNAME_VALID)) {
    		usernameLabel.setText(usernameAnswer);
    		valid = false;
    	}
    	
    	String passwordAnswer = Utilities.checkIfPasswordValid(passwordField.getText());
    	if(!passwordAnswer.equals(Utilities.PASSWORD_VALID)) {
    		passwordLabel.setText(passwordAnswer);
    		valid = false;
    	}
    	
    	if(!valid)
    		return;
    	
    	LongUser user = Client.getInstance().requestLogin(usernameField.getText(), passwordField.getText());
    	
    	if(user == null) {
    	//	SceneHandler.showError("La combinazione username/password è sbagliata");
    		Client.getInstance().resetClient();
    		return;
    	}
    	
    	ChatLogic.getInstance().setMyInformation(user);
    	SceneHandler.getInstance().setChatScene();
    	
    	Client.getInstance().setOnFailed(new ClientFailedController());
    	Client.getInstance().setOnSucceeded(new ClientSucceedController());
    	Client.getInstance().start();
    }
    
    @FXML
    void setRegisterScene(ActionEvent event) {
    	SceneHandler.getInstance().setRegisterScene();
    }

}
