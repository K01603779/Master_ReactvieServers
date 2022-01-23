package DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class ConnectionPool {
	
	public static Connection getConnection() throws NamingException, SQLException {
		Connection con = null;
		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource) envContext.lookup("jdbc/WarehouseDB");
		con = ds.getConnection();
		return con;
	}
	public static void closeConnection(Connection connection) {
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
}
