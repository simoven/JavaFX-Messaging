package application;

import com.pavlobu.emojitextflow.EmojiTextFlowParameters;

import application.graphics.SceneHandler;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainApplication extends Application {
	
	private static final double EMOJI_SCALE_FACTOR = 1D;
	private static final double textSize = 18;
	public static EmojiTextFlowParameters emojiTextFlowParameters;
	

	@Override
	public void start(Stage primaryStage) throws Exception {
        emojiTextFlowParameters = new EmojiTextFlowParameters();
        emojiTextFlowParameters.setEmojiScaleFactor(EMOJI_SCALE_FACTOR);
        emojiTextFlowParameters.setTextAlignment(TextAlignment.LEFT);
        emojiTextFlowParameters.setFont(Font.font("Arial", FontWeight.NORMAL, textSize));
        emojiTextFlowParameters.setTextColor(Color.WHITE);
	    
		SceneHandler.getInstance().init(primaryStage);
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
