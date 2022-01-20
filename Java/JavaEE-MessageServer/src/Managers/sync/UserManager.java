package Managers.sync;

import java.io.IOException;

import javax.websocket.RemoteEndpoint.Basic;

import Entities.Message;
import Managers.Manager;

public class UserManager implements Manager {

	String user;
	Basic connection;

	public UserManager(String user, Basic connection) {
		this.user = user;
		this.connection = connection;

	}

	public void sendMessage(Message message) {
		synchronized (connection) {
			try {
				connection.sendText(message.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handleMsg(Message message) {
		if (message != null) {
			sendMessage(message);
		}

	}

}
