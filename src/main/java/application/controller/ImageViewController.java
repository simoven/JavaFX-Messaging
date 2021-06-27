package application.controller;

import java.io.ByteArrayInputStream;

import application.graphics.ImageViewer;
import application.misc.FXUtilities;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    private StackPane thisRoot;
    
    @FXML
    private Button selectButton;
    
    public StackPane getThisRoot() {
		return thisRoot;
	}
    
    @FXML
    void initialize() {
    	ImageViewer.getInstance().setController(this);
    	closeButton.getStyleClass().add("imageViewerButton");
    	selectButton.getStyleClass().add("imageViewerButton");
    	paneVBox.setAlignment(Pos.CENTER);
    	imageView.fitWidthProperty().bind(thisRoot.prefWidthProperty().multiply(0.95));
    	imageView.fitHeightProperty().bind(thisRoot.prefHeightProperty().multiply(0.8));
    	paneVBox.getParent().getStyleClass().add("halfTransparent");
    }
    
    @FXML
    void closePanel(MouseEvent event) {
    	StackPane parent = (StackPane) thisRoot.getParent();
    	parent.getChildren().remove(thisRoot);
    }
    
    //aggiorno i constraints per adattarmi al pannello dove devo essere aggiunto
    public void updateConstraints(StackPane root) {
    	thisRoot.prefWidthProperty().unbind();
    	thisRoot.prefHeightProperty().unbind();
    	thisRoot.prefWidthProperty().bind(root.widthProperty());
    	thisRoot.prefHeightProperty().bind(root.heightProperty());
    }

	public void showImage(byte[] image) {
		imageView.setImage(new Image(new ByteArrayInputStream(image), paneVBox.getPrefWidth() * 1.5, paneVBox.getPrefHeight() * 1.5, true, true));
		
		ContextMenu menu = new ContextMenu();
		MenuItem item = new MenuItem("Salva");
		menu.getItems().add(item);
		
		item.setOnAction(evt-> { 
			menu.hide();
			FXUtilities.saveImage(image);
		});
		
		imageView.setOnContextMenuRequested(evt -> {
			menu.show(imageView.getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
		});
	}
	
	public void updateSelectButton(boolean isActive) {
		selectButton.setVisible(isActive);
		selectButton.setDisable(!isActive);
	}
	
	@FXML
    void confirmThisImage(MouseEvent event) {
		ImageViewer.getInstance().confirmFoto();
		closePanel(null);
    }
}
