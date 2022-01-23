package DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import entities.User;

public  final class DBUser extends ConnectionPool {
	
	public static User getUser(String email, String password,int retryCnt) {
		User user = null;
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from users WHERE email = ? AND password = ?");
			prep.setString(1, email);
			prep.setString(2, password);
			ResultSet rs = prep.executeQuery();
			if (rs.next()) {
				user = mapUser(rs);
			}
			rs.close();
		} catch (Exception e) {
			closeConnection(con);
			System.out.println("getUser from DB resulted in error. Retry "+ retryCnt +" times");
			if(retryCnt ==0) {
				return null;
			}else {
				return getUser(email, password, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return user;
	}

	public static int createUser(User user,int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement(
					"INSERT INTO users (firstName, lastName, password, email, address,creditCard) VALUES (?,?,?,?,?,?)");
			prep.setString(1, user.getFirstname());
			prep.setString(2, user.getLastName());
			prep.setString(3, user.getPassword());
			prep.setString(4, user.getEmail());
			prep.setString(5, user.getAddress());
			prep.setString(6, user.getCreditCard());
			prep.execute();
			prep.close();
			prep = con.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rset = prep.executeQuery();
			if (rset.next()) {
				int id = rset.getInt("LAST_INSERT_ID()");
				System.out.println("created userID " + id);
				return id;
			}
		} catch (Exception e) {
			System.out.println("create User resulted in Erro retry " +retryCnt +" times");
			closeConnection(con);
			if(retryCnt==0) {
				return -1;
			}else {
				return createUser(user, retryCnt-1);
			}
		} finally {
			closeConnection(con);
		}
		return -1;
	}

	private static User mapUser(ResultSet rs) throws SQLException {
		int userID = rs.getInt(1);
		String firstName = rs.getString(2);
		String lastName = rs.getString(2);
		String password = rs.getString(3);
		String email = rs.getString(4);
		String address = rs.getString(5);
		String creditCard= rs.getString(6);
		return new User(userID, email, firstName, lastName, address, password,creditCard);
	}
}
