package application.net.misc;

import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1714223638602693321L;

	protected String username;
	protected String status;
	protected String gpOwner;
	protected String creationDate;
	protected byte [] proPic;
	
	public User(String username) {
		this.username = username;
		this.proPic = null;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getCreationDate() {
		return creationDate;
	}
	
	public void setPropicFile(byte[] arr) {
		proPic = arr;
	}
	
	public void setGpOwner(String gpOwner) {
		this.gpOwner = gpOwner;
	}
	
	public String getGpOwner() {
		return gpOwner;
	}
	
	public byte[] getProPic() {
		return proPic;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
}
