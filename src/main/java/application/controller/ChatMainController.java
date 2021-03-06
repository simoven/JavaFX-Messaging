package application.controller;

import application.graphics.ChatView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ChatMainController implements EventHandler <MouseEvent>{

    @FXML
    private StackPane rightPane;

    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane leftScrollPane;
    
    @FXML
    private HBox topHbox;

    @FXML
    private Circle newChatButton;
    
    @FXML
    private VBox leftVbox;
    
    @FXML
    private VBox allChatVbox;
    
    @FXML
    private HBox layoutHBox;
    
    @FXML
    private HBox buttonBox;

    @FXML
    private Circle myPropicCircle;
    
    private Image buttonDefault;
    
    private Image buttonPressed;
    
    public VBox getLeftVbox() { return leftVbox; }
    
    public VBox getAllChatVbox() { return allChatVbox; }
    
    public Circle getMyPropicCircle() { return myPropicCircle; }
    
    @FXML
    void initialize() {
    	ChatView.getInstance().setChatMainController(this);
    	SceneHandler.getInstance().setMainStackPane(rightPane);
    	leftVbox.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.2));
    	topHbox.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	leftScrollPane.prefHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.8));
    	buttonBox.maxHeightProperty().bind(SceneHandler.getInstance().getWindowFrame().heightProperty().multiply(0.05));
    	buttonBox.getStyleClass().add("transparent");
    	searchField.getStyleClass().add("searchTextField");
    	buttonDefault = new Image(getClass().getResourceAsStream("/application/images/chatIconDefault.png"), 100, 100, true, true);
    	buttonPressed = new Image(getClass().getResourceAsStream("/application/images/chatIconPressed.png"), 100, 100, true, true);
    	newChatButton.setFill(new ImagePattern(buttonDefault));
    }
    
    @FXML
    void buttonReleased(MouseEvent event) {
    	newChatButton.setFill(new ImagePattern(buttonDefault));
    }
    
    @FXML
    void buttonClicked(MouseEvent event) {
    	newChatButton.setFill(new ImagePattern(buttonPressed));
    }
    
    @FXML
    void newChat(MouseEvent event) {
    	ChatLogic.getInstance().showContactsChoice();
    }
    
    @Override
    //Questo metodo gestisce il click su una chat a sinistra
    public void handle(MouseEvent event) {
    	SceneHandler.getInstance().checkImageSceneActive();
    	HBox box = (HBox) event.getSource();
    	//Se c'?? il pallino di notifica, lo tolgo
    	if(box.getChildren().get(0) instanceof Circle) {
    		if(((Circle) box.getChildren().get(0)).getRadius() == 5)
    			box.getChildren().remove(0);
    	}
    	
    	VBox vBox = (VBox) box.getChildren().get(1);
    	Label username = (Label) vBox.getChildren().get(0);
    	Label groupId = (Label) vBox.getChildren().get(2);
    	//Significa che ?? una chat di gruppo
    	if(!groupId.getText().equals("-1"))
    		ChatLogic.getInstance().setGroupChatActive(Integer.parseInt(groupId.getText()));
    	else 
    		ChatLogic.getInstance().setSingleActiveChat(username.getText());
    }
    
    @FXML
    //Ho premuto dei tatsti nella barra di ricerca
    void keySearchTyped(KeyEvent event) {
    	if(searchField.getText().isBlank()) {
    		ChatLogic.getInstance().displayAllChat();
    		return;
    	}
    	
    	allChatVbox.getChildren().clear();
    	ChatLogic.getInstance().showChatFiltered(searchField.getText());
    }
    
    @FXML
    //Ho cliccato sulla mia foto
    void displayMyInformation(MouseEvent event) {
    	ChatLogic.getInstance().displayMyInformation();
    }
}
