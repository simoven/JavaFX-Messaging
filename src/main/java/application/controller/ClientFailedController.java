package application.controller;

import application.net.misc.Utilities;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientFailedController implements EventHandler<WorkerStateEvent> {

	@Override
	public void handle(WorkerStateEvent event) {
		 Utilities.getInstance().logToFile(event.getSource().getException().getMessage());
	}
}
