package application.controller;

import application.logic.ChatLogic;
import application.logic.messages.ChatMessage;
import application.logic.messages.Message;
import application.net.client.Client;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientSucceedController implements EventHandler<WorkerStateEvent> {

	@Override
	public void handle(WorkerStateEvent event) {
		Message packet = (Message) event.getSource().getValue();
		if(packet instanceof ChatMessage) {
			ChatLogic.getInstance().addIncomingMessage((ChatMessage) packet);
			System.out.println(((ChatMessage) packet).getText());
		}
		
		Client.getInstance().restart();
		System.out.println("Restart");
	}
	
}
