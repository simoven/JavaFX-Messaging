package application.misc;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import application.net.misc.Utilities;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundEffectsHandler {

	private final String VOLUME_ON_KEY = "VOLUME_ON_KEY";
			
	private static SoundEffectsHandler instance = null;
	private Media incomingMessage;
	private Media incomingMessageActiveChat;
	private Media sentMessage;
	private boolean volumeOn;
	private Preferences pref;
	
	
	public static SoundEffectsHandler getInstance() {
		if(instance == null)
			instance = new SoundEffectsHandler();
		
		return instance;
	}
	
	private SoundEffectsHandler() {
		incomingMessage = new Media(getClass().getResource("/application/sound/messageIncoming.mp3").toExternalForm());
		incomingMessageActiveChat = new Media(getClass().getResource("/application/sound/messageIncomingActiveChat.mp3").toExternalForm());
		sentMessage = new Media(getClass().getResource("/application/sound/messageSent.wav").toExternalForm());
		pref = Preferences.userRoot().node(this.getClass().getName());
		volumeOn = pref.getBoolean(VOLUME_ON_KEY, true);
	}
	
	public void playIncomingMessage() {
		if(!volumeOn)
			return;
		
		MediaPlayer player = new MediaPlayer(incomingMessage);
		player.play();
	}
	
	public void playIncomingMessageActiveChat() {
		if(!volumeOn)
			return;
		
		MediaPlayer player = new MediaPlayer(incomingMessageActiveChat);
		player.play();
	}
	
	public void playSentMessage() {
		if(!volumeOn)
			return;
		
		MediaPlayer player = new MediaPlayer(sentMessage);
		player.play();
	}
	
	public void setVolume(boolean enabled) {
		volumeOn = enabled;
		pref.putBoolean(VOLUME_ON_KEY, enabled);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			Utilities.getInstance().logToFile(e.getMessage());
		}
	}
	
	public boolean isVolumeOn() {
		return volumeOn;
	}
}
