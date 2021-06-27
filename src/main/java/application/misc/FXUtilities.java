package application.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
	
	public static void saveImage(byte [] img) {
		System.out.println(img [0]);
		FileChooser chooser = new FileChooser();
		File toSave = chooser.showSaveDialog(SceneHandler.getInstance().getWindowFrame());
		
		if(toSave == null)
			return;
		
		try {
			String fileName = toSave.getName();
			String extension = "";
			//Se il primo byte è -1, allora è un jpeg
			if (img [0] == -1 && !fileName.endsWith(".jpeg"))
				extension += ".jpeg";
			
			if (img [0] == -119 && !fileName.endsWith(".png"))
				extension += ".png";
			
			toSave = new File(toSave.getAbsoluteFile() + extension);
			toSave.createNewFile();
			OutputStream stream = new FileOutputStream(toSave);
			stream.write(img);
			stream.flush();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
