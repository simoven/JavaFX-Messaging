package application.controller;

import java.io.File;

import application.graphics.ChatDialog;
import application.graphics.ImageViewer;
import application.graphics.MyProfileView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import application.net.misc.Utilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class MyProfileController {

    @FXML
    private HBox topHbox;
    
    @FXML
    private StackPane myStackPane;

    @FXML
    private Label infoLabel;

    @FXML
    private Circle propicCircle;

    @FXML
    private Label changePicLabel;

    @FXML
    private TextField statusTextField;

    @FXML
    private VBox fieldVBox;

    @FXML
    private BorderPane root;

    @FXML
    private Circle backButton;

    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label rowCountLabel;

    @FXML
    private Button editStatusButton;

    @FXML
    private Label nameLabel;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private HBox statusHBox;
    
    @FXML
    private PasswordField oldPasswordField;
    
    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button changePasswordButton;
    
    @FXML
    private HBox passwordHBox;
    
    @FXML
    private Label passwordInfoLabel;

    @FXML
    private MenuButton popupMenuButton;
    
    private Image defaultImage;
    
    private Image dotImage;
    
    public Circle getPropicCircle() {
		return propicCircle; }
    
    public Label getNameLabel() {
		return nameLabel; }
    
    public Label getUsernameLabel() {
		return usernameLabel; }
    
    public TextField getStatusTextField() {
	    return statusTextField; }
    
    public Image getDefaultImage() {
		return defaultImage; }
    
    public Label getRowCountLabel() {
		return rowCountLabel; }

    @FXML
    void backButtonPressed(MouseEvent event) {
    	SceneHandler.getInstance().setChatPane();
    	passwordHBox.setVisible(false);
    	passwordInfoLabel.setText("");
    }
    
    @FXML
    void initialize() {
    	MyProfileView.getInstance().setController(this);
    	dotImage = new Image(getClass().getResourceAsStream("/application/images/3dot_white.png"), 30, 30, true, true);
    	defaultImage = new Image(getClass().getResourceAsStream("/application/images/defaultSinglePic.png"), 100, 100, true, true);
    	statusTextField.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.60));
    	oldPasswordField.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.30));
    	backButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/backArrow.png"), 100, 100, true, true)));
    	popupMenuButton.setGraphic(new ImageView(dotImage));
    	editStatusButton.getStyleClass().add("newChatButtons");
    	changePasswordButton.getStyleClass().add("redButton");
    	statusTextField.getStyleClass().add("searchTextField");
    	oldPasswordField.getStyleClass().add("searchTextField");
    	newPasswordField.getStyleClass().add("searchTextField");
    	confirmPasswordField.getStyleClass().add("searchTextField");
    	passwordInfoLabel.setText("");
    	passwordHBox.setVisible(false);
    	editStatusButton.setVisible(false);
    	editStatusButton.setDisable(true);
    }
    
    @FXML
    void changeProfilePic(MouseEvent event) {
    	int res = ChatDialog.getInstance().showPhotoOptionDialog();
    	File selectedPhoto = null;
    	if(res == ChatDialog.NEW_PHOTO_OPTION) 
    		selectedPhoto = FXUtilities.chooseImage();
    	else if(res == ChatDialog.REMOVE_PHOTO_OPTION) 
    		selectedPhoto = null;
    	else 
			 return;
		 
		 if(selectedPhoto == null) {
			 propicCircle.setFill(new ImagePattern(defaultImage));
			 ChatLogic.getInstance().updateMyPhoto(null);
		 }
		 else 
			 ImageViewer.getInstance().displayImageChooser(myStackPane, this, selectedPhoto);
    }
    
    public void updatePhoto(File photo) {
    	try {
    		//propicCircle.setFill(new ImagePattern(new Image(new FileInputStream(photo), 200, 200, true, true)));
    		ChatLogic.getInstance().updateMyPhoto(photo);
    	} catch (Exception e) {
    		//TODO
    		e.printStackTrace();
    	}
    }
    
    @FXML
    void displayMyPic(MouseEvent event) {
    	ImageViewer.getInstance().displayImageInPane(myStackPane, ChatLogic.getInstance().getMyInformation().getProfilePic());
    }

    @FXML
    void onStatusFieldKeyPressed(KeyEvent event) {
    	if(event.getCode() != KeyCode.SPACE) {
    		editStatusButton.setVisible(true);
    		editStatusButton.setDisable(false);
    	}
    	
    	if(statusTextField.getText().length() > 90)
    		statusTextField.setText(statusTextField.getText().substring(0, 90));
    	
    	rowCountLabel.setText(statusTextField.getText().length() + "/90");
    }
    
    @FXML
    void changeStatus(MouseEvent event) {
    	ChatLogic.getInstance().updateMyStatus(statusTextField.getText());
    	editStatusButton.setVisible(false);
    	editStatusButton.setDisable(true);
    }
    
    @FXML
    void changePassword(MouseEvent event) {
    	if(oldPasswordField.getText().isBlank()) {
    		passwordInfoLabel.setText("La vecchia password non può essere vuota");
    		return;
    	}
    	
    	String answer = Utilities.checkIfPasswordValid(oldPasswordField.getText());
    	if(answer != Utilities.PASSWORD_VALID) {
    		passwordInfoLabel.setText(answer);
    		return;
    	}
    	
    	if(newPasswordField.getText().isBlank() || confirmPasswordField.getText().isBlank()) {
    		passwordInfoLabel.setText("La nuova password non può essere vuota");
    		return;
    	}
    	
    	if(!newPasswordField.getText().equals(confirmPasswordField.getText())) {
    		passwordInfoLabel.setText("Le due password non corrispondono");
    		return;
    	}
    	
    	answer = Utilities.checkIfPasswordValid(newPasswordField.getText());
    	if(answer != Utilities.PASSWORD_VALID) 
    		answer = ChatLogic.getInstance().checkPasswordErrorText(answer);
    	
    	if(oldPasswordField.getText().equals(newPasswordField.getText())) {
    		passwordInfoLabel.setText("La vecchia e nuova password non possono essere uguali");
    		return;
    	}
    	
    	passwordInfoLabel.setText("Password valida");
    	ChatLogic.getInstance().changePassword(oldPasswordField.getText(), newPasswordField.getText());
    }
    
    @FXML
    void showChangePassword(ActionEvent event) {
    	passwordHBox.setVisible(true);
    	passwordInfoLabel.setText("");
    	scrollPane.setVvalue(1);
    }
    
    @FXML
    void logOut(ActionEvent event) {
    	ChatLogic.getInstance().resetLogic();
    }
}

