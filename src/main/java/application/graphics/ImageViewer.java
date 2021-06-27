package application.graphics;

import java.io.File;

import application.controller.ChatPaneController;
import application.controller.ContactInformationController;
import application.controller.CreateGroupController;
import application.controller.ImageViewController;
import application.controller.MyProfileController;
import application.net.misc.Utilities;
import javafx.scene.layout.StackPane;

//Questa classe si occupa di gestire la visualizzazione di un'immagine a schermo intero in : sola visualizzazione - scelta foto
//Quando una classe richiede di visualizzare una foto salvo l'oggetto che ha richiesto l'azione, così se clicco su "seleziona" per selezionare la foto
//so quale classe ha chiamato il pannello e quindi quale azione fare
public class ImageViewer {

	private static ImageViewer instance = null;
	private ImageViewController controller = null;
	private StackPane lastPaneOpen = null;
	private Object classWhoRequestedAction = null;
	private File actualImg = null;
	
	private ImageViewer() {}
	
	public static ImageViewer getInstance() {
		if(instance == null)
			instance = new ImageViewer();
		
		return instance;
	}
	
	public void setController(ImageViewController controller) {
		this.controller = controller;
	}
	
	private void closeLastPanel() {
		//Salvo l'ultimo stackPane dove ho aperto il pannello così, prima di aprirne un altro, quello vecchio lo chiudo
		if(lastPaneOpen != null) {
			int idx = lastPaneOpen.getChildren().indexOf(controller.getThisRoot());
			if(idx != -1)
				lastPaneOpen.getChildren().remove(idx);
		}
	}
	
	//Questo metodo mostra il pannello di visualizzazione dell'immagine
	public void displayImageInPane(StackPane root, byte [] image) {
		closeLastPanel();
		lastPaneOpen = root;
		controller.updateSelectButton(false);
		controller.updateConstraints(root);
		controller.showImage(image);
		root.getChildren().add(controller.getThisRoot());
	}
	
	//Questo metodo mostra il pannello con il bottone per selezionare l'immagine
	public void displayImageChooser(StackPane root, Object requester, File image) {
		closeLastPanel();
		lastPaneOpen = root;
		classWhoRequestedAction = requester;
		actualImg = image;
		controller.updateSelectButton(true);
		controller.updateConstraints(root);
		controller.showImage(Utilities.getByteArrFromFile(image));
		root.getChildren().add(controller.getThisRoot());
	}
	
	//se clicco "seleziona" sul pannello qui, in base a chi ha richiesto il pannello, svolgo l'azione
	public void confirmFoto() {
		//qua significa che sto inviando una foto
		if(classWhoRequestedAction instanceof ChatPaneController)
			((ChatPaneController) classWhoRequestedAction).confirmAttachImage(actualImg);
		//qua significa che sto modificando la mia foto profilo
		else if(classWhoRequestedAction instanceof MyProfileController)
			((MyProfileController) classWhoRequestedAction).updatePhoto(actualImg);
		//qua devo cambiare la foto di gruppo
		else if(classWhoRequestedAction instanceof ContactInformationController)
			((ContactInformationController) classWhoRequestedAction).changeGroupPhoto(actualImg);
		//qua sto scegliendo la foto per la creazione di un gruppo
		else if(classWhoRequestedAction instanceof CreateGroupController)
			((CreateGroupController) classWhoRequestedAction).confirmImage(actualImg);
	}
}
