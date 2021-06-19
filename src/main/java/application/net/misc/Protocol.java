package application.net.misc;

public class Protocol {

	public static final String REQUEST_LOGIN = "Login requested";
	public static final String REQUEST_REGISTRATION = "Registration requested";
	public static final String REQUEST_SUCCESSFUL = "Ok";
	
	public static final String MESSAGE_SEND_REQUEST = "Sending message";
	public static final String ONLINE_STATUS_REQUEST = "Request the online status of the user";
	public static final String GROUP_CREATION = "Creating group";
	public static final String GROUP_PARTECIPANT = "Adding new partecipants";
	public static final String GROUP_CREATION_DONE = "Done";
	
	public static final String SEND_FAILED = "Error while sending the message";
	public static final String USER_DISCONNECTED = "User is offline";
	public static final String USER_ALREADY_LOGGED = "User is already online";
	public static final String WRONG_CREDENTIAL = "The combination of username/password is wrong";
	public static final String INVALID_CREDENTIAL = "Username or password are invalid";
	public static final String USER_ALREADY_EXIST = "Select username already exist";
	public static final String COMMUNICATION_ERROR = "Error while handling the request";
	public static final String SERVER_ERROR = "A server error happened";
	public static final String BAD_REQUEST = "A bad request was generated";
	public static final String USER_ONLINE = "User is online";
	public static final String IMAGE_NULL = "Image is null";
	public static final String IMAGE_NOT_NULL = "Image is not null";
	public static final String GENERIC_ERROR = "Error";
	
	public static final String ADDED_NEW_GROUP = "User added to a new Group";
	public static final String MESSAGES_RETRIEVED = "Messages retrieved from server";
	public static final String MESSAGES_LIST = "Lista messaggi";
	public static final String CONTACTS_SEARCH = "Searching contacts";
	
	public static final String CONTACT_INFORMATION_REQUEST = "Requesting info";
	public static final String GROUP_INFORMATION_REQUEST = "Request group info";
	public static final String GROUP_PARTECIPANT_REQUEST = "Request group partecipants";
	public static final String CONTACT_FULL_INFORMATION_REQUEST = "Request full contact info";
	public static final String GROUP_MEMBER_RIMOTION = "Remove user from group";
	public static final String GROUP_MEMBER_ADD = "Adding new member";
	public static final String GROUP_MEMBER_LEFT = "Left the group";
	public static final String GROUP_DELETION = "Delete the group";
	public static final String GROUP_PICTURE_CHANGED = "Picture changed";
	public static final String GROUP_NAME_CHANGED = "Name changed";
	public static final String PASSWORD_CHANGE = "Change the password";
	public static final String STATUS_CHANGE = "Change the status";
	public static final String PHOTO_CHANGE = "Change the photo";
}
