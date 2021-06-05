package application.controller;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientFailedController implements EventHandler<WorkerStateEvent> {

	@Override
	public void handle(WorkerStateEvent event) {
		System.out.println("Ho fallito");
		event.getSource().getException().printStackTrace();
	}

}
