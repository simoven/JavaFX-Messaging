package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import application.graphics.ChatDialog;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordConfirmField;

    @FXML
    private Button registerButton;
    
    @FXML
    private ImageView chatIcon;

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
    private Circle helpButton;
    
    @FXML
    private Label nameLabel;

    @FXML
    private Label passwordLabel;
    
    private File selectedImage;
    
    private Image defaultPic;
    
    public Circle getPicChooserCircle() { return picChooserCircle; }
    
    public void setSelectedImage(File selectedImage) { this.selectedImage = selectedImage; }
    
    @FXML
    void initialize() {
    	selectedImage = null;
    	defaultPic = new Image(getClass().getResource("/application/images/defaultSinglePic.png").toExternalForm(), 100, 100, true, true);
    	picChooserCircle.setFill(new ImagePattern(defaultPic));
    
    	usernameLabel.setText("");
    	passwordLabel.setText("");
    	helpButton.setVisible(false);
    	nameLabel.setVisible(false);
    	
    	loginButton.getStyleClass().add("loginRegistrationButtons");
    	registerButton.getStyleClass().add("loginRegistrationButtons");
    	helpButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/questionMark.png"), 100, 100, true, true)));
    	chatIcon.setImage(new Image(getClass().getResourceAsStream("/application/images/chatHome.png"), 142, 142, true, true));
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
    		
    		if(passwordAnswer.equals(Utilities.PASSWORD_NOT_VALID))
    			helpButton.setVisible(true);
    		
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
    		ChatDialog.getInstance().showResponseDialog("La registrazione è avvenuta con successo");
    		SceneHandler.getInstance().setLoginScene();
    	}
    	else 
    		Client.getInstance().resetClient();
    	
    	clearField();
    }
    
    private void clearField() {
    	usernameField.setText("");
    	nameField.setText("");
    	lastNameField.setText("");
    	passwordField.setText("");
    	passwordConfirmField.setText("");
	}
    
    private void hidelabels() {
    	usernameLabel.setText("");
    	nameLabel.setVisible(false);
    	passwordLabel.setText("");
    	helpButton.setVisible(false);
    }

	@FXML
    void setPicture(MouseEvent event) {
    	if(selectedImage != null) {
    		selectedImage = null;
			picChooserCircle.setFill(new ImagePattern(defaultPic));
			return;
    	}
    	
		File file = FXUtilities.chooseImage();
		
		if(file != null) {
			try
			{
				Image img2 = new Image(new FileInputStream(file.getAbsolutePath()), 100, 100, true, true);
				picChooserCircle.setFill(new ImagePattern(img2));
				selectedImage = file;
			} catch (FileNotFoundException e) {
				ChatDialog.getInstance().showResponseDialog("Impossibile caricare l'immagine, riprova");
			}
		}
    }
    
    @FXML
    void setLoginScene(ActionEvent event) {
    	hidelabels();
    	SceneHandler.getInstance().setLoginScene();
    }
    
    @FXML
    void showHelp(MouseEvent event) {
    	String text = "La password deve contenere almeno :\n" +
    				  "  \n- Un carattere maiuscolo " + 
    				  "  \n- Un carattere minuscolo " + 
    				  "  \n- Un carattere numerico " + 
    				  "  \n- Un carattere tra @.?#$%^&+=! ";
    	ChatDialog.getInstance().showResponseDialog(text);
    }

}
