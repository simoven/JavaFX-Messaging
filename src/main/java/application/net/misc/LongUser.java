package application.net.misc;

public class LongUser extends User {
	
	private static final long serialVersionUID = 376711494554622169L;
	
	private String name;
	private String lastName;
	private String password;
	
	public LongUser(String username, String name, String lastName) {
		super(username);
		
		this.name = name;
		this.lastName = lastName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLastName() {
		return lastName;
	}

}
