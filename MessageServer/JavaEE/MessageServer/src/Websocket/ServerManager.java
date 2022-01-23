package Websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import DBConnection.DBMessage;
import DBConnection.DBResult;
import Entities.Group;
import Entities.Message;
import Entities.MsgType;
import Entities.Respondent;
import Entities.User;
import Managers.Manager;
import Managers.sync.GroupManager;
import Managers.sync.UserManager;

@ServerEndpoint(value = "/message", configurator = WebsocketConfigurator.class)
public class ServerManager {

	private static HashMap<String, Manager> managers = new HashMap<String, Manager>();
	private static HashMap<Session, String> userSessions = new HashMap<Session, String>();
	private static final String serverID = "server";
	public static final int retryCnt = 3;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		//System.out.println("New Connection");
		String username = (String) config.getUserProperties().get("username");
		String password = (String) config.getUserProperties().get("password");
		boolean create = (boolean) config.getUserProperties().get("create");
		session.setMaxIdleTimeout(0);
		if (managers.get(username) == null) {
			//System.out.println("New Connection - get User");
			DBResult<User> dbUser = DBMessage.getUser(username, password, retryCnt);
			//System.out.println("New Connection - get User -Result");
			if (dbUser.success) {
				User user = dbUser.result;
				if (create) {
					if (user == null) {
						// create user
						DBResult<String> dbString = DBMessage.createUser(username, password, retryCnt); 
						String result = dbString.result;
						if (dbString.success) {
							if (!result.equals("")) {
								System.out.println("User created");
								UserManager client = new UserManager(username, session.getBasicRemote());
								managers.put(username, client);
								userSessions.put(session, username);
							} else {
								closeSession(session,
										"ServerManager Create User: Creation of user " + username + "failed in DB");
							}
						} else {
							closeSession(session, "ServerManager : " + username + " couldn't be connected (SQL-Error)");
						}
					} else {
						closeSession(session, "ServerManager Create User: User " + username + "already exists");
					}
				} else {
					if (user != null) {
						System.out.println("User in DB found " + user.id + " Password " + user.password);
						UserManager client = new UserManager(username, session.getBasicRemote());
						managers.put(username, client);
						userSessions.put(session, username);
						DBResult<List<Message>> dbMessage = DBMessage.getMessageOfUser(username, retryCnt);
						if (dbMessage.success) {
							List<Message> messages = dbMessage.result;
							for (Message message : messages) {
								ServerManager.handleMsg(message);
							}
						}
					} else {
						closeSession(session, "ServerManager: No User  " + username + "found in DB");
					}
				}
			} else {
				closeSession(session, "ServerManager : " + username + " couldn't be connected (SQL-Error)");
			}
		} else {
			closeSession(session, "ServerManager: No User  " + username + "already logged in ");
		}
	}

	@OnClose
	public void onClose(Session session) {
		String username = userSessions.remove(session);
		if (username != "") {
			managers.remove(username);
			System.out.println("Username " + username + " logged out");
		}
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void handleMessage(String text, Session session) {
		try {
		JSONObject obj = new JSONObject(text);
		//System.out.println("handle Message " + text);
		String senderID = obj.getString("senderID");
		String receiverID = obj.getString("receiverID");
		String content = "";
		if (obj.has("content")) {
			content = obj.getString("content");
		}
		int type = obj.getInt("type");
		Message message = new Message(senderID, receiverID, content, type);
		handleMsg(message);
		}catch(Exception e) {
			System.out.println("Received Exception " + e.getLocalizedMessage());
		}
	}

	public static void handleMsg(Message message) {

		if (message.receiverID.equals(serverID)) {
			if (message.type == MsgType.CreateGroup) {
				createGroup(message);
			} else if (message.type == MsgType.RemoveGroup) {
				delteGroup(message);
			}
		} else {
			if (managers.containsKey(message.receiverID)) {
				managers.get(message.receiverID).handleMsg(message);
			} else {
				DBResult<Respondent> dbRespondent = DBMessage.getRespondent(message.receiverID, retryCnt);
				if (dbRespondent.success) {
					Respondent resp = dbRespondent.result;
					boolean success;
					if (resp instanceof User) {
						System.out.println("User not logged in sync " + message);
						success = DBMessage.storeMsg(message, retryCnt);
						if (!success) {
							sendBackErrorMsg(message);
						}
					} else if (resp instanceof Group) {
						Group grp = (Group) resp;
						System.out.println("Created Group " + resp.id);
						SimpleEntry<List<String>, List<String>> entries = DBMessage.getUserEntriesFromGroup(resp.id,
								retryCnt).result;
						GroupManager group = new GroupManager(grp.manager, grp.id, entries.getKey(),
								entries.getValue());
						managers.put(resp.id, group);
						group.handleMsg(message);
					}
				} else {
					sendBackErrorMsg(message);
				}
			}
		}
	}

	private static void delteGroup(Message msg) {
		Manager manager = managers.get(msg.content);
		boolean result;
		if (manager != null && manager instanceof GroupManager) {
			System.out.println("Deleted Group " + msg.content);
			if (((GroupManager) manager).getCreator().equals(msg.senderID)) {
				managers.remove(msg.content);
				result = DBMessage.deleteEntity(msg.content, retryCnt);
				if (!result) {
					sendBackErrorMsg(msg);
				}
			}
		} else {
			DBResult<Group> dbGroup = DBMessage.getGroup(msg.content, retryCnt);
			if (dbGroup.success) {
				Group gr = dbGroup.result;
				if (gr != null) {
					if (gr.manager.equals(msg.senderID)) {
						DBMessage.deleteEntity(gr.id, retryCnt);
					}
				}
			} else {
				sendBackErrorMsg(msg);
			}
		}

	}

	private static void createGroup(Message msg) {
		if (managers.get(msg.content) == null) {
			DBResult<Group> dbResult = DBMessage.getGroup(msg.content, retryCnt);
			if (dbResult.success) {
				Group gr = dbResult.result;
				if (gr == null) {
					DBMessage.createGroup(msg.content, msg.senderID, retryCnt);
					System.out.println("Created Group " + msg.content);
					GroupManager group = new GroupManager(msg.senderID, msg.content);
					managers.put(msg.content, group);
				}
			} else {
				sendBackErrorMsg(msg);
			}
		}
	}

	private static void closeSession(Session session, String message) {
		try {
			System.out.println(message);
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendBackErrorMsg(Message msg) {
		System.out.println("sendBack Msg" + msg);
		Manager ret = managers.get(msg.senderID);
		if (ret != null && ret instanceof UserManager) {
			Message errorMsg = new Message("server", msg.senderID, msg.toString(), MsgType.Error);
			ret.handleMsg(errorMsg);
		}
	}
	
	

}
