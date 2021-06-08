package application.controller;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import application.net.client.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ChatMainController implements EventHandler <MouseEvent>{

    @FXML
    private BorderPane rightPane;

    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane leftScrollPane;

    @FXML
    private Button newChatButton;
    
    @FXML
    private VBox leftVbox;
    
    @FXML
    private VBox allChatVbox;
    
    @FXML
    private HBox layoutHBox;

    @FXML
    private Button newGroupButton;
    
    @FXML
    private HBox buttonBox;

    @FXML
    private Circle myPropicCircle;
    
    public VBox getLeftVbox() { return leftVbox; }
    
    public VBox getAllChatVbox() { return allChatVbox; }
    
    public Circle getMyPropicCircle() { return myPropicCircle; }
    
    @FXML
    void initialize() {
    	ChatView.getInstance().setChatMainController(this);
    	leftVbox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.2));
    	buttonBox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.2));
    	buttonBox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    }
    
    public void setChatPane() {	
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(SceneHandler.getInstance().getChatPane());
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
    }
    
    @FXML
    void newChat(ActionEvent event) {
    	//Significa che il pannello è già attivo
    	if(layoutHBox.getChildren().get(1).equals(SceneHandler.getInstance().getContactsPane()))
    		return;
    	
    	layoutHBox.getChildren().remove(1);
    	layoutHBox.getChildren().add(SceneHandler.getInstance().getContactsPane());
    	HBox.setHgrow(layoutHBox.getChildren().get(1), Priority.ALWAYS);
    	ChatLogic.getInstance().showContactsChoice();
    }
    
    @Override
    public void handle(MouseEvent event) {
    	//Questo metodo gestisce il click su una chat
    	HBox box = (HBox) event.getSource();
    	//Se c'è il pallino di notifica, lo tolgo
    	if(box.getChildren().get(0) instanceof Circle) {
    		if(((Circle) box.getChildren().get(0)).getRadius() == 5)
    			box.getChildren().remove(0);
    	}
    	
    	VBox vBox = (VBox) box.getChildren().get(1);
    	Label username = (Label) vBox.getChildren().get(0);
    	ChatLogic.getInstance().setSingleActiveChat(username.getText());
    	Client.getInstance().requestOnlineStatus(username.getText());
    }
}
