package application.net.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.security.crypto.bcrypt.BCrypt;

import application.logic.ImageMessage;
import application.logic.TextMessage;
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
		User utente = null;
		String query = "SELECT * FROM Utente WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		
		ResultSet rs = stm.executeQuery();
		
		if(rs.next()) {
			String pass = rs.getString("password");
			if(BCrypt.checkpw(password, pass)) {
				utente = new User(username, password, rs.getString("Nome"), rs.getString("Cognome"));
				utente.setPropicFile(rs.getBytes("ProPic"));
			}
		}
		
		rs.close();
		
		return utente;
	}
	
	public synchronized boolean registerUser(User utente) throws SQLException {
		if(checkUserExist(utente))
			return false;
		
		String query = "INSERT INTO Utente VALUES(?,?,?,?,?,null);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, utente.getUsername());
		stm.setString(2, BCrypt.hashpw(utente.getPassword(), BCrypt.gensalt(12)));
		stm.setString(3, utente.getName());
		stm.setString(4, utente.getLastName());
		stm.setBytes(5, Utilities.getByteArrFromFile(utente.getProPicAsFile()));
		
		int res = stm.executeUpdate();
		stm.close();
		
		return res != 0;
	}
	
	public synchronized boolean checkUserExist(User utente) throws SQLException {
		String query = "SELECT * FROM users WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, utente.getUsername());
		ResultSet rs = stm.executeQuery();
		boolean result = rs.next();
		stm.close();
	
		return result;
	}
	
	public synchronized void addPendingMessage(TextMessage msg, String receiver) throws SQLException {
		String dateTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String query;
		if(msg.isAGroupMessage())
			query = "INSERT INTO MessaggioDiGruppo Values(null, ?, ?, ?, null, ?, ?);";
		else 
			query = "INSERT INTO Messaggi Values(null, ?, ?, ?, null, ?);";
		PreparedStatement stmt = dbConnection.prepareStatement(query);
		stmt.setString(1, msg.getSender());
		stmt.setString(2, receiver);
		stmt.setString(3, msg.getText());
		stmt.setString(4, dateTime);
		//Nel caso del messaggio di gruppo il receiver è un intero : l'id del gruppo
		//uso la string receiver in modo da capire qual è l'username della persona che non ha ricevuto il messaggio
		if(msg.isAGroupMessage())
			stmt.setInt(5, Integer.parseInt(msg.getReceiver()));
		stmt.executeUpdate();
		stmt.close();
	}

	public synchronized void addPendingImage(ImageMessage msg, String receiver) throws SQLException {
		String dateTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String query;
		if(msg.isAGroupMessage())
			query = "INSERT INTO MessaggioDiGruppo Values(null, ?, ?, null, ?, ?, ?)";
		else
			query = "INSERT INTO Messaggi Values(null, ?, ?, null, ?, ?);";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, msg.getSender());
		stm.setString(2, receiver);
		stm.setBytes(3, msg.getImage());
		stm.setString(4, dateTime);
		if(msg.isAGroupMessage())
			stm.setInt(5, Integer.parseInt(msg.getReceiver()));
		stm.executeUpdate();
		stm.close();
		
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
		String query = "UPDATE Utente SET Ultimo_accesso=(SELECT datetime('now')) WHERE username=?;";
		PreparedStatement stm = dbConnection.prepareStatement(query);
		stm.setString(1, username);
		stm.executeUpdate();
		stm.close();
	}

	public ArrayList<String> getGroupPartecipants(String groupId) throws SQLException {
		ArrayList <String> tmp = new ArrayList <String>();
		try {
			String query = "SELECT User_utente FROM UtenteInGruppo WHERE Id_gruppo=?;";
			PreparedStatement stm = dbConnection.prepareStatement(query);
			stm.setInt(1, Integer.parseInt(groupId));
			
			ResultSet rs = stm.executeQuery();
			while(rs.next()) {
				tmp.add(rs.getString("User_utente"));
			}
		} catch (NumberFormatException e) {
			//Significa che il group id non è un intero, quindi è sbagliato
			return null;
		}
		
		return tmp;
	}		
}
