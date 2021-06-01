package application.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

	private ServerSocket server;
	private ExecutorService executorService;
	
	private Map <String, Socket> activeUser;
	
	public Server() {
		activeUser = new HashMap <String, Socket>();
		executorService = Executors.newCachedThreadPool();
	}
	
	public boolean startServer() {
		if(DatabaseHandler.getInstance().tryConnection()) {
			try {
				server = new ServerSocket(8000);
				
				Thread th = new Thread(this);
				th.setDaemon(true);
				th.start();
				
				return true;
			} catch (IOException e) {
				server = null;
				return false;
			}
		}
		
		return false;
	}
	
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			try {
				System.out.println("[SERVER] Waiting for connections..");
				Socket socket = server.accept();
				System.out.println("[SERVER] New Client connected");
				
				ServerListener sl = new ServerListener(socket, this);
				executorService.submit(sl);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkIsUserLogged(String username) {
		return activeUser.get(username) != null;
	}
	
	public void addOnlineUser(String username, Socket socket) {
		activeUser.put(username, socket);
	}
	
	public boolean disconnectUser(String username) {
		if(username == null)
			return false;
		
		if(activeUser.get(username) != null)
			activeUser.remove(username);
		
		return true;
	}
	
	public Socket getSocket(String username) {
		return activeUser.get(username);
	}
}
