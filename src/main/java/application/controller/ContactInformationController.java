package application.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import application.graphics.ChatDialog;
import application.graphics.ContactInfoView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import application.net.client.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ContactInformationController implements EventHandler <ActionEvent> {

    @FXML
    private HBox topHbox;

    @FXML
    private ScrollPane bottomScrollPane;

    @FXML
    private Label infoLabel;

    @FXML
    private Circle propicCircle;

    @FXML
    private TextField textField2;

    @FXML
    private TextField textField1;

    @FXML
    private VBox fieldVBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Circle backButton;

    @FXML
    private VBox scrollPaneVBox;
    
    @FXML
    private MenuButton popupMenuButton;
    
    @FXML
    private Label myStatusLabel;
    
    @FXML
    private Label changeImageLabel;
    
    private Image dotWhiteImage;
    
    private Image defaultGroup;
    
    @FXML
    private BorderPane root;
    
    public Label getInfoLabel() {return infoLabel; }
    
    public TextField getTextField1() { return textField1; }
    
    public TextField getTextField2() { return textField2; }
    
    public Label getStatusLabel() { return statusLabel; }
   
    public Circle getPropicCircle() { return propicCircle; }
    
    public VBox getScrollPaneVBox() { return scrollPaneVBox; }
    
    public Label getChangeImageLabel() { return changeImageLabel; }
    
    public MenuButton getPopupMenuButton() { return popupMenuButton; }
    
    public Label getMyStatusLabel() { return myStatusLabel; }
    
    @FXML 
    void initialize() {
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	dotWhiteImage = new Image(getClass().getResourceAsStream("/application/images/3dot_white.png"), 30, 30, true, true);
    	backButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/backArrow.png"), 100, 100, true, true)));
    	defaultGroup = new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true);
    	popupMenuButton.setGraphic(new ImageView(dotWhiteImage));
    	disableChangeImageLabel();
    	ContactInfoView.getInstance().setController(this);
    	textField1.setEditable(false);
    	textField2.setEditable(false);
    }
    
    @FXML
    void backButtonPressed(MouseEvent event) {
    	SceneHandler.getInstance().setChatPane();
    }

	@Override
	public void handle(ActionEvent event) {
		String choice = ((MenuItem) event.getSource()).getText();
		
		if(choice.equals("Aggiungi contatto")) {
			ChatLogic.getInstance().setContactVisibility(textField2.getText().substring(1), true);
			((MenuItem) event.getSource()).setText("Rimuovi contatto");
		}
		else if(choice.equals("Rimuovi contatto")) {
			ChatLogic.getInstance().setContactVisibility(textField2.getText().substring(1), false);
			((MenuItem) event.getSource()).setText("Aggiungi contatto");
		}
	}
	
	public void enableChangeImageLabel() {
		changeImageLabel.setDisable(false);
		changeImageLabel.setVisible(true);
	}
	
	public void disableChangeImageLabel() {
		changeImageLabel.setDisable(true);
		changeImageLabel.setVisible(false);
	}
	
	 @FXML
    void changeGroupImage(MouseEvent event) {
		 int result = ChatDialog.getInstance().showPhotoOptionDialog();
		 File selectedPhoto = null;
		 if(result == ChatDialog.NEW_PHOTO_OPTION)
			selectedPhoto = FXUtilities.chooseImage(); 
		 else if (result == ChatDialog.REMOVE_PHOTO_OPTION)
			 selectedPhoto = null;
		 else 
			 return;
		 
		 if(selectedPhoto == null)
			 propicCircle.setFill(new ImagePattern(defaultGroup));
		 else {
			 try {
				 propicCircle.setFill(new ImagePattern(new Image(new FileInputStream(selectedPhoto), 100, 100, true, true)));
			 } catch (Exception e) {
				 //TODO show error
			 }
		 }
		 
		 ChatLogic.getInstance().groupPictureChanged(selectedPhoto);
    }
}