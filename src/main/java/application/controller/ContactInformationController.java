package application.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import application.graphics.ChatDialog;
import application.graphics.ContactInfoView;
import application.graphics.ImageViewer;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
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
import javafx.scene.layout.StackPane;
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
    private Circle approveButton;

    @FXML
    private Circle cancelButton;
    
    @FXML
    private Circle changeNameButton;

    @FXML
    private VBox scrollPaneVBox;
    
    @FXML
    private MenuButton popupMenuButton;
    
    @FXML
    private Label myStatusLabel;
    
    @FXML
    private Label changeImageLabel;
    
    @FXML
    private StackPane myStackPane;
    
    @FXML
    private VBox infoVBox;
    
    private Image dotWhiteImage;
    
    private Image defaultGroup;
    
    @FXML
    private BorderPane root;
    
    private String previousName = "";
    
    private byte [] currentImage = null;
    
    public Label getInfoLabel() {return infoLabel; }
    
    public TextField getTextField1() { return textField1; }
    
    public TextField getTextField2() { return textField2; }
    
    public Label getStatusLabel() { return statusLabel; }
   
    public Circle getPropicCircle() { return propicCircle; }
    
    public VBox getScrollPaneVBox() { return scrollPaneVBox; }
    
    public Label getChangeImageLabel() { return changeImageLabel; }
    
    public MenuButton getPopupMenuButton() { return popupMenuButton; }
    
    public Label getMyStatusLabel() { return myStatusLabel; }
    
    private void setNameButtonsVisibility(boolean areVisible) {
    	approveButton.setVisible(areVisible);
		cancelButton.setVisible(areVisible);
		approveButton.setDisable(!areVisible);
		cancelButton.setDisable(!areVisible);
    }
    
    @FXML 
    void initialize() {
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	topHbox.prefHeightProperty().bind(SceneHandler.getInstance().getChatPane().heightProperty().multiply(0.05));
    	dotWhiteImage = new Image(getClass().getResourceAsStream("/application/images/3dot_white.png"), 30, 30, true, true);
    	backButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/backArrow.png"), 100, 100, true, true)));
    	defaultGroup = new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true);
    	changeNameButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/pencil.png"), 80, 80, true, true)));
    	cancelButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/cancelIcon.png"), 100, 100, true, true)));
    	approveButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/approveTicker.png"), 100, 100, true, true)));
    	popupMenuButton.setGraphic(new ImageView(dotWhiteImage));
    	infoVBox.getStyleClass().add("blackVBox");
    	disableChangeImageLabel();
    	ContactInfoView.getInstance().setController(this);
    	textField1.setEditable(false);
    	textField2.setEditable(false);
    }
    
    @FXML
    void backButtonPressed(MouseEvent event) {
    	SceneHandler.getInstance().setChatPane(true);
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
		changeNameButton.setDisable(false);
		changeNameButton.setVisible(true);
	}
	
	public void disableChangeImageLabel() {
		changeImageLabel.setDisable(true);
		changeImageLabel.setVisible(false);
		changeNameButton.setDisable(true);
		changeNameButton.setVisible(false);
		setNameButtonsVisibility(false);
	}
	
	@FXML
	//Questo metodod viene chiamato cliccando su "Cambia immagine"
    void changeGroupImage(MouseEvent event) {
		 int result = ChatDialog.getInstance().showCustomDialog(ChatDialog.PHOTO_DIALOG_NEW_PHOTO);
		 File selectedPhoto = null;
		 if(result == ChatDialog.NEW_PHOTO_OPTION) {
			selectedPhoto = FXUtilities.chooseImage();
			
			if(selectedPhoto != null)
				ImageViewer.getInstance().displayImageChooser(myStackPane, this, selectedPhoto);
		 }
		 else if (result == ChatDialog.REMOVE_PHOTO_OPTION) {
			 propicCircle.setFill(new ImagePattern(defaultGroup));
			 ChatLogic.getInstance().groupPictureChanged(null);
			 
		 }
    }
	
	//questo metodo viene chiamato dopo che confermo la foto da cambiare per il gruppo
	public void changeGroupPhoto(File image) {
		try {
			propicCircle.setFill(new ImagePattern(new Image(new FileInputStream(image), 200, 200, true, true)));
			ChatLogic.getInstance().groupPictureChanged(image);
		} catch (Exception e) {
			ChatDialog.getInstance().showResponseDialog("Impossibile caricare l'immagine, riprova");
		}
	}
	 
	 @FXML
    void cancelNameChange(MouseEvent event) {
		 textField1.setText(previousName);
		 textField1.setEditable(false);
		 setNameButtonsVisibility(false);
    }

    @FXML
    void confirmNameChange(MouseEvent event) {
    	if(textField1.getText().isBlank())
    		return;
    	
    	 textField1.setEditable(false);
		 setNameButtonsVisibility(false);
		 ChatLogic.getInstance().groupNameChanged(textField1.getText());
    }
    
    @FXML
    void prepareChangeName(MouseEvent event) {
    	previousName = textField1.getText();
    	textField1.setEditable(true);
    	textField1.requestFocus();
    	setNameButtonsVisibility(true);
    }
    
    @FXML
    void displayProfilePhoto(MouseEvent event) {
    	if(currentImage != null)
    		ImageViewer.getInstance().displayImageInPane(myStackPane, currentImage);
    }
    
    public void displayImage(byte[] img, boolean isGroupImage) {
    	currentImage = img;
    	if(img != null)
			propicCircle.setFill(new ImagePattern(new Image(new ByteArrayInputStream(img), 200, 200, true, true)));
		else {
			if(!isGroupImage)
				propicCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultSinglePic.png"), 200, 200, true, true)));
			else
				propicCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 200, 200, true, true)));
		}
    }
}