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

	public static final int REMOVE_PHOTO_OPTION = 0;
	public static final int NEW_PHOTO_OPTION = 1;
	public static final int APPROVE_OPTION = 2;
	public static final int DISCARD_OPTION = 3;
	public static final int RETRY_OPTION = 4;
	public static final int OK_OPTION = 5;
	public static final int REMOVE_FOR_ME_OPTION = 6;
	public static final int REMOVE_FOR_ALL_OPTION = 7;
	
	public static final int PHOTO_DIALOG_WALLPAPER = 10;
	public static final int PHOTO_DIALOG_NEW_PHOTO = 11;
	public static final int CONFIRM_DIALOG_DELETE_MESSAGE = 12;
	
	public static int result = -1;
	
	private static ChatDialog instance = null;
	private Stage window;
	private Scene scene;
	private VBox parent;
	
	private ChatDialog() {
		parent = new VBox();
		scene = new Scene(parent);
		window = new Stage();
		window.setResizable(false);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setScene(scene);
		scene.getStylesheets().add(getClass().getResource("/application/styles/dialogStyle.css").toExternalForm());
	}
	
	public static ChatDialog getInstance() {
		if(instance == null)
			instance = new ChatDialog();
		
		return instance;
	}
	
	public int showCustomDialog(int option) {
		result = -1;
		parent.getChildren().clear();
		window.setTitle("Conferma scelta");
		Label text = new Label();
		
		if(option == PHOTO_DIALOG_NEW_PHOTO)
			text.setText("Scegli se cambiare immagine o eliminare l'attuale");
		
		else if(option == PHOTO_DIALOG_WALLPAPER)
			text.setText("Scegli se impostare lo sfondo di default o uno nuovo");
		
		else if(option == CONFIRM_DIALOG_DELETE_MESSAGE)
			text.setText("Stai per eliminare il messaggio, conferma");
		
		text.setWrapText(true);
		
		Button leftButton = new Button();
		Button rightButton = new Button();
		
		if(option == PHOTO_DIALOG_NEW_PHOTO) {
			leftButton.setText("Elimina attuale");
			rightButton.setText("Nuova foto");
		}
		else if(option == PHOTO_DIALOG_WALLPAPER) {
			leftButton.setText("Sfondo default");
			rightButton.setText("Nuovo sfondo");
		}
		else if(option == CONFIRM_DIALOG_DELETE_MESSAGE) {
			leftButton.setText("Elimina per tutti");
			rightButton.setText("Elimina per me");
		}
		
		leftButton.getStyleClass().add("redButton");
		rightButton.getStyleClass().add("blueButton");
		
		HBox container = new HBox();
		container.getChildren().add(leftButton);
		container.getChildren().add(rightButton);
		
		parent.setAlignment(Pos.CENTER);
		leftButton.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		rightButton.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		
		leftButton.setOnAction(ev -> {
			if(option == PHOTO_DIALOG_NEW_PHOTO || option == PHOTO_DIALOG_WALLPAPER)
				result = REMOVE_PHOTO_OPTION;
			else if(option == CONFIRM_DIALOG_DELETE_MESSAGE)
				result = REMOVE_FOR_ALL_OPTION;
			
			window.close();
		});
		
		rightButton.setOnMousePressed(ev -> {
			if(option == PHOTO_DIALOG_NEW_PHOTO || option == PHOTO_DIALOG_WALLPAPER)
				result = NEW_PHOTO_OPTION;
			else if(option == CONFIRM_DIALOG_DELETE_MESSAGE)
				result = REMOVE_FOR_ME_OPTION;
			
			window.close();
		});
		
		parent.getChildren().add(text);
		parent.getChildren().add(container);
		HBox.setHgrow(container, Priority.ALWAYS);
		HBox.setHgrow(leftButton, Priority.ALWAYS);
		HBox.setHgrow(rightButton, Priority.ALWAYS);
		VBox.setMargin(text, new Insets(20));
		
		window.showAndWait();
		
		return result;
	}
	
	public int showConfirmDialog(String testo) {
		result = -1;
		parent.getChildren().clear();
		window.setTitle("Conferma scelta");
		Label text = new Label(testo);
		text.setWrapText(true);
		
		Button approve = new Button ("Conferma");
		Button discard = new Button("Annulla");
		
		approve.getStyleClass().add("blueButton");
		discard.getStyleClass().add("redButton");
		
		HBox container = new HBox();
		container.getChildren().add(discard);
		container.getChildren().add(approve);
		
		parent.setAlignment(Pos.CENTER);
		approve.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		discard.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		
		approve.setOnAction(ev -> {
			result = APPROVE_OPTION;
			window.close();
		});
		
		discard.setOnMousePressed(ev -> {
			result = DISCARD_OPTION;
			window.close();
		});
		
		parent.getChildren().add(text);
		parent.getChildren().add(container);
		HBox.setHgrow(container, Priority.ALWAYS);
		VBox.setMargin(text, new Insets(20));
		
		window.showAndWait();
		
		return result;
	}
	
	public void showResponseDialog(String testo) {
		parent.getChildren().clear();
		window.setTitle("Esito operazione");
		Label text = new Label(testo);
		text.setWrapText(true);
		
		Button approve = new Button ("Ho capito");
		
		approve.getStyleClass().add("blueButton");
		
		HBox container = new HBox();
		container.getChildren().add(approve);
		
		parent.setAlignment(Pos.CENTER);
		approve.prefWidthProperty().bind(parent.widthProperty());
		
		approve.setOnAction(ev -> {
			window.close();
		});
		
		parent.getChildren().add(text);
		parent.getChildren().add(container);
		HBox.setHgrow(container, Priority.ALWAYS);
		VBox.setMargin(text, new Insets(20));
		
		window.showAndWait();
	}
	
	public int showErrorDialog(String testo) {
		result = -1;
		parent.getChildren().clear();
		window.setTitle("Errore nella richiesta");
		Label text = new Label(testo);
		text.setWrapText(true);
		
		Button confirm = new Button ("Chiudi");
		Button retry = new Button("Riprova");
		
		confirm.getStyleClass().add("redButton");
		retry.getStyleClass().add("blueButton");
		
		HBox container = new HBox();
		container.getChildren().add(confirm);
		container.getChildren().add(retry);
		
		parent.setAlignment(Pos.CENTER);
		confirm.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		retry.prefWidthProperty().bind(parent.widthProperty().multiply(0.5));
		
		confirm.setOnAction(ev -> {
			result = OK_OPTION;
			window.close();
		});
		
		retry.setOnMousePressed(ev -> {
			result = RETRY_OPTION;
			window.close();
		});
		
		parent.getChildren().add(text);
		parent.getChildren().add(container);
		HBox.setHgrow(container, Priority.ALWAYS);
		VBox.setMargin(text, new Insets(20));
		
		window.showAndWait();
		
		return result;
	}
}
