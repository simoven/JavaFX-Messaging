package application.controller;

import java.io.ByteArrayInputStream;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ImageViewController {

    @FXML
    private Button closeButton;

    @FXML
    private ImageView imageView;
    
    @FXML
    private VBox paneVBox;
    
    @FXML
    private StackPane root;
    
    @FXML
    void initialize() {
    	ChatView.getInstance().setImageViewController(this);
    	paneVBox.prefWidthProperty().bind(SceneHandler.getInstance().getChatPaneStackPane().widthProperty());
    	paneVBox.prefHeightProperty().bind(SceneHandler.getInstance().getChatPaneStackPane().heightProperty());
    	paneVBox.setAlignment(Pos.CENTER);
    	imageView.fitWidthProperty().bind(paneVBox.prefWidthProperty().multiply(0.95));
    	imageView.fitHeightProperty().bind(paneVBox.prefHeightProperty().multiply(0.9));
    	paneVBox.getParent().getStyleClass().add("halfTransparent");
    }
    
    @FXML
    void closePanel(MouseEvent event) {
    	SceneHandler.getInstance().closeImageScene();
    }

	public void handleClick(byte[] image) {
		imageView.setImage(new Image(new ByteArrayInputStream(image), paneVBox.getPrefWidth() * 1.5, paneVBox.getPrefHeight() * 1.5, true, true));
    	SceneHandler.getInstance().setImageScene(); 
	}
}
