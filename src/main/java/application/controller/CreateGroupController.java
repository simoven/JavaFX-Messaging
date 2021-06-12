package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import application.graphics.CreateChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.misc.FXUtilities;
import application.net.misc.Utilities;
import javafx.event.ActionEvent;
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
    private HBox topHBox;
    
    @FXML
    private TextField groupNameLabel;

    @FXML
    private Circle groupProfilePic;

    @FXML
    private VBox partecipantsVBox;

    @FXML
    private Circle createGroupButton;

    @FXML
    private ScrollPane partecipantScrollPane;
    
    @FXML
    private Label invalidNameLabel;
    
    private File selectedImage;
    
    public VBox getPartecipantsVBox() { return partecipantsVBox; }

    @FXML
    void initialize() {
    	createGroupButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/approveTicker.png"), 100, 100, true, true)));
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	//topHBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	partecipantScrollPane.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	partecipantsVBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	invalidNameLabel.setVisible(false);
    	groupProfilePic.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/defaultGroup.png"), 100, 100, true, true)));
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
    	
    	if(selectedContactsName.size() > 0) {
    		ChatLogic.getInstance().createGroup(name, selectedImage, selectedContactsName);
    	}
    }

}
