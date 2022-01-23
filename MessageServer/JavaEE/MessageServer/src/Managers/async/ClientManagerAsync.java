package Managers.async;

import java.io.IOException;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;

import Entities.Message;
import Managers.Manager;

public class ClientManagerAsync implements Manager{
	
	String user;
	Async connection;
	
	public ClientManagerAsync(String user, Async async) {
		this.user = user;
		this.connection=async;
		
	}
	public void sendMessage(Message message) {
		synchronized (connection) {
			connection.sendText(message.toString());
		}
	}

	@Override
	public void handleMsg(Message message) {
		if(message != null) {
			sendMessage(message);
		}
		
	}
	

}
