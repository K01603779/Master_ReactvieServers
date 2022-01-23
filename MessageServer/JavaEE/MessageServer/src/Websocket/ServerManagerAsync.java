package Websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CompletableFuture;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import DBConnection.DBMessage;
import Entities.Group;
import Entities.Message;
import Entities.MsgType;
import Entities.Respondent;
import Entities.User;
import Managers.Manager;
import Managers.async.ClientManagerAsync;
import Managers.async.GroupManagerAsync;

@ServerEndpoint(value = "/messageasync", configurator = WebsocketConfigurator.class)
public class ServerManagerAsync {

	private static HashMap<String, Manager> managers = new HashMap<String, Manager>();
	private static HashMap<Session, String> userSessions = new HashMap<Session, String>();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		//System.out.println("New Connection");
		String username = (String) config.getUserProperties().get("username");
		String password = (String) config.getUserProperties().get("password");
		boolean create = (boolean) config.getUserProperties().get("create");
		session.setMaxIdleTimeout(0);
		session.setMaxTextMessageBufferSize(100000);
		if (managers.get(username) == null) {
			CompletableFuture.supplyAsync(() -> DBMessage.getUser(username, password, 3),
					DBMessage.getExecutor()).thenAccept(dbResult -> {
						if (dbResult.success) {
							User user = dbResult.result;
							if (create) {
								if (user == null) {
									CompletableFuture
											.supplyAsync(() -> DBMessage.createUser(username, password, 3),
													DBMessage.getExecutor())
											.thenAccept(res -> {
												String result = res.result;
												if (res.success) {
													if (!result.equals("")) {
														System.out.println("User created");
														ClientManagerAsync client = new ClientManagerAsync(username,
																session.getAsyncRemote());
														managers.put(username, client);
														userSessions.put(session, username);
													} else {
														closeSession(session,
																"ServerManager Create User: Creation of user "
																		+ username + "failed in DB");
													}
												} else {
													closeSession(session, "ServerManager : " + username
															+ " couldn't be created (SQL-Error)");
												}
											});
								} else {
									closeSession(session,
											"ServerManager Create User: User " + username + "already exists");
								}
							} else {
								if (user != null) {
									System.out.println("User in DB found " + user.id + " Password " + user.password);
									ClientManagerAsync client = new ClientManagerAsync(username,
											session.getAsyncRemote());
									managers.put(username, client);
									userSessions.put(session, username);
									CompletableFuture
											.supplyAsync(() -> DBMessage.getMessageOfUser(username, 3),
													DBMessage.getExecutor())
											.thenAccept(dbMessage -> {
												List<Message> messages = dbMessage.result;
												if (dbMessage.success) {
													for (Message message : messages) {
														ServerManagerAsync.handleMsg(message);
													}
												}
											});
								} else {
									closeSession(session, "ServerManager: No User  " + username + "found in DB");
								}
							}
						} else {
							closeSession(session, "ServerManager : " + username + " couldn't be connected (SQL-Error)");
						}
					});
		} else {
			closeSession(session, "ServerManager: No User  " + username + "already logged in ");
		}
	}

	@OnClose
	public void onClose(Session session,CloseReason reason) {
		System.out.println("Reason : " + reason.getReasonPhrase());
		System.out.println("Close Code : " + reason.getCloseCode().toString());
		String username = userSessions.remove(session);
		if (username != "") {
			managers.remove(username);
			System.out.print("Username " + username + " logged out" );
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
		if (message.receiverID.equals("server")) {
			if (message.type == MsgType.CreateGroup) {
				createGroup(message);
			} else if (message.type == MsgType.RemoveGroup) {
				delteGroup(message);
			}
		} else {
			if (managers.containsKey(message.receiverID)) {
				managers.get(message.receiverID).handleMsg(message);
			} else {
				CompletableFuture.supplyAsync(() -> DBMessage.getRespondent(message.receiverID, 3),
						DBMessage.getExecutor()).thenAccept(dbResp -> {
							Respondent resp = dbResp.result;
							if (dbResp.success) {
								if (resp instanceof User) {
									System.out.println("User not logged in " + message);
									CompletableFuture.supplyAsync(() -> DBMessage.storeMsg(message, 3),DBMessage.getExecutor()).thenAccept(result ->{
										if(!result) {
											sendBackErrorMsg(message);
										}
									});
									DBMessage.storeMsg(message,3); 
								} else if (resp instanceof Group) {
									Group grp = (Group) resp;
									System.out.println("Created Group " + resp.id);
									CompletableFuture
											.supplyAsync(() -> DBMessage.getUserEntriesFromGroup(resp.id,3),
													DBMessage.getExecutor())
											.thenAccept(dbEntries -> {
												SimpleEntry<List<String>, List<String>> entries =dbEntries.result; 
												GroupManagerAsync group = new GroupManagerAsync(grp.manager, grp.id,
														entries.getKey(), entries.getValue());
												managers.put(resp.id, group);
												group.handleMsg(message);
											});
								}
							} else {
								sendBackErrorMsg(message);
							}
						});
			}
		}
	}

	private static void delteGroup(Message msg) {
		Manager manager = managers.get(msg.content);
		if (manager != null && manager instanceof GroupManagerAsync) {
			System.out.println("Deleted Group " + msg.content);
			if (((GroupManagerAsync) manager).getCreator().equals(msg.senderID)) {
				managers.remove(msg.content);
				CompletableFuture.supplyAsync(() -> DBMessage.deleteEntity(msg.content, 3),
						DBMessage.getExecutor()).thenAccept(dbResult -> {
							if (!dbResult) {
								sendBackErrorMsg(msg);
							}
						});
			}
		} else {
			CompletableFuture.supplyAsync(() -> DBMessage.getGroup(msg.content, 3),
					DBMessage.getExecutor()).thenAccept(dbGr -> {
						Group gr = dbGr.result;
						if (dbGr.success) {
							if (gr != null) {
								if (gr.manager.equals(msg.senderID)) {
									CompletableFuture.supplyAsync(() -> DBMessage.deleteEntity(gr.id, 3),
											DBMessage.getExecutor()).thenAccept(dbRet -> {
												if (!dbRet) {
													sendBackErrorMsg(msg);
												}
											});
								}
							}
						} else {
							sendBackErrorMsg(msg);
						}
					});
		}

	}

	private static void createGroup(Message msg) {
		if (managers.get(msg.content) == null) {
			CompletableFuture.supplyAsync(() -> DBMessage.getGroup(msg.content, 3),
					DBMessage.getExecutor()).thenAccept(dbGr -> {
						Group gr = dbGr.result;
						if (dbGr.success) {
							if (gr == null) {
								CompletableFuture.supplyAsync(
										() -> DBMessage.createGroup(msg.content, msg.senderID, 3),
										DBMessage.getExecutor()).thenAccept(
												result -> {
													// ConnectionPoolMessage.createGroup(msg.content, msg.senderID); //
													if (result.success) {
														System.out.println("Created Group " + msg.content);
														GroupManagerAsync group = new GroupManagerAsync(msg.senderID,
																msg.content);
														managers.put(msg.content, group);
													} else {
														sendBackErrorMsg(msg);
													}
												});
							}
						} else {
							sendBackErrorMsg(msg);
						}
					});
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
		if (ret != null && ret instanceof ClientManagerAsync) {
			Message errorMsg = new Message("server", msg.senderID, msg.toString(), MsgType.Error);
			ret.handleMsg(errorMsg);
		}
	}

}
