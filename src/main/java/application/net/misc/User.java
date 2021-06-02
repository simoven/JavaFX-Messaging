package application.net.misc;

import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1714223638602693321L;
	
	private String name;
	private String lastName;
	private String username;
	private String password;
	private byte [] proPic;
	
	public User(String username, String password, String name, String lastName) {
		this.name = name;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.proPic = null;
	}
	
	public void setPropicFile(byte[] arr) {
		proPic = arr;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] getProPic() {
		return proPic;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
