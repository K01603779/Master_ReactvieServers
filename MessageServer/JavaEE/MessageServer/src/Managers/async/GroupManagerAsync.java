package Managers.async;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import DBConnection.DBMessage;
import Entities.Message;
import Entities.MsgType;
import Entities.User;
import Managers.Manager;
import Websocket.ServerManagerAsync;

public class GroupManagerAsync implements Manager {

	HashSet<String> members;
	HashSet<String> invitees;
	String creator;
	String groupName;

	public GroupManagerAsync(String creator, String groupName) {
		this.creator = creator;
		this.groupName = groupName;
		members = new HashSet<>();
		members.add(creator);
		invitees = new HashSet<>();
	}

	public GroupManagerAsync(String creator, String groupName, List<String> member, List<String> invitee) {
		this.creator = creator;
		this.groupName = groupName;
		members = new HashSet<>();
		for (String entry : member) {
			members.add(entry);
		}
		members.add(creator);
		invitees = new HashSet<>();
		for (String entry : invitee) {
			invitees.add(entry);
		}
	}

	@Override
	public void handleMsg(Message message) {
		//System.out.println("GroupManager " + groupName + "Received Msg " + message.toString());
		if (message != null && (invitees.contains(message.senderID) || members.contains(message.senderID))) {
			switch (message.type) {
			case Private:
				sendMessage(message);
				break;
			case AcceptRequest:
				acceptRequest(message);
				break;
			case DeclineRequest:
				declineRequest(message);
				break;
			case LeaveGroup:
				leaveGroup(message);
				break;
			case InviteToGroup:
				inviteToGroup(message);
				break;
			default:
				break;
			}
		}
	}

	private void acceptRequest(Message message) {
		System.out.println("GroupManager: User" + message.senderID + " accepted request");
		if (invitees.contains(message.senderID)) {
			members.add(message.senderID);
			invitees.remove(message.senderID);
			CompletableFuture.supplyAsync(
					() -> DBMessage.updateGroupEntry(this.groupName, message.senderID, true, 3),
					DBMessage.getExecutor()).thenAccept(result -> {
						if (!result) {
							ServerManagerAsync.sendBackErrorMsg(message);
						}
					});

		}
	}

	private void declineRequest(Message message) {
		invitees.remove(message.senderID);
		CompletableFuture.supplyAsync(() -> DBMessage.removeGroupEntry(this.groupName, message.senderID, 3),
				DBMessage.getExecutor()).thenAccept(result -> {
					if (!result) {
						ServerManagerAsync.sendBackErrorMsg(message);
					}
				}

		);
	}

	private void leaveGroup(Message message) {
		members.remove(message.senderID);
		CompletableFuture.supplyAsync(() -> DBMessage.removeGroupEntry(this.groupName, message.senderID, 3),
				DBMessage.getExecutor()).thenAccept(result -> {
					if (!result) {
						ServerManagerAsync.sendBackErrorMsg(message);
					}
				});
	}

	private void inviteToGroup(Message message) {
		if (!(members.contains(message.content) && invitees.contains(message.content))) {
			CompletableFuture.supplyAsync(() -> DBMessage.getUser(message.getContent(), 3),
					DBMessage.getExecutor()).thenAccept(dbUser -> {
						User user = dbUser.result;
						if (dbUser.success) {
							if (user != null) {
								System.out.println("GroupMangerAsync add to inveets " + message.content);
								invitees.add(message.content);								
								ServerManagerAsync.handleMsg(
										new Message(groupName, message.content, "", MsgType.InviteToGroup));
								CompletableFuture
										.supplyAsync(() -> DBMessage.updateGroupEntry(this.groupName,
												user.id, false, 3), DBMessage.getExecutor())
										.thenAccept(result -> {
											if (!result) {
												ServerManagerAsync.sendBackErrorMsg(message);
											}
										});
							}
						} else {
							ServerManagerAsync.sendBackErrorMsg(message);
						}
					});
		}
	}

	private void sendMessage(Message message) {
		Message send;
		System.out.println("GroupManager : " + groupName + "sends message to members");
		for (String member : members) {
			send = new Message(groupName + " -> " + message.senderID, member, message.content, message.type);
			ServerManagerAsync.handleMsg(send);
		}
	}

	public String getCreator() {
		return this.creator;
	}

}
