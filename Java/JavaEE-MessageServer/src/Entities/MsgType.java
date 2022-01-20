package Entities;

public enum MsgType {
	Error, // TODO
	Private, CreateGroup, InviteToGroup, DeclineRequest, AcceptRequest, LeaveGroup, RemoveGroup;
	
	public static MsgType of(int value) {
		switch(value){
			case 1:
				return MsgType.Private;
			case 2:
				return MsgType.CreateGroup;
			case 3:
				return MsgType.InviteToGroup;
			case 4:
				return MsgType.DeclineRequest;
			case 5:
				return MsgType.AcceptRequest;
			case 6:
				return MsgType.LeaveGroup;
			case 7: 
				return MsgType.RemoveGroup;
			default:
				return MsgType.Error;
		}
	}
};