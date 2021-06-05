package application.net.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import application.logic.chat.Chat;
import application.logic.chat.GroupChat;
import application.logic.chat.SingleChat;
import application.logic.contacts.Contact;
import application.logic.contacts.GroupContact;
import application.logic.contacts.SingleContact;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.misc.User;
import application.net.misc.Utilities;

//Il database locale è quasi identico a quello sul server 
/*step dopo il login :
	controllo se esiste il mio db
		se esiste inizio a recuperare i contatti
		poi recupero i messaggi con chat singole
		e poi recupero le informazioni e i massi dei gruppi
*/
public class LocalDatabaseHandler {

	private static LocalDatabaseHandler instance = null;
	private String myUsername;
	private Connection dbConnection;
	
	private LocalDatabaseHandler() {}
	
	public static LocalDatabaseHandler getInstance() {
		if(instance == null)
			instance = new LocalDatabaseHandler();
		
		return instance;
	}
	
	public void setUsername(String username) {
		this.myUsername = username;
	}
	
	public void createLocalDB() {
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:" + myUsername + ".db");
			
			if(dbConnection != null && !dbConnection.isClosed()) {
				createUtente();
				createGruppo();
				createMessaggi();
				createMessaggioDiGruppo();
				createUtenteInGruppo();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkUserExist(String username) throws SQLException {
		String query = "SELECT * FROM Utente WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		ResultSet rs = stm.executeQuery();
		boolean result = rs.next();
		stm.close();
	
		return result;
	}
	
	public boolean registerUser(User utente) throws SQLException {
		if(checkUserExist(utente.getUsername()))
			return false;
		
		String query = "INSERT INTO Utente VALUES(?,?,?,?);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, utente.getUsername());
		stm.setString(2, utente.getName());
		stm.setString(3, utente.getLastName());
		stm.setBytes(4, utente.getProPic());
		
		int res = stm.executeUpdate();
		stm.close();
		
		return res != 0;
	}
	
	public void addMessage(ChatMessage msg) throws SQLException {
		String dateTime = Utilities.getCurrentISODate();
		String query;
		if(msg.isAGroupMessage())
			query = "INSERT INTO MessaggioDiGruppo Values(null, ?, ?, ?, ?, ?, ?);";
		else 
			query = "INSERT INTO Messaggi Values(null, ?, ?, ?, ?, ?);";
		PreparedStatement stmt = dbConnection.prepareStatement(query);
		stmt.setString(1, msg.getSender());
		stmt.setString(2, msg.getReceiver());
		stmt.setString(3, msg.getText());
		stmt.setBytes(4, msg.getImage());
		stmt.setString(5, dateTime);
		if(msg.isAGroupMessage())
			stmt.setInt(5, msg.getGroupId());
		stmt.executeUpdate();
		stmt.close();
	}
	
	public Vector <Message> retrieveMessageFromChat(String dest) throws SQLException {
		Vector <Message> msgList = new Vector <Message>();
		String query = "SELECT * FROM Messaggi WHERE Sender=? OR Receiver=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, dest);
		stm.setString(2, dest);
		ResultSet rs = stm.executeQuery();
		
		while(rs.next()) {
			ChatMessage msg = new ChatMessage(rs.getString("Sender"), rs.getString("Receiver"));
			msg.setText(rs.getString("Message_text"));
			msg.setImage(rs.getBytes("Image"));
			msg.setSentDate(Utilities.getDateFromString(rs.getString("Date")));
			msg.setSentHour(Utilities.getHourFromString(rs.getString("Date")));
			
			msgList.add(msg);
		}
		
		rs.close();
		stm.close();
		
		return msgList;
	}
	
	public Vector <Chat> retrieveSingleChatInfo(Vector <Contact> contactList) throws SQLException {
		Vector <Chat> singleChatVector = new Vector <Chat> ();
		for(Contact c : contactList) {
			if(!(c instanceof SingleContact))
					continue;
			
			if(c.getUsername().equals(myUsername))
				continue;
			
			SingleContact sinContact = (SingleContact) c;
			SingleChat chat = new SingleChat(sinContact);
			chat.setListMessaggi(retrieveMessageFromChat(sinContact.getUsername()));
			
			if(!chat.getListMessaggi().isEmpty())
				singleChatVector.add(chat);
		}
		
		return singleChatVector;
	}
	
	public Vector <Message> retrieveMessageFromGroupChat(int groupId) throws SQLException {
		Vector <Message> msgList = new Vector <Message>();
		String query = "SELECT * FROM MessaggioDiGruppo;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		ResultSet rs = stm.executeQuery();
		
		while(rs.next()) {
			ChatMessage msg = new ChatMessage(rs.getString("Sender"), rs.getString("Receiver"));
			msg.setText(rs.getString("Message_text"));
			msg.setImage(rs.getBytes("Image"));
			msg.setGroupMessage(true);
			msg.setGroupId(rs.getInt("Group_id"));
			msg.setSentDate(Utilities.getDateFromString(rs.getString("Date")));
			msg.setSentHour(Utilities.getHourFromString(rs.getString("Date")));
			
			msgList.add(msg);
		}
		
		rs.close();
		stm.close();
		
		return msgList;
	}
	
	public Vector <String> retriveGroupPartecipants(int groupId) throws SQLException {
		Vector <String> tmp = new Vector <String>();
		String query = "SELECT User_utente FROM UtenteInGruppo WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setInt(1, groupId);
		
		ResultSet rs = stm.executeQuery();
		while(rs.next()) {
			tmp.add(rs.getString("User_utente"));
		}
		
		return tmp;
	}
	
	public Vector <Chat> getGroupChats(Vector <Contact> contactList) throws SQLException {
		Vector <Chat> groupChatArr = new Vector <Chat>();
		//Per ogni contatto gruppo salvato, recupero la lista dei membri e dei messaggi e la associo ai miei contatti
		for(Contact contact : contactList) {
			if(!(contact instanceof GroupContact))
				continue;
			
			GroupContact gpContact = (GroupContact) contact;
			GroupChat chat = new GroupChat(gpContact);
			Vector <SingleContact> listContatti = new Vector <SingleContact>();
			Vector <String> listUser = retriveGroupPartecipants(gpContact.getGroupId());
			//Per ogni stringa username, cerco il contatto a cui è associato
			for(String user : listUser) {
				for(Contact c : contactList) {
					if(c instanceof SingleContact && c.getUsername().equals(user)) {
						listContatti.add((SingleContact) c);
						break;
					}
				}
			}
			
			chat.setListUtenti(listContatti);
			chat.setListMessaggi(retrieveMessageFromGroupChat(gpContact.getGroupId()));
			groupChatArr.add(chat);
		}
		
		return groupChatArr;
	}
	
	public Vector <Contact> retrieveContacts() throws SQLException {
		Vector <Contact> listContatti = new Vector <Contact>();
		String query = "SELECT * FROM Utente";
		Statement stm = dbConnection.createStatement();
		ResultSet rs = stm.executeQuery(query);
		while(rs.next()) {
			SingleContact contact = new SingleContact(rs.getString("Username"));
			contact.setProfilePic(rs.getBytes("Img_profilo"));
			contact.setStatus(rs.getString("Status"));
			
			listContatti.add(contact);
		}
		
		rs.close();
		stm.close();
		
		query = "SELECT * FROM Gruppo";
		stm = dbConnection.createStatement();
		rs = stm.executeQuery(query);
		while(rs.next()) {
			GroupContact contact = new GroupContact(rs.getString("Nome"), rs.getInt("Id_gruppo"));
			contact.setProfilePic(rs.getBytes("ProPic"));
			
			listContatti.add(contact);
		}
		
		rs.close();
		stm.close();
		
		return listContatti;
	}
	
	private void createUtente() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS \"Utente\" (\n"
				+ "	\"Username\"	TEXT NOT NULL,\n"
				+ "	\"Nome\"	TEXT NOT NULL,\n"
				+ "	\"Cognome\"	TEXT NOT NULL,\n"
				+ "	\"Img_profilo\"	BLOB,\n"
				+ "	\"Status\"	TEXT,\n"
				+ "	PRIMARY KEY(\"Username\")\n"
				+ ")";
		Statement stm = dbConnection.createStatement();
		stm.executeUpdate(query);
		stm.close();
	}
	
	private void createGruppo() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS \"Gruppo\" (\n"
				+ "	\"Id_gruppo\"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
				+ "	\"Nome\"	TEXT NOT NULL,\n"
				+ "	\"ProPic\"	BLOB\n"
				+ ")";
		Statement stm = dbConnection.createStatement();
		stm.executeUpdate(query);
		stm.close();
	}
	
	private void createMessaggi() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS \"Messaggi\" (\n"
				+ "	\"Id_messaggio\"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
				+ "	\"Sender\"	TEXT NOT NULL,\n"
				+ "	\"Receiver\"	TEXT NOT NULL,\n"
				+ "	\"Message_text\"	TEXT,\n"
				+ "	\"Image\"	BLOB,\n"
				+ "	\"Date\"	TEXT NOT NULL,\n"
				+ "	FOREIGN KEY(\"Sender\") REFERENCES \"Utente\"(\"Username\"),\n"
				+ "	FOREIGN KEY(\"Receiver\") REFERENCES \"Utente\"(\"Username\")\n"
				+ ")";
		Statement stm = dbConnection.createStatement();
		stm.executeUpdate(query);
		stm.close();
	}
	
	private void createMessaggioDiGruppo() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS \"MessaggioDiGruppo\" (\n"
				+ "	\"Id_messaggio\"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
				+ "	\"Sender\"	TEXT NOT NULL,\n"
				+ "	\"Receiver\"	TEXT NOT NULL,\n"
				+ "	\"MessageText\"	TEXT,\n"
				+ "	\"Image\"	BLOB,\n"
				+ "	\"Date\"	TEXT NOT NULL,\n"
				+ "	\"Group_id\"	INTEGER NOT NULL,\n"
				+ "	FOREIGN KEY(\"Sender\") REFERENCES \"Utente\"(\"Username\"),\n"
				+ "	FOREIGN KEY(\"Receiver\") REFERENCES \"Utente\"(\"Username\")\n"
				+ ")";
		Statement stm = dbConnection.createStatement();
		stm.executeUpdate(query);
		stm.close();
	}
	
	private void createUtenteInGruppo() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS \"UtenteInGruppo\" (\n"
				+ "	\"User_utente\"	TEXT NOT NULL,\n"
				+ "	\"Id_gruppo\"	INTEGER NOT NULL,\n"
				+ "	\"Id_aggiunta\"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
				+ "	FOREIGN KEY(\"Id_gruppo\") REFERENCES \"Gruppo\"(\"Id_gruppo\")\n"
				+ ")";
		Statement stm = dbConnection.createStatement();
		stm.executeUpdate(query);
		stm.close();
	}
	
}