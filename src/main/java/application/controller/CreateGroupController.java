package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import application.graphics.CreateChatView;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class CreateGroupController {

	@FXML
	private AnchorPane root;
	
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
    private ScrollPane alluserScrollpane;
    
    @FXML
    private Label invalidNameLabel;
    
    private Image buttonDefault;
    
    private Image buttonPressed;
    
    private File selectedImage;
    
    public VBox getPartecipantsVBox() { return partecipantsVBox; }

    @FXML
    void initialize() {
    	buttonDefault = new Image(getClass().getResourceAsStream("/application/images/approveTicker.png"), 100, 100, true, true);
    	buttonPressed = new Image(getClass().getResourceAsStream("/application/images/approveTickerPressed.png"), 100, 100, true, true);
    	createGroupButton.setFill(new ImagePattern(buttonDefault));
    	
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	alluserScrollpane.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	partecipantsVBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	invalidNameLabel.setVisible(false);
    	groupProfilePic.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true)));
    	partecipantsVBox.getStyleClass().add("transparent");
    	groupNameLabel.getStyleClass().add("searchTextField");
    	CreateChatView.getInstance().setCreateGroupController(this);
    }
    
    @FXML
    void chooseImage(MouseEvent event) {
    	File file = FXUtilities.chooseImage();
    	
    	if(file != null) {
    		try {
	    		Image img2 = new Image(new FileInputStream(file.getAbsolutePath()), 100, 100, true, true);
				groupProfilePic.setFill(new ImagePattern(img2));
				selectedImage = file;
    		} catch (Exception e) {
    			//TODO cannot load image
    		}
    	}
    	else {
    		file = null;
    		groupProfilePic.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true)));
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
    	//Nella vbox aggiungo un hbox e un pane, quindi sono sicuro che troverò hbox con dentro una checkbox ogni due indici
    	for(int i = 0; i < partecipantsVBox.getChildren().size();) {
    		if(partecipantsVBox.getChildren().get(i) instanceof HBox) {
    			HBox container = (HBox) partecipantsVBox.getChildren().get(i);
    			CheckBox checkBox = (CheckBox) container.getChildren().get(container.getChildren().size() - 1);
    			if(checkBox.isSelected()) {
    				Label usernameLabel = (Label) ((VBox) container.getChildren().get(1)).getChildren().get(0);
    				selectedContactsName.add(usernameLabel.getText());
    			}
    				
    		}
    		
    		i = i + 2;
    	}
    	
    	if(selectedContactsName.size() > 0)
    		ChatLogic.getInstance().createGroup(name, selectedImage, selectedContactsName);
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
