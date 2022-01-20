package DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import Entities.Group;
import Entities.Message;
import Entities.Respondent;
import Entities.User;

public class DBMessage extends ConnectionPool {

	public static DBResult<User> getUser(String username, String password, int retryCnt) {
		User user = null;
		Connection con = null;
		try {
			con = getConnection();
			System.out.println("New Connection - get Connection");
			// TODO use MsgType
			PreparedStatement prep = con
					.prepareStatement("select * from respondent where respondentID =? and content =? and type = 0");
			prep.setString(1, username);
			prep.setString(2, password);
			ResultSet rs = prep.executeQuery();
			user = mapUser(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<User>(user, false);
			} else {
				return getUser(username, password, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<User>(user, true);
	}

	private static User mapUser(ResultSet set) throws SQLException {
		if (set.next()) {
			String username = set.getString("respondentID");
			String password = set.getString("content");
			return new User(username, password);
		}
		return null;
	}

	private static Respondent mapRespondant(ResultSet set) throws SQLException {
		if (set.next()) {
			int type = set.getInt("type");
			String content = set.getString("content");
			String id = set.getString("respondentID");
			if (type == 0) {
				return new User(id, content);
			} else if (type == 1) {
				return new Group(id, content);
			}
		}
		return null;
	}

	public static DBResult<User> getUser(String username, int retryCnt) {
		User user = null;
		Connection con = null;
		try {
			con = getConnection();
			// TODO use MsgType
			PreparedStatement prep = con
					.prepareStatement("select * from respondent where respondentID =? and type = 0");
			prep.setString(1, username);
			ResultSet rs = prep.executeQuery();
			user = mapUser(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<User>(null, false);
			} else {
				return getUser(username, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<User>(user, true);
	}

	public static DBResult<Respondent> getRespondent(String id, int retryCnt) {
		Respondent respondent = null;
		Connection con = null;
		try {
			con = getConnection();
			// TODO use MsgType
			PreparedStatement prep = con.prepareStatement("select * from respondent where respondentID =?");
			prep.setString(1, id);
			ResultSet rs = prep.executeQuery();
			respondent = mapRespondant(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<Respondent>(null, false);
			} else {
				return getRespondent(id, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<Respondent>(respondent, true);
	}

	public static DBResult<String> createUser(String username, String password, int retryCnt) {
		String user = "";
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con
					.prepareStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,0)");
			prep.setString(1, username);
			prep.setString(2, password);
			prep.execute();
			prep.close();
			prep = con.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rset = prep.executeQuery();
			rset.next();
			user = rset.getString("LAST_INSERT_ID()");
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<String>("", false);
			} else {
				createUser(username, password, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<String>(user, true);
	}

	public static DBResult<Group> getGroup(String groupname,int retryCnt) {
		Group respondent = null;
		Connection con = null;
		try {
			con = getConnection();
			// TODO use MsgType
			PreparedStatement prep = con
					.prepareStatement("select * from respondent where respondentID =? and type = 1");
			prep.setString(1, groupname);
			ResultSet rs = prep.executeQuery();
			respondent = (Group) mapRespondant(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if(retryCnt == 0) {
				return new DBResult<Group>(null, false);
			}else {
				return getGroup(groupname, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<Group>(respondent, true);
	}

	public static DBResult<String> createGroup(String groupname, String creator,int retryCnt) {
		String group = "";
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con
					.prepareStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,1)");
			prep.setString(1, groupname);
			prep.setString(2, creator);
			prep.execute();
			prep.close();
			prep = con.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rset = prep.executeQuery();
			rset.next();
			group = rset.getString("LAST_INSERT_ID()");
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if(retryCnt == 0) {
				return new DBResult<String>("", false);
			}else {
				return createGroup(groupname, creator, retryCnt-1);
			}
		} finally {
		}
		return new DBResult<String>(group, true);
	}

	public static boolean removeGroupEntry(String groupname, String username,int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("delete from groupEntry where userID = ? and groupID = ?");
			prep.setString(1, username);
			prep.setString(2, groupname);
			prep.execute();
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			if(retryCnt ==0) {
				return false;
			}else {
				return removeGroupEntry(groupname, username, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static boolean deleteEntity(String id, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("delete from respondent where respondentID = ?");
			prep.setString(1, id);
			prep.execute();
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			if(retryCnt ==0) {
				return false;
			}else {
				return deleteEntity(id, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static boolean updateGroupEntry(String groupname, String username, boolean accepted, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con
					.prepareStatement("replace  into groupEntry (groupID, userID, accepted) values (?,?,?)");
			prep.setString(1, groupname);
			prep.setString(2, username);
			prep.setBoolean(3, accepted);
			prep.execute();
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			if(retryCnt ==0) {
				return false;
			}else {
				return updateGroupEntry(groupname, username, accepted, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static DBResult<SimpleEntry<List<String>, List<String>>> getUserEntriesFromGroup(String groupname, int retryCnt) {
		List<String> members = new LinkedList<String>();
		List<String> invitees = new LinkedList<String>();
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from groupEntry where groupID = ?");
			prep.setString(1, groupname);
			ResultSet rs = prep.executeQuery();
			boolean accepted = false;
			String userID = "";
			while (rs.next()) {
				accepted = rs.getBoolean("accepted");
				userID = rs.getString("userID");
				if (accepted) {
					members.add(userID);
				} else {
					invitees.add(userID);
				}
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if(retryCnt ==0) {
				return new DBResult<SimpleEntry<List<String>,List<String>>>(null, false);
			}else {
				return getUserEntriesFromGroup(groupname,retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		SimpleEntry<List<String>, List<String>> entry =new SimpleEntry<List<String>, List<String>>(members, invitees);
		return new DBResult<SimpleEntry<List<String>,List<String>>>(entry, true);

	}

	public static boolean storeMsg(Message message, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con
					.prepareStatement("INSERT INTO message (senderID, receiverID, content,type) VALUES (?,?,?,?)");
			prep.setString(1, message.senderID);
			prep.setString(2, message.receiverID);
			prep.setString(3, message.content);
			prep.setInt(4, message.type.ordinal());
			prep.execute();
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			if(retryCnt ==0) {
				return false;
			}else {
				return storeMsg(message, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static DBResult<List<Message>> getMessageOfUser(String username,int retryCnt) {
		LinkedList<Message> messages = new LinkedList<Message>();
		String senderID, receiverID, content;
		Message me;
		PreparedStatement prep2;
		int type, messageID;
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from message where receiverID = ?");
			prep.setString(1, username);
			ResultSet rs = prep.executeQuery();

			while (rs.next()) {
				senderID = rs.getString("senderID");
				receiverID = rs.getString("receiverID");
				content = rs.getString("content");
				type = rs.getInt("type");
				messageID = rs.getInt("messageID");
				me = new Message(senderID, receiverID, content, type);
				messages.add(me);
				/*prep2 = con.prepareStatement("delete from message where messageID = ?");
				prep2.setInt(1, messageID);
				prep2.execute();
				prep2.close();*/
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
			closeConnection(con);
			if(retryCnt ==0) {
				return new DBResult<List<Message>>(messages, false);
			}else {
				return getMessageOfUser(username, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<List<Message>>(messages, true);
	}

}
