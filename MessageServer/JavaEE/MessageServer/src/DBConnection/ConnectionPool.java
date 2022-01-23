package DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class ConnectionPool {
	//@Resource(name = "jdbc/WarehouseDB")
	private static DataSource ds;

	public static Connection getConnection() throws NamingException, SQLException {
		Connection con = null;
		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:/comp/env");
		ds = (DataSource) envContext.lookup("jdbc/WarehouseDB");
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

	
	static ExecutorService es = Executors.newFixedThreadPool(88); //TODO 

	public static ExecutorService getExecutor() {
		return es;
	}
}
