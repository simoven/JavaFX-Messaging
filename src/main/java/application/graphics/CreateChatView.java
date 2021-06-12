package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.ChatChooserController;
import application.controller.CreateGroupController;
import application.logic.contacts.SingleContact;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

//Questa classe raccoglie i metodi che servono a mostrare le informazioni per creare delle nuove chat
public class CreateChatView {

	private static CreateChatView instance = null;
	private ChatChooserController chatChooserController = null;
	private CreateGroupController createGroupController = null;
	
	private CreateChatView() {}
	
	public static CreateChatView getInstance() {
		if(instance == null)
			instance = new CreateChatView();
		
		return instance;
	}
	
	public void setChatChooserController(ChatChooserController chatChooserController) {
		this.chatChooserController = chatChooserController; }
	
	public void setCreateGroupController(CreateGroupController createGroupController) {
		this.createGroupController = createGroupController; }
	
	public ChatChooserController getChatChooserController() {
		return chatChooserController; }
	
	public CreateGroupController getCreateGroupController() {
		return createGroupController; }
	
	//Questo metodo aggiunge i miei contatti nel pannello di selezione di una nuova chat
	public void appendContactInChoiceScreen(SingleContact contact, boolean isGlobalContact, boolean isForGroupCreation) {
		HBox container = new HBox();
		container.prefWidthProperty().bind(chatChooserController.getAlluserScrollpane().widthProperty());
		Circle shape = new Circle();
		shape.setRadius(25);
		Image img;
		if(contact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(contact.getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.jpeg").toExternalForm(), 100, 100, true, true);
			
		shape.setFill(new ImagePattern(img));
		container.getChildren().add(shape);
		HBox.setMargin(shape, new Insets(10));
		
		VBox textContainer = new VBox();
		Label username = new Label(contact.getUsername());
		textContainer.getChildren().add(username);
		VBox.setMargin(username, new Insets(10, 10, 5, 0));
		if(contact.getStatus() != null) {
			Label status = new Label(contact.getStatus());
			textContainer.getChildren().add(status);
			VBox.setMargin(status, new Insets(0, 10, 5, 0));
		}
		
		container.getChildren().add(textContainer);
		
		if(isForGroupCreation) {
			Pane spacer = new Pane();
			CheckBox checkBox = new CheckBox();
			container.getChildren().add(spacer);
			container.getChildren().add(checkBox);
			HBox.setHgrow(spacer, Priority.ALWAYS);
			HBox.setMargin(checkBox, new Insets(20, 20, 20, 10));
		}
		
		if(isGlobalContact && !isForGroupCreation) {
			Image world = new Image(getClass().getResourceAsStream("/application/images/world.png"), 200, 200, true, true);
			ImageView view = new ImageView(world);
			Pane spacer = new Pane();
			view.setFitHeight(20);
			view.setFitWidth(20);
			container.getChildren().add(spacer);
			container.getChildren().add(view);
			HBox.setHgrow(spacer, Priority.ALWAYS);
			HBox.setMargin(view, new Insets(20, 20, 20, 10));
		}
		
		Pane horizontaLine = new Pane();
		horizontaLine.getStyleClass().add("horizontaLine");
		horizontaLine.setPrefHeight(1);
		
		if(isForGroupCreation) {
			createGroupController.getPartecipantsVBox().getChildren().add(container);
			createGroupController.getPartecipantsVBox().getChildren().add(horizontaLine);
		}
		else {
			chatChooserController.getAllUsersVbox().getChildren().add(container);
			chatChooserController.getAllUsersVbox().getChildren().add(horizontaLine);
			container.addEventHandler(MouseEvent.MOUSE_CLICKED, chatChooserController);
		}
		
		VBox.setMargin(horizontaLine, new Insets(0, 10, 0, 10));
	}
}