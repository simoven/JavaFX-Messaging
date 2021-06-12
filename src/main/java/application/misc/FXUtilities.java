package application.misc;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import application.graphics.SceneHandler;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FXUtilities {

	public static File chooseImage() {
		FileChooser chooser = new FileChooser();
    	List <String> extensions = new LinkedList<>();
    	extensions.add("*.png");
    	extensions.add("*.jpeg");
    	extensions.add("*.jpg");
    	chooser.getExtensionFilters().add(new ExtensionFilter("Immagini", extensions));
		File file = chooser.showOpenDialog(SceneHandler.getInstance().getWindowFrame());
		
		return file;
	}
}
