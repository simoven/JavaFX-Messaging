package application.graphics;

import java.io.ByteArrayInputStream;

import application.controller.MyProfileController;
import application.logic.contacts.SingleContact;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class MyProfileView {

	private static MyProfileView instance;
	private MyProfileController controller;
	
	private MyProfileView() {}
	
	public void setController(MyProfileController controller) {
		this.controller = controller; }
	
	public static MyProfileView getInstance() {
		if(instance == null)
			instance = new MyProfileView();
		
		return instance;
	}
	
	public void displayMyInformation(SingleContact myContact, String fullName) {
		if(myContact.getProfilePic() == null)
			controller.getPropicCircle().setFill(new ImagePattern(controller.getDefaultImage()));
		else
			controller.getPropicCircle().setFill(new ImagePattern(new Image(new ByteArrayInputStream(myContact.getProfilePic()), 100, 100, true, true)));
		
		controller.getNameLabel().setText(fullName);
		controller.getUsernameLabel().setText("@" + myContact.getUsername());
		
		if(myContact.getStatus() != null) {
			controller.getStatusTextField().setText(myContact.getStatus());
			controller.getRowCountLabel().setText(myContact.getStatus().length() + "/90");
		}
	
	}
}
