package application.net.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;

import javafx.scene.image.Image;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1714223638602693321L;
	
	private String name;
	private String lastName;
	private String username;
	private String password;
	private Image proPic;
	private File proPicAsFile;
	
	public User(String username, String password, String name, String lastName) {
		this.name = name;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.proPic = null;
		this.proPicAsFile = null;
	}
	
	public void setPropicFile(byte[] arr) {
		proPic = new Image(new ByteArrayInputStream(arr));
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getName() {
		return name;
	}
	
	public Image getProPic() {
		return proPic;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public File getProPicAsFile() {
		return proPicAsFile;
	}
}
