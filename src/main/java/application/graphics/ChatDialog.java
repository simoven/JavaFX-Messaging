package application.graphics;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatDialog {

	public static final int NEW_PHOTO_OPTION = 1;
	public static final int REMOVE_PHOTO_OPTION = 0;
	
	public static int result = -1;
	
	private static ChatDialog instance = null;
	private Stage window;
	private Scene scene;
	private VBox parent;
	
	private ChatDialog() {
		parent = new VBox();
		scene = new Scene(parent);
		window = new Stage();
		window.setTitle("Conferma scelta");
		window.setResizable(false);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setScene(scene);
		scene.getStylesheets().add(getClass().getResource("/application/dialogStyle.css").toExternalForm());
	}
	
	public static ChatDialog getInstance() {
		if(instance == null)
			instance = new ChatDialog();
		
		return instance;
	}
	
	public int showPhotoOptionDialog() {
		result = -1;
		parent.getChildren().clear();
		Label text = new Label("Vuoi cambiare immagine o eliminare l' attuale ?");
		text.setWrapText(true);
		
		Button newPhoto = new Button ("Nuova foto");
		Button removeOld = new Button("Elimina attuale");
		
		newPhoto.getStyleClass().add("blueButton");
		removeOld.getStyleClass().add("redButton");
		
		HBox container = new HBox();
		container.getChildren().add(removeOld);
		container.getChildren().add(newPhoto);
		
		parent.setAlignment(Pos.CENTER);
		newPhoto.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		removeOld.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		
		newPhoto.setOnAction(ev -> {
			result = NEW_PHOTO_OPTION;
			window.close();
		});
		
		removeOld.setOnMousePressed(ev -> {
			result = REMOVE_PHOTO_OPTION;
			window.close();
		});
		
		parent.getChildren().add(text);
		parent.getChildren().add(container);
		HBox.setHgrow(container, Priority.ALWAYS);
		HBox.setHgrow(newPhoto, Priority.ALWAYS);
		HBox.setHgrow(removeOld, Priority.ALWAYS);
		VBox.setMargin(text, new Insets(20));
		
		window.showAndWait();
		
		return result;
	}
}
