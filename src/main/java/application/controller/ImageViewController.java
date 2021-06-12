package application.controller;

import java.io.ByteArrayInputStream;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ImageViewController {

    @FXML
    private Button closeButton;

    @FXML
    private ImageView imageView;
    
    @FXML
    void initialize() {
    	imageView.fitWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	ChatView.getInstance().setImageViewController(this);
    	//imageView.fitHeightProperty().bind();
    }
    
    @FXML
    void closePanel(MouseEvent event) {
    	SceneHandler.getInstance().closeImageScene();
    }

	public void handleClick(byte[] image) {
		imageView.setImage(new Image(new ByteArrayInputStream(image), 500, 500, true, true));
    	SceneHandler.getInstance().setImageScene();
		
	}

}
