package application.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import application.graphics.SceneHandler;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.stage.FileChooser;

public class PicChooser implements EventHandler <MouseEvent> {
	
	private RegisterController controller;
	
	public PicChooser(RegisterController controller) {
		this.controller = controller;
	}
	
	@Override
	public void handle(MouseEvent event) {
		FileChooser chooser = new FileChooser();
		File file = chooser.showOpenDialog(SceneHandler.getInstance().getWindowFrame());
		
		if(file != null) {
			try
			{
				Image img2 = new Image(new FileInputStream(file.getAbsolutePath()), 100, 100, true, true);
				controller.getPicChooserCircle().setFill(new ImagePattern(img2));
				controller.setSelectedImage(file);
			} catch (FileNotFoundException e) {
				//show error
			}
		}
	}
}
