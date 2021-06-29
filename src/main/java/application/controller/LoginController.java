package application.controller;

import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.logic.contacts.SingleContact;
import application.net.client.Client;
import application.net.misc.LongUser;
import application.net.misc.Utilities;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private ImageView chatIcon;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label passwordLabel;
    
    @FXML
    void initialize() {
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    	registerButton.getStyleClass().add("loginRegistrationButtons");
    	loginButton.getStyleClass().add("loginRegistrationButtons");
    	chatIcon.setImage(new Image(getClass().getResourceAsStream("/application/images/chatHome.png"), 142, 142, true, true));
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
    		Client.getInstance().resetClient();
    		return;
    	}
    	
    	Client.getInstance().setLogged(true);
    	
    	Utilities.getInstance();
    	
    	SceneHandler.getInstance().setChatScene();
    	SingleContact myContact = new SingleContact(user.getUsername());
    	myContact.setProfilePic(user.getProPic());
    	myContact.setStatus(user.getStatus());
    	ChatLogic.getInstance().setMyInformation(myContact);
    	ChatLogic.getInstance().setFullName(user.getName() + " " + user.getLastName());
    	
    	Client.getInstance().setOnFailed(new ClientFailedController());
    	Client.getInstance().setOnSucceeded(new ClientSucceedController());
    	
    	if(Client.getInstance().getState().equals(State.FAILED))
    		Client.getInstance().reset();
    	
    	Client.getInstance().start();
    }
    
    @FXML
    void setRegisterScene(ActionEvent event) {
    	SceneHandler.getInstance().setRegisterScene();
    }

}
