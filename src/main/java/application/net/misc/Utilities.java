package application.net.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Utilities {

	public static final String USERNAME_TOO_SHORT = "L'username deve essere lungo almeno 4 caratteri";
	public static final String PASSWORD_TOO_SHORT = "La password deve essere lunga almeno 8 caratteri";
	public static final String USERNAME_TOO_LONG = "L'username deve essere lungo al massimo 16 caratteri";
	public static final String PASSWORD_TOO_LONG = "La password deve essere lunga al massimo 20 caratteri";
	public static final String USERNAME_VALID = "Username is valid";
	public static final String PASSWORD_VALID = "Password is valid";
	public static final String USERNAME_NOT_VALID = "I caratteri dell'username non sono validi";
	public static final String PASSWORD_NOT_VALID = "I caratteri della password non sono validi";
	
	
	public static byte[] getByteArrFromFile(File file) {
		if(file == null)
			return null;
		
		ByteArrayOutputStream bos = null;
		        
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            bos = new ByteArrayOutputStream();
            for (int len; (len = fis.read(buffer)) != -1;) {
                bos.write(buffer, 0, len);
            }
            fis.close();
        } catch (Exception e) {
            return null;
        } 
        
        return bos != null ? bos.toByteArray() : null;
	}
	
	public static String getCurrentISODate() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
	}

	public static String getDateFromString(String date) {
		ZoneId id = ZoneId.systemDefault();
		String localTime = ZonedDateTime.parse(date).withZoneSameInstant(id).toString();
		String [] parti = localTime.split("T");
		return parti [0];
	}
	
	public static String getHourFromString(String date) {
		ZoneId id = ZoneId.systemDefault();
		String localTime = ZonedDateTime.parse(date).withZoneSameInstant(id).toString();
		String [] parti = localTime.split("T");
		return parti [1].split("\\.") [0];
	}
	
	public static String getHourFromStringTrimmed(String date) {
		String hour = getHourFromString(date);
		String [] split = hour.split(":");
		return split [0] + ":" + split [1];
	}
	
	public static String checkIfUsernameValid(String username) {
		if(username == null || username.length() < 4)
			return USERNAME_TOO_SHORT;
		
		//null è riservato ai messaggi del server
		if(username.equals("null"))
			return USERNAME_NOT_VALID;
		
		if(username.length() > 16)
			return USERNAME_TOO_LONG;
		
		//Può contenere lettere, numeri e underscore
		String regex = "[a-zA-Z_0-9]+";
		if(Pattern.matches(regex, username))
			return USERNAME_VALID;
		
		return USERNAME_NOT_VALID;
	}
	
	public static String checkIfPasswordValid(String password) {
		if(password == null || password.length() < 8)
			return PASSWORD_TOO_SHORT;
		
		if(password.length() > 20)
			return PASSWORD_TOO_LONG;
		
		//Deve contenere almeno un numero, carattere lowercase, uppercase, speciale e niente spazi
		String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).*";
		if(Pattern.matches(regex, password))
			return PASSWORD_VALID;
		
		return PASSWORD_NOT_VALID;
	}
	
	public static boolean checkIfNameValid(String name) {
		if(name == null || name.isBlank())
			return false;
		
		String regex = "[a-zA-Z\\s]+";
		return Pattern.matches(regex, name);
		
	}
	
	public static String getTodayDate() {
		return getDateFromString(getCurrentISODate());
	}
	
	public static boolean checkIfGroupNameValid(String name) {
		if(name == null || name.isBlank())
			return false;
		
		String regex = "[a-zA-Z0-9\\s]+";
		return Pattern.matches(regex, name);
	}
}
