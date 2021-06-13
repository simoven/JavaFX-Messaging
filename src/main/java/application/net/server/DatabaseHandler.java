package application.net.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.springframework.security.crypto.bcrypt.BCrypt;

import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.misc.LongUser;
import application.net.misc.User;
import application.net.misc.Utilities;

public class DatabaseHandler {
	
	private static DatabaseHandler instance;
	
	private Connection dbConnection;
	
	private DatabaseHandler() {}
	
	public static DatabaseHandler getInstance() {
		if(instance == null)
			instance = new DatabaseHandler();
		
		return instance;
	}
	
	public boolean tryConnection() {
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:ServerDB.db");
		
			if(!dbConnection.isClosed() && dbConnection != null)
				return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public synchronized User checkUserLogin(String username, String password) throws SQLException {
		LongUser utente = null;
		if(!checkUserExist(username))
			return null;
		
		String query = "SELECT * FROM Utente WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		
		ResultSet rs = stm.executeQuery();
		
		if(rs.next()) {
			String pass = rs.getString("password");
			if(BCrypt.checkpw(password, pass)) {
				utente = new LongUser(username, rs.getString("Nome"), rs.getString("Cognome"));
				utente.setPassword("");
				utente.setPropicFile(rs.getBytes("Img_profilo"));
			}
		}
		
		rs.close();
		
		return utente;
	}
	
	public synchronized boolean registerUser(LongUser utente) throws SQLException {
		if(checkUserExist(utente.getUsername()))
			return false;
		
		String query = "INSERT INTO Utente VALUES(?,?,?,?,?,null, null);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, utente.getUsername());
		stm.setString(2, BCrypt.hashpw(utente.getPassword(), BCrypt.gensalt(12)));
		stm.setString(3, utente.getName());
		stm.setString(4, utente.getLastName());
		stm.setBytes(5, utente.getProPic());
		
		int res = stm.executeUpdate();
		stm.close();
		
		return res != 0;
	}
	
	public synchronized boolean checkUserExist(String username) throws SQLException {
		String query = "SELECT * FROM Utente WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		ResultSet rs = stm.executeQuery();
		boolean result = rs.next();
		stm.close();
	
		return result;
	}
	
	public synchronized void addPendingMessage(ChatMessage msg, String receiver) throws SQLException {
		String query;
		if(msg.isAGroupMessage())
			query = "INSERT INTO MessaggioDiGruppo Values(null, ?, ?, ?, ?, ?, ?);";
		else 
			query = "INSERT INTO Messaggi Values(null, ?, ?, ?, ?, ?);";
		PreparedStatement stmt = dbConnection.prepareStatement(query);
		stmt.setString(1, msg.getSender());
		stmt.setString(2, receiver);
		stmt.setString(3, msg.getText());
		stmt.setBytes(4, msg.getImage());
		stmt.setString(5, msg.getTimestamp());
		//Nel caso del messaggio di gruppo il receiver è un intero : l'id del gruppo
		//uso la string receiver in modo da capire qual è l'username della persona che non ha ricevuto il messaggio
		if(msg.isAGroupMessage())
			stmt.setInt(6, msg.getGroupId());
		stmt.executeUpdate();
		stmt.close();
	}

	public synchronized String getLastAccess(String userToCheck) throws SQLException {
		String query = "SELECT Ultimo_accesso FROM Utente WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, userToCheck);
		ResultSet rs = stm.executeQuery();
		
		if(rs.next())
			return rs.getString(1);
	
		return null;
	}
	
	public synchronized void updateLastAccess(String username) throws SQLException {
		String date = Utilities.getCurrentISODate();
		String query = "UPDATE Utente SET Ultimo_accesso=? WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, date);
		stm.setString(2, username);
		stm.executeUpdate();
		stm.close();
	}

	public synchronized ArrayList<String> getGroupPartecipants(int groupId) throws SQLException {
		ArrayList <String> tmp = new ArrayList <String>();
		String query = "SELECT User_utente FROM UtenteInGruppo WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setInt(1, groupId);
		
		ResultSet rs = stm.executeQuery();
		while(rs.next()) {
			tmp.add(rs.getString("User_utente"));
		}
		
		return tmp;
	}		
	
	public synchronized ArrayList <Message> getPendingMessages(String username) throws SQLException {
		ArrayList <Message> lista = new ArrayList <Message>();
		
		//Prima aggiungo i messaggi singoli
		String query = "SELECT * FROM Messaggi WHERE Receiver=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		
		ResultSet rs = stm.executeQuery();
		while(rs.next()) {
			ChatMessage msg = new ChatMessage(rs.getString("Sender"), username);
			msg.setText(rs.getString("Message_text"));
			msg.setImage(rs.getBytes("Image"));
			msg.setGroupMessage(false);
			msg.setTimestamp(rs.getString("Date"));
			lista.add(msg);
		}
		
		rs.close();
		stm.close();
		
		//E dopo quelli di gruppo
		query = "SELECT * FROM MessaggioDiGruppo WHERE Receiver=?";
		stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		
		rs = stm.executeQuery();
		while(rs.next()) {
			ChatMessage msg = new ChatMessage(rs.getString("Sender"), username);
			msg.setText(rs.getString("Message_text"));
			msg.setImage(rs.getBytes("Image"));
			msg.setGroupMessage(true);
			msg.setGroupId(rs.getInt("Group_id"));
			msg.setTimestamp(rs.getString("Date"));
			lista.add(msg);
		}
		
		rs.close();
		stm.close();
		
		query = "DELETE FROM Messaggi WHERE Receiver=?;";
		stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		stm.executeUpdate();
		
		stm.close();
		
		query = "DELETE FROM MessaggioDiGruppo WHERE Receiver=?;";
		stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		stm.executeUpdate();
		
		stm.close();
		
		return lista;
	}
	
	public synchronized ArrayList <User> searchUsers(String subUsername) throws SQLException {
		ArrayList <User> lista = new ArrayList <User>();
		String query = "SELECT * FROM Utente WHERE Username LIKE '%" + subUsername + "%';";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		//stm.setString(1, "%" + subUsername + "%");
		ResultSet rs = stm.executeQuery();
		while (rs.next()) {
			System.out.println("Result found");
			User user = new User(rs.getString("Username"));
			user.setPropicFile(rs.getBytes("Img_profilo"));
			user.setStatus(rs.getString("Status"));
			lista.add(user);
		}
		
		rs.close();
		stm.close();
		
		return lista;
	}

	public int createGroup(String groupName, String owner, byte[] imgProfilo) throws SQLException {
		String query = "INSERT INTO Gruppo VALUES (null, ?, ?, ?);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, groupName);
		stm.setBytes(2, imgProfilo);
		stm.setString(3, owner);
		
		if(stm.executeUpdate() == 0)
			return -1;
		
		stm.close();
		
		query = "SELECT * FROM Gruppo WHERE Id_gruppo IN (SELECT max(Id_gruppo) FROM Gruppo);";
		stm = dbConnection.prepareStatement(query);
		ResultSet rs = stm.executeQuery();
		int group_id = -1;
		if(rs.next()) {
			group_id = rs.getInt("Id_gruppo");
		}
		
		rs.close();
		stm.close();
		
		return group_id;
	}

	public void addPartecipantsToGroup(int groupID, Vector<String> partecipants) throws SQLException {
		String query = "INSERT INTO UtenteInGruppo VALUES (?,?, null);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		
		for(String user : partecipants) {
			stm.setString(1, user);
			stm.setInt(2, groupID);
			stm.executeUpdate();
		}
	}

	public User getUserInfo(String userToCheck) throws SQLException {
		User user = null;
		String query = "SELECT * FROM Utente WHERE Username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, userToCheck);
		ResultSet rs = stm.executeQuery();
		if(rs.next()) {
			user = new User(rs.getString("Username"));
			user.setPropicFile(rs.getBytes("Img_profilo"));
			user.setStatus(rs.getString("Status"));
		}
		
		rs.close();
		stm.close();
		return user;
	}

	public User getGroupInfo(int groupId) throws SQLException {
		User user = null;
		String query = "SELECT * FROM Gruppo WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setInt(1, groupId);
		ResultSet rs = stm.executeQuery();
		if(rs.next()) {
			user = new User(rs.getString("Nome"));
			user.setPropicFile(rs.getBytes("Propic"));
			user.setGpOwner(rs.getString("Owner"));
		}
		
		stm.close();
		rs.close();
		
		return user;
	}
} 
