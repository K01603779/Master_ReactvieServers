package Websocket;

import java.util.List;
import java.util.Map;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

public class WebsocketConfigurator extends Configurator {
	
	
	 @Override
	    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
	        Map<String, List<String>> headers = request.getHeaders();
	        if (headers != null) {
	        	String username =headers.get("username").get(0);
	        	String password =headers.get("password").get(0);
	        	boolean create = Boolean.parseBoolean(headers.get("create").get(0));
	        	sec.getUserProperties().put("username", username);
	        	sec.getUserProperties().put("password", password);
	        	sec.getUserProperties().put("create", create);
	        }
	    }
}
