package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.ContactInformationController;
import application.logic.ChatLogic;
import application.logic.chat.GroupChat;
import application.logic.contacts.SingleContact;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ContactInfoView {

	private static ContactInfoView instance;
	private ContactInformationController controller;
	
	private ContactInfoView() {}
	
	public static ContactInfoView getInstance() {
		if(instance == null)
			instance = new ContactInfoView();
		
		return instance;
	}
	
	public void setController(ContactInformationController controller) {
		this.controller = controller;
	}
	
	public ContactInformationController getController() {
		return controller;
	}
	
	//Mostra le informazioni su un contatto singolo
	public void showInfo(SingleContact contact) {
		controller.getPopupMenuButton().getItems().clear();
		controller.getStatusLabel().setVisible(true);
		controller.getMyStatusLabel().setVisible(true);
		controller.getScrollPaneVBox().getChildren().clear();
		controller.disableChangeImageLabel();
		
		controller.getInfoLabel().setText("Info sul contatto");
		controller.getTextField2().setText("@" + contact.getUsername());
		controller.getTextField1().setText(contact.getName() + " " + contact.getLastName());
		if(contact.getStatus() != null)
			controller.getStatusLabel().setText(contact.getStatus());
		else
			controller.getStatusLabel().setText("");
		
		controller.displayImage(contact.getProfilePic(), false);
	
		MenuItem item = new MenuItem();
		item.setOnAction(controller);
		
		if(!contact.isVisible())
			item.setText("Aggiungi contatto");
		else
			item.setText("Rimuovi contatto");
		
		controller.getPopupMenuButton().getItems().add(item);
		createLabel("Gruppi in comune");
		for(GroupChat commonChat : ChatLogic.getInstance().getCommonGroups(contact.getUsername()))
			appendGroupInfo(commonChat);
	}

	private void createLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: #7289da");
		controller.getScrollPaneVBox().getChildren().add(label);
		VBox.setMargin(label, new Insets(5, 0, 5, 10));
	}

	//Questo metodo mostra tutte le info sul gruppo
	public void showGroupInfo(GroupChat activeChat, boolean iAmOwner, boolean iAmRemoved) {
		controller.getPopupMenuButton().getItems().clear();
		controller.getMyStatusLabel().setVisible(false);
		controller.getStatusLabel().setVisible(false);
		controller.getScrollPaneVBox().getChildren().clear();
		controller.disableChangeImageLabel();
		
		controller.getInfoLabel().setText("Info sul gruppo");
		controller.getTextField1().setText(activeChat.getGroupInfo().getUsername());
		controller.getTextField2().setText("Creato da " + activeChat.getGroupInfo().getOwner() + " il " + activeChat.getGroupInfo().getCreationDate());
		
		if(iAmRemoved) {	
			controller.getStatusLabel().setText("Non sei piu' un partecipante");
			controller.getStatusLabel().setVisible(true);
		}
		
		byte [] proPic = activeChat.getGroupInfo().getProfilePic();
		
		controller.displayImage(proPic, true);
		
		//aggiungo i vari menu item
		MenuItem item = new MenuItem();
		if(iAmOwner) {
			controller.enableChangeImageLabel();
			item.setText("Elimina gruppo");
			item.setOnAction(ev -> {
				String title = "Sei sicuro di voler eliminare il gruppo ?\nL'operazione non ?? reversibile";
				if(ChatDialog.getInstance().showConfirmDialog(title) == ChatDialog.APPROVE_OPTION)
					ChatLogic.getInstance().deleteGroup(activeChat.getGroupInfo().getGroupId());
			});
			
			MenuItem item2 = new MenuItem();
			item2.setText("Aggiungi partecipante");
			item2.setOnAction(ev -> {
				controller.getPopupMenuButton().hide();
				ChatLogic.getInstance().requestAddToGroup(activeChat);
			});
			controller.getPopupMenuButton().getItems().add(item2);
		}
		else {
			item.setText("Abbandona gruppo");
			item.setOnAction(ev -> {
				if(ChatDialog.getInstance().showConfirmDialog("Stai per abbandonare. Sei sicuro ?") == ChatDialog.APPROVE_OPTION) {
					ChatLogic.getInstance().leftGroup(activeChat);
					controller.getPopupMenuButton().hide();
					SceneHandler.getInstance().setChatPane(true);
				}
			});
		}
		
		controller.getPopupMenuButton().getItems().add(item);
		createLabel("Elenco partecipanti");
		
		for(SingleContact contact : activeChat.getListUtenti())
			appendGroupPartecipants(activeChat, contact, iAmOwner);
	}
	
	//Questo metodo aggiunge un partecipante di un gruppo nella sezione dei partecipanti
	private void appendGroupPartecipants(GroupChat chat, SingleContact contact, boolean iAmOwner) {
		HBox container = new HBox();
		container.prefWidthProperty().bind(controller.getScrollPaneVBox().prefWidthProperty());
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
		MenuButton button = new MenuButton();
		MenuItem item = new MenuItem();
		
		if(iAmOwner) {
			Pane spacer = new Pane();
			spacer.setPrefHeight(1);
			container.getChildren().add(spacer);
			HBox.setHgrow(spacer, Priority.ALWAYS);
			
			Image dotBlackImage = new Image(getClass().getResourceAsStream("/application/images/3dot_2.png"), 25, 25, true, true);
			button.setStyle("-fx-background-color: transparent;");
			button.setGraphic(new ImageView(dotBlackImage));
			item.setText("Rimuovi utente");
			
			button.getItems().add(item);
			if(!chat.getGroupInfo().getOwner().equals(contact.getUsername())) 
				container.getChildren().add(button);
			
			HBox.setMargin(button, new Insets(22, 10, 10, 0));
		}
		
		Pane horizontaLine = new Pane();
		horizontaLine.getStyleClass().add("horizontaLine");
		horizontaLine.setPrefHeight(1);
		horizontaLine.setMinHeight(1);
		horizontaLine.prefWidthProperty().bind(controller.getScrollPaneVBox().prefWidthProperty());
		
		if(controller.getScrollPaneVBox().getChildren().size() > 1)
			controller.getScrollPaneVBox().getChildren().add(horizontaLine);
		
		controller.getScrollPaneVBox().getChildren().add(container);
		VBox.setMargin(horizontaLine, new Insets(0, 10, 0, 10));
		
		//?? il bottone rimuovi utente
		item.setOnAction(ev -> {
			ev.consume();
			button.hide();
			ChatLogic.getInstance().requestRimotion(chat, contact);
			controller.getScrollPaneVBox().getChildren().remove(container);
			controller.getScrollPaneVBox().getChildren().remove(horizontaLine);
		});
		
		container.setOnMouseClicked(ev -> {
			ChatLogic.getInstance().setSingleActiveChat(contact.getUsername());
		});
	}
	
	//Questo metodo appende i gruppi in comune nelle info di un contatto
	private void appendGroupInfo(GroupChat commonChat) {
		HBox container = new HBox();
		container.prefWidthProperty().bind(controller.getScrollPaneVBox().prefWidthProperty());
		Circle shape = new Circle();
		shape.setRadius(25);
		Image img;
		if(commonChat.getGroupInfo().getProfilePic() != null)
			img = new Image(new ByteArrayInputStream(commonChat.getGroupInfo().getProfilePic()), 100, 100, true, true);
		else
			img = new Image(getClass().getResource("/application/images/defaultGroup.png").toExternalForm(), 100, 100, true, true);
			
		shape.setFill(new ImagePattern(img));
		container.getChildren().add(shape);
		HBox.setMargin(shape, new Insets(10));
		
		VBox textContainer = new VBox();
		
		Label username = new Label(commonChat.getGroupInfo().getUsername());
		username.getStyleClass().add("contactUsernameLabel");
		textContainer.getChildren().add(username);
		VBox.setMargin(username, new Insets(10, 10, 5, 0));
		
		Label members = new Label(commonChat.getListUtenti().size() + " Membri");
		members.getStyleClass().add("contactStatusLabel");
		textContainer.getChildren().add(members);
		VBox.setMargin(members, new Insets(0, 10, 5, 0));
		 
		container.getChildren().add(textContainer);
		
		
		Pane horizontaLine = new Pane();
		horizontaLine.getStyleClass().add("horizontaLine");
		horizontaLine.setPrefHeight(1);
		horizontaLine.prefWidthProperty().bind(controller.getScrollPaneVBox().prefWidthProperty());
		if(controller.getScrollPaneVBox().getChildren().size() > 1)
			controller.getScrollPaneVBox().getChildren().add(horizontaLine);
		
		controller.getScrollPaneVBox().getChildren().add(container);
		VBox.setMargin(horizontaLine, new Insets(0, 10, 0, 10));
		
		container.setOnMouseClicked(ev -> {
			ChatLogic.getInstance().setGroupChatActive(commonChat.getGroupInfo().getGroupId());
		});
	}
}
