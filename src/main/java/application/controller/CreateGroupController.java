package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import application.graphics.ChatDialog;
import application.graphics.CreateChatView;
import application.graphics.ImageViewer;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import application.net.misc.Utilities;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class CreateGroupController {

	@FXML
	private BorderPane root;
	
    @FXML
    private HBox topHbox;
    
    @FXML
    private TextField groupNameLabel;

    @FXML
    private Circle groupProfilePic;

    @FXML
    private VBox partecipantsVBox;

    @FXML
    private Circle createGroupButton;
    
    @FXML
    private StackPane myStackPane;

    @FXML
    private ScrollPane alluserScrollpane;
    
    @FXML
    private Circle backButton;
    
    @FXML
    private Label invalidNameLabel;
    
    private Image buttonDefault;
    
    private Image buttonPressed;
    
    private Image defaultGroupIcon;
    
    private File selectedImage;
    
    public VBox getPartecipantsVBox() { return partecipantsVBox; }

    @FXML
    void initialize() {
    	buttonDefault = new Image(getClass().getResourceAsStream("/application/images/approveTicker.png"), 100, 100, true, true);
    	buttonPressed = new Image(getClass().getResourceAsStream("/application/images/approveTickerPressed.png"), 100, 100, true, true);
    	defaultGroupIcon = new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true);
    	createGroupButton.setFill(new ImagePattern(buttonDefault));
    	
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	topHbox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	alluserScrollpane.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	partecipantsVBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	invalidNameLabel.setVisible(false);
    	groupProfilePic.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true)));
    	backButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/backArrow.png"), 100, 100, true, true)));
    	partecipantsVBox.getStyleClass().add("transparent");
    	groupNameLabel.getStyleClass().add("searchTextField");
    	CreateChatView.getInstance().setCreateGroupController(this);
    }
    
    @FXML
    void backButtonPressed(MouseEvent event) {
    	if(ChatLogic.getInstance().getActiveChat() != null)
    		SceneHandler.getInstance().setChatPane(true);
    	else
    		SceneHandler.getInstance().setDefaultChatPane();
    }
    
    private void removeGroupImage() {
    	selectedImage = null;
		groupProfilePic.setFill(new ImagePattern(defaultGroupIcon));
    }
    
    @FXML
    void chooseImage(MouseEvent event) {
    	if(selectedImage != null) {
    		removeGroupImage();
    		return;
    	}
    	
    	File file = FXUtilities.chooseImage();
    	
    	if(file != null)
    		ImageViewer.getInstance().displayImageChooser(myStackPane, this, file);
    	else {
    		file = null;
    		groupProfilePic.setFill(new ImagePattern(defaultGroupIcon));
    	}
    }
    
    public void confirmImage(File img) {
    	try {
    		Image img2 = new Image(new FileInputStream(img), 100, 100, true, true);
			groupProfilePic.setFill(new ImagePattern(img2));
			selectedImage = img;
		} catch (Exception e) {
			ChatDialog.getInstance().showResponseDialog("Impossibile caricare l'immagine, riprova");
		}
    }
    
    @FXML
    void createGroup(MouseEvent event) {
    	invalidNameLabel.setVisible(false);
    	
    	String name = groupNameLabel.getText();
    	if(name  == null || !Utilities.checkIfGroupNameValid(name)) {
    		invalidNameLabel.setVisible(true);
    		return;
    	}
    	
    	ArrayList <String> selectedContactsName = new ArrayList <String>();
    	//Nella vbox aggiungo un hbox e un pane, quindi scorro il for ogni due indici, a partire da 1 perché c'è la label
    	for(int i = 1; i < partecipantsVBox.getChildren().size(); i = i + 2) {
    		if(partecipantsVBox.getChildren().get(i) instanceof HBox) {
    			HBox container = (HBox) partecipantsVBox.getChildren().get(i);
    			CheckBox checkBox = (CheckBox) container.getChildren().get(container.getChildren().size() - 1);
    			if(checkBox.isSelected()) {
    				Label usernameLabel = (Label) ((VBox) container.getChildren().get(1)).getChildren().get(0);
    				selectedContactsName.add(usernameLabel.getText());
    			}
    		}
    	}
    	
    	if(selectedContactsName.size() > 0)
    		ChatLogic.getInstance().createGroup(name, selectedImage, selectedContactsName);
    	
    	groupNameLabel.setText("");
    	invalidNameLabel.setVisible(false);
    	removeGroupImage();
    }
    
    @FXML
    void buttonPressed(MouseEvent event) {
    	createGroupButton.setFill(new ImagePattern(buttonPressed));
    }

    @FXML
    void buttonReleased(MouseEvent event) {
    	createGroupButton.setFill(new ImagePattern(buttonDefault));
    }

}
