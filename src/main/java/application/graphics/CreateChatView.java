package application.graphics;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

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
	private ArrayList <SingleContact> usersNotInGroup = null;
	
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
	
	//Questo metodo aggiunge i miei contatti nel pannello di selezione di una nuova chat oppure per la creazione di un gruppo
	public void appendContactInChoiceScreen(SingleContact contact, boolean isGlobalContact, boolean showCheckBox, boolean isForGroupAdd) {
		HBox container = new HBox();
		container.prefWidthProperty().bind(chatChooserController.getAlluserScrollpane().widthProperty());
		Circle shape = new Circle();
		shape.setRadius(25);
		Image img;
		if(contact.getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(contact.getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultSinglePic.png").toExternalForm(), 100, 100, true, true);
			
		shape.setFill(new ImagePattern(img));
		container.getChildren().add(shape);
		HBox.setMargin(shape, new Insets(10));
		
		VBox textContainer = new VBox();
		Label username = new Label(contact.getUsername());
		username.getStyleClass().add("contactUsernameLabel");
		textContainer.getChildren().add(username);
		VBox.setMargin(username, new Insets(10, 10, 5, 0));
		if(contact.getStatus() != null) {
			Label status = new Label(contact.getStatus());
			status.getStyleClass().add("contactStatusLabel");
			textContainer.getChildren().add(status);
			VBox.setMargin(status, new Insets(0, 10, 5, 0));
		}
		
		container.getChildren().add(textContainer);
		
		//Se è per la creazione di un gruppo, aggiungo la checkbox
		if(showCheckBox) {
			Pane spacer = new Pane();
			CheckBox checkBox = new CheckBox();
			container.getChildren().add(spacer);
			container.getChildren().add(checkBox);
			HBox.setHgrow(spacer, Priority.ALWAYS);
			HBox.setMargin(checkBox, new Insets(20, 20, 20, 10));
		}
		
		//Se invece è un contatto uscito da una ricerca globale, aggiungo una icona del mondo
		if(isGlobalContact && !showCheckBox) {
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
		
		if(showCheckBox && !isForGroupAdd) {
			if(createGroupController.getPartecipantsVBox().getChildren().size() > 1)
				createGroupController.getPartecipantsVBox().getChildren().add(horizontaLine);
			
			createGroupController.getPartecipantsVBox().getChildren().add(container);
		}
		else {
			chatChooserController.getAllUsersVbox().getChildren().add(container);
			chatChooserController.getAllUsersVbox().getChildren().add(horizontaLine);
			container.addEventHandler(MouseEvent.MOUSE_CLICKED, chatChooserController);
		}
		
		VBox.setMargin(horizontaLine, new Insets(0, 10, 0, 10));
	}

	public void changeButtonUse(boolean isForAddingTogroup) {
		chatChooserController.setButtonsForGroupAdd(isForAddingTogroup);
	}

	public void setGroupIdForAdd(int groupId) {
		chatChooserController.setGroupIdForAdd(groupId);
	}

	public void clearContactVBox() {
		chatChooserController.getAllUsersVbox().getChildren().clear();
	}

	public void setUserNotInGroup(ArrayList<SingleContact> contactsToAdd) {
		usersNotInGroup = contactsToAdd;
	}
	
	//Questi due metodi mostrano i contatti che non sono in un gruppo e che sono stati "calcolati" prima in chatlogic
	public void showPreviousFetchedContacts() {
		chatChooserController.getAllUsersVbox().getChildren().clear();
		for(SingleContact contact : usersNotInGroup) 
			appendContactInChoiceScreen(contact, false, true, true);
	}

	public void showPreviousFetchedContactsFiltered(String subUsername) {
		chatChooserController.getAllUsersVbox().getChildren().clear();
		for(SingleContact contact : usersNotInGroup) 
			if(contact.getUsername().contains(subUsername)) 
				CreateChatView.getInstance().appendContactInChoiceScreen((SingleContact) contact, false, true, true);
	}

	//Questo metodo aggiunge il label "aggiungi partecipanti" alla vbox dove ci sono i contatti
	public void appendPartecipantsLabel() {
		Label label = new Label("Aggiungi partecipanti");
		label.setStyle("-fx-text-fill: #7289da");
		createGroupController.getPartecipantsVBox().getChildren().add(label);
		VBox.setMargin(label, new Insets(5, 0, 5, 10));
		
	}
}
