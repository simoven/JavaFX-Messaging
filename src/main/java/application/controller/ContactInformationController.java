package application.controller;

import application.graphics.ContactInfoView;
import application.graphics.SceneHandler;
import application.logic.ChatLogic;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ContactInformationController implements EventHandler <ActionEvent> {

    @FXML
    private HBox topHbox;

    @FXML
    private ScrollPane bottomScrollPane;

    @FXML
    private Label infoLabel;

    @FXML
    private Circle propicCircle;

    @FXML
    private TextField textField2;

    @FXML
    private TextField textField1;

    @FXML
    private VBox fieldVBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Circle backButton;

    @FXML
    private VBox scrollPaneVBox;
    
    @FXML
    private MenuButton popupMenuButton;
    
    @FXML
    private Label myStatusLabel;
    
    private Image dotWhiteImage;
    
    private Image dotBlackImage;
    
    @FXML
    private BorderPane root;
    
    public Label getInfoLabel() {
		return infoLabel;
	}
    
    public TextField getTextField1() {
		return textField1;
	}
    
    public TextField getTextField2() {
		return textField2;
	}
    
   public Label getStatusLabel() {
	return statusLabel; }
   
    public Circle getPropicCircle() {
		return propicCircle;
	}
    
    public VBox getScrollPaneVBox() {
		return scrollPaneVBox;
	}
    
    public MenuButton getPopupMenuButton() { return popupMenuButton; }
    
    public Label getMyStatusLabel() { return myStatusLabel; }
    
    @FXML 
    void initialize() {
    	root.prefWidthProperty().bind(SceneHandler.getInstance().getWindowFrame().widthProperty().multiply(0.8));
    	dotBlackImage = new Image(getClass().getResourceAsStream("/application/images/3dot_2.png"), 30, 30, true, true);
    	dotWhiteImage = new Image(getClass().getResourceAsStream("/application/images/3dot_white.png"), 30, 30, true, true);
    	backButton.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/application/images/backArrow.png"), 100, 100, true, true)));
    	popupMenuButton.setGraphic(new ImageView(dotWhiteImage));
    	ContactInfoView.getInstance().setController(this);
    	textField1.setEditable(false);
    	textField2.setEditable(false);
    }
    
    @FXML
    void backButtonPressed(MouseEvent event) {
    	SceneHandler.getInstance().setChatPane();
    }

	@Override
	public void handle(ActionEvent event) {
		String choice = ((MenuItem) event.getSource()).getText();
		
		if(choice.equals("Aggiungi contatto")) {
			ChatLogic.getInstance().setContactVisibility(textField2.getText().substring(1), true);
			((MenuItem) event.getSource()).setText("Rimuovi contatto");
		}
		else if(choice.equals("Rimuovi contatto")) {
			ChatLogic.getInstance().setContactVisibility(textField2.getText().substring(1), false);
			((MenuItem) event.getSource()).setText("Aggiungi contatto");
		}
	}
}