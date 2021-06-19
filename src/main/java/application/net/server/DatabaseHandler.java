package application.net.server;

import java.io.File;
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
				utente.setStatus(rs.getString("Status"));
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

	public  synchronized int createGroup(String groupName, String owner, byte[] imgProfilo) throws SQLException {
		String query = "INSERT INTO Gruppo VALUES (null, ?, ?, ?, ?);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, groupName);
		stm.setBytes(2, imgProfilo);
		stm.setString(3, owner);
		stm.setString(4, Utilities.getDateFromString(Utilities.getCurrentISODate()));
		
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

	public synchronized void addPartecipantsToGroup(int groupID, Vector<String> partecipants) throws SQLException {
		String query = "INSERT INTO UtenteInGruppo VALUES (?,?, null);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		
		for(String user : partecipants) {
			stm.setString(1, user);
			stm.setInt(2, groupID);
			stm.executeUpdate();
		}
	}

	public synchronized User getUserInfo(String userToCheck) throws SQLException {
		User user = null;
		String query = "SELECT * FROM Utente WHERE Username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, userToCheck);
		ResultSet rs = stm.executeQuery();
		if(rs.next()) {
			user = new LongUser(rs.getString("Username"), rs.getString("Nome"), rs.getString("Cognome"));
			user.setPropicFile(rs.getBytes("Img_profilo"));
			user.setStatus(rs.getString("Status"));
		}
		
		rs.close();
		stm.close();
		return user;
	}

	public synchronized User getGroupInfo(int groupId) throws SQLException {
		User user = null;
		String query = "SELECT * FROM Gruppo WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setInt(1, groupId);
		ResultSet rs = stm.executeQuery();
		if(rs.next()) {
			user = new User(rs.getString("Nome"));
			user.setPropicFile(rs.getBytes("Propic"));
			user.setGpOwner(rs.getString("Owner"));
			user.setCreationDate(rs.getString("Data_creazione"));
		}
		
		stm.close();
		rs.close();
		
		return user;
	}

	public synchronized void removeUserFromGroup(int groupId, String member) throws SQLException {
		String query = "DELETE FROM UtenteInGruppo WHERE User_utente=? AND Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, member);
		stm.setInt(2, groupId);
		stm.executeUpdate();
		stm.close();
	}

	public synchronized boolean checkGroupExists(int groupId) throws SQLException {
		String query = "SELECT * FROM Gruppo WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setInt(1, groupId);
		ResultSet rs = stm.executeQuery();
		boolean exist = rs.next();
		
		stm.close();
		rs.close();
		
		return exist;
	}

	public synchronized void deleteGroup(int groupId) throws SQLException {
		String [] queries = new String [3];
		queries [0] = "DELETE FROM MessaggioDiGruppo WHERE Group_id=?;";
		queries [1] = "DELETE FROM UtenteinGruppo WHERE Id_gruppo=?;";
		queries [2] = "DELETE FROM Gruppo WHERE Id_gruppo=?";
		
		for(int i = 0; i < 3; ++i) {
			PreparedStatement stm = dbConnection.prepareStatement(queries [i]);
			stm.setInt(1, groupId);
			stm.executeUpdate();
			stm.close();
		}
	}
	
	public synchronized void updateGroup(int groupId, byte[] proPic) throws SQLException {
		String query = "UPDATE Gruppo SET ProPic=? WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setBytes(1, proPic);
		stm.setInt(2, groupId);
		stm.executeUpdate();
		stm.close();
	}
	
	public synchronized void updateGroup(int groupId, String newName) throws SQLException {
		String query = "UPDATE Gruppo SET Nome=? WHERE Id_gruppo=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, newName);
		stm.setInt(2, groupId);
		stm.executeUpdate();
		stm.close();
	}

	public boolean updatePassword(String username, String newPassword) throws SQLException {
		String query = "Update Utente SET Password=? WHERE Username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
		stm.setString(2, username);
		boolean answer = stm.executeUpdate() != 0;
		stm.close();
		return answer;
	}

	public boolean updateProPic(String requester, File proPic) throws SQLException {
		String query = "Update Utente SET Img_profilo=? WHERE Username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setBytes(1, Utilities.getByteArrFromFile(proPic));
		stm.setString(2, requester);
		boolean answer = stm.executeUpdate() != 0;
		stm.close();
		return answer;
	}

	public boolean updateStatus(String requester, String status) throws SQLException {
		String query = "Update Utente SET Status=? WHERE Username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, status);
		stm.setString(2, requester);
		boolean answer = stm.executeUpdate() != 0;
		stm.close();
		return answer;
	}
}
