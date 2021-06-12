package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import application.graphics.SceneHandler;
import application.misc.FXUtilities;
import application.net.client.Client;
import application.net.misc.LongUser;
import application.net.misc.Utilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordConfirmField;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private Circle picChooserCircle;

    @FXML
    private TextField nameField;
    
    @FXML
    private Label editPicLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField lastNameField;
    
    @FXML
    private Label nameLabel;

    @FXML
    private Label passwordLabel;
    
    private File selectedImage;
    
    public Circle getPicChooserCircle() { return picChooserCircle; }
    
    public void setSelectedImage(File selectedImage) { this.selectedImage = selectedImage; }
    
    @FXML
    void initialize() {
    	selectedImage = null;
    	Image img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
    	picChooserCircle.setFill(new ImagePattern(img));
    
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    	nameLabel.setVisible(false);
    }
    
    @FXML
    void tryRegistration(ActionEvent event) {
    	nameLabel.setVisible(false);
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    	boolean valid = true;
    	
    	String usernameAnswer = Utilities.checkIfUsernameValid(usernameField.getText());
    	String passwordAnswer = Utilities.checkIfPasswordValid(passwordField.getText());
    	
    	if(!usernameAnswer.equals(Utilities.USERNAME_VALID)) {
    		usernameLabel.setText(usernameAnswer);
    		valid = false;
    	}
    	
    	if(!passwordAnswer.equals(Utilities.PASSWORD_VALID)) {
    		passwordLabel.setText(passwordAnswer);
    		valid = false;
    	}
    	
    	if(!Utilities.checkIfNameValid(lastNameField.getText()) || !Utilities.checkIfNameValid(nameField.getText())) {
    		nameLabel.setVisible(true);
    		valid = false;
    	}
    	
    	if(!passwordField.getText().equals(passwordConfirmField.getText())) {
    		passwordLabel.setText("Le due password non corrispondono");
    		valid = false;
    	}
    	
    	if(!valid)
    		return;
    	
    	LongUser utente = new LongUser(usernameField.getText(), nameField.getText(), lastNameField.getText());
    	utente.setPassword(passwordField.getText());
    	utente.setPropicFile(Utilities.getByteArrFromFile(selectedImage));
    	
    	if(Client.getInstance().requestRegistration(utente)) {
    		//TODO SceneHandler.showConfirm();
    		SceneHandler.getInstance().setLoginScene();
    	}
    	else {
    		Client.getInstance().resetClient();
    	}
    }
    
    @FXML
    void setPicture(MouseEvent event) {
		File file = FXUtilities.chooseImage();
		
		if(file != null) {
			try
			{
				Image img2 = new Image(new FileInputStream(file.getAbsolutePath()), 100, 100, true, true);
				picChooserCircle.setFill(new ImagePattern(img2));
				selectedImage = file;
			} catch (FileNotFoundException e) {
				//show error
			}
		}
    }
    
    @FXML
    void setLoginScene(ActionEvent event) {
    	SceneHandler.getInstance().setLoginScene();
    }

}
