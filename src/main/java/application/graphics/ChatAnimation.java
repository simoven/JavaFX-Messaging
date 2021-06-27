package application.graphics;

import application.logic.ChatLogic;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ChatAnimation {

	//Faccio partire il pannello dalla sua heigth e fa una traslazione height -> 0, quindi verso sopra
	static void doSlideInFromBottom(Pane paneToRemove, Pane paneToAdd, StackPane mainStackPane) {
	    if(mainStackPane.getChildren().contains(paneToAdd))
	    	mainStackPane.getChildren().remove(paneToAdd);
	    
	    mainStackPane.getChildren().add(paneToAdd);
	  
		paneToAdd.translateYProperty().set(paneToAdd.getHeight());
	    KeyValue keyValue = new KeyValue(paneToAdd.translateYProperty(), 0, Interpolator.EASE_IN);
	    KeyFrame keyFrame = new KeyFrame(Duration.millis(400), keyValue);
	    Timeline timeline =  new Timeline(keyFrame);
	    timeline.setOnFinished(evt -> {
	    	if(paneToRemove != null)
	    		mainStackPane.getChildren().remove(paneToRemove);
	    });
	    timeline.play();
	}
	
	static void doSlideInFromTop(Pane paneToRemove, Pane paneToAdd, StackPane mainStackPane) {
	    if(mainStackPane.getChildren().contains(paneToAdd))
	    	mainStackPane.getChildren().remove(paneToAdd);
	    
	    mainStackPane.getChildren().add(paneToAdd);
		
		paneToAdd.translateYProperty().set(-1 * SceneHandler.getInstance().getWindowFrame().getHeight());
	    KeyValue keyValue = new KeyValue(paneToAdd.translateYProperty(), 0, Interpolator.EASE_IN);
	    KeyFrame keyFrame = new KeyFrame(Duration.millis(400), keyValue);
	    Timeline timeline =  new Timeline(keyFrame);
	    timeline.setOnFinished(evt -> {
	    	if(paneToRemove != null)
	    		mainStackPane.getChildren().remove(paneToRemove);
	    	
	    	//quando l'animazione finisce, notifico chatLogic, che provvederà a fare delle richieste
	    	if(paneToAdd.equals(SceneHandler.getInstance().getChatPane()))
	    		ChatLogic.getInstance().updateStuff();
	    });
	    timeline.play();
	}
	
	//In questa animazione, per evitare che il pannello mentre arriva da sinistra si sovrappone alle chat creo una clip, in modo da ridurre la parte visibile del pannello
	static void doSlideInFromLeft(Pane paneToRemove, Pane paneToAdd, StackPane mainStackPane) {	
		if(mainStackPane.getChildren().contains(paneToAdd))
	    	mainStackPane.getChildren().remove(paneToAdd);
		
		mainStackPane.getChildren().add(paneToAdd);
		
		Rectangle clip = new Rectangle();
		clip.setHeight(paneToAdd.getHeight());
		clip.setWidth(0);
		paneToAdd.setClip(clip);
		
		//Per evitare che la clip si sposti a destra insieme al pannello, assegno al translate x della clip il valori opposto
		//Cosi mentre il pannello scorre verso destra, la clip rimane ferma
		clip.translateXProperty().set(paneToAdd.getWidth());
		paneToAdd.translateXProperty().set(-1 * paneToAdd.getWidth());
		
		//Il pannello parte da -width, mentre la clip parte da 0 del pannello + width, quindi alla fine dopo le trasformazioni partirà sempre sul bordo sinistro dello stackpane
		KeyValue kv1 = new KeyValue(clip.widthProperty(), paneToAdd.getWidth());
		KeyValue kv2 = new KeyValue(clip.translateXProperty(), 0);
		KeyValue kv3 = new KeyValue(paneToAdd.translateXProperty(), 0);
		KeyFrame frame = new KeyFrame(Duration.millis(300), kv1, kv2, kv3);
		Timeline timeline = new Timeline(frame);
		timeline.setOnFinished(ev -> {
			paneToAdd.setClip(null);
			
			if(paneToRemove != null)
				mainStackPane.getChildren().remove(paneToRemove);
		});
		timeline.play();
	}
}
