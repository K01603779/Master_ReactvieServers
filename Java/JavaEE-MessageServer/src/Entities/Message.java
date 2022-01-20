package Entities;

import org.json.JSONObject;

public class Message {
	public String senderID, receiverID, content;
	public MsgType type;

	public Message(String senderID, String receiverID, String content, int type) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.content = content;
		this.type = MsgType.of(type);
	}

	public Message(String senderID, String receiverID, String content, MsgType type) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.content = content;
		this.type = type;
	}

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("senderID", senderID);
		json.put("receiverID", receiverID);
		json.put("type", type.ordinal());
		json.put("content", content);
		// TODO Auto-generated method stub
		return json.toString();
	}
	
	public String getSenderID() {
		return this.senderID;
	}
	public String getReceiverID() {
		return this.receiverID;
	}
	public int getType() {
		return this.type.ordinal();
	}
	public String getContent() {
		return this.content;
	}

}