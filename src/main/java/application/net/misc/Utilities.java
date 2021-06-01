package application.net.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Utilities {

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
}
