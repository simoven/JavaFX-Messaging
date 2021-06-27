package application.graphics;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiImageCache;
import com.pavlobu.emojitextflow.EmojiParser;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class EmojiLoader {

	//non posso prendere tutte le emoji, quindi ne prendo solo alcune
	private String [] allEmojis = {":grinning:", ":grin:", ":joy:", ":rofl:", ":smile:", ":sweat_smile:", ":innocent:", ":no_mouth:", ":tired_face:", ":nerd:",
							":thumbsup:", ":thumbsdown:", ":relaxed:", ":sunglasses:", ":disappointed_relieved:", ":heart_eyes:", ":kissing_heart:", ":thinking:", ":confused:",
							":see_no_evil:", ":hear_no_evil:", ":speak_no_evil:", ":blush:", ":wink:", ":smirk:", ":yum:", ":hugging:", ":no_mouth:", ":grimacing:",
							":eyes:", ":scream:", ":rolling_eyes:", ":smiling_imp:", ":kissing_heart:", ":astonished:", ":disappointed:", ":cry:", ":sob:", 
							":rage:", ":angry:", ":clown:", ":ghost:", ":robot:", ":poop:", ":smile_cat:", ":boy:", ":girl:", ":man:", ":woman:", ":older_man:", 
							":older_woman:", ":baby:", ":cop:", ":spy:", ":santa:", ":pregnant_woman:", ":call_me:",
							":face_palm:", ":shrug:", ":walking:", ":runner:", ":kiss:", ":handshake:", ":muscle:", ":punch:", ":point_up:", ":middle_finger:",
							":fingers_crossed:", ":raised_hand:", ":ok_hand:", 
							":dog:", ":cat:", ":wolf:", ":horse:", ":pig:", ":duck:", ":bird:", ":bear:", ":cow:", ":unicorn:", ":tiger:", ":lion_face:", ":leopard:", ":wolf:", 
							":poodle:", ":gorilla:", ":monkey:", ":rooster:", ":panda_face:",
							":watermelon:", ":banana:", ":apple:", ":strawberry:", ":pineapple:", ":tomato:", ":peach:", ":hamburger:", ":pizza:", ":doughnut:", ":ice_cream:",
							":iphone:", ":computer:", ":desktop:", ":notebook:", ":satellite:", ":book:", ":moneybag:", ":dollar:", ":euro:", ":camera:", ":tv:", ":dvd:", 
							":house:", ":headphones:", ":bed:", ":office:", ":full_moon:",":sunrise:", ":star:", ":flags:", ":gift:", ":gun:", ":shield:",
							":train:", ":race_car:", ":metro:", ":bus:", ":ambulance:", ":police_car:", ":ship:", ":airplane:", ":rocket:",
							":soccer:", ":volleyball:", ":football:", ":basketball:", ":tennis:", ":8ball:", ":bowling:", 
							":heart:", ":fire:", ":top:", ":radioactive:", ":no_entry:", ":warning:", ":rainbow:", ":flag_it:", ":flag_eu:", ":flag_us:"};
	
	
	private static EmojiLoader instance = null;
	
	private EmojiLoader() {}
	
	public static EmojiLoader getInstance() {
		if(instance == null)
			instance = new EmojiLoader();
		
		return instance;
	}
	
	public HBox getEmojiHBox() {
		HBox container = new HBox();
		FlowPane pane = new FlowPane();
		pane.setHgap(5);
		pane.setVgap(4);
		ScrollPane scrollPane = new ScrollPane(pane);
		pane.setStyle("-fx-background-color: white");
		scrollPane.prefWidthProperty().bind(container.prefWidthProperty());
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setFitToWidth(true);
		for(String emoji : allEmojis) {
    		Emoji em = EmojiParser.getInstance().getEmoji(emoji); 
    		if(em == null) {
    			System.out.println(emoji);
    			continue;
    		}
    		
    		Image image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(em.getHex()));
    		ImageView v = new ImageView(image);
    		v.setFitWidth(30);
    		v.setFitHeight(30);
    		pane.getChildren().add(v);
    		
    		v.setOnMouseClicked(ev -> {
    			ChatView.getInstance().getChatPaneController().getMessageTextArea().appendText(emoji + " ");
    		});
    	}
		
		container.getChildren().add(scrollPane);
		HBox.setHgrow(pane, Priority.ALWAYS);
		return container;
	}
	
	private String getEmojiImagePath(String hexStr) throws NullPointerException {
		return getClass().getResource("/application/emoji_images/" + hexStr + ".png").toExternalForm();
	}
}
