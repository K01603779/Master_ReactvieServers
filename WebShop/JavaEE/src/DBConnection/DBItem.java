package DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import entities.Item;

public class DBItem extends ConnectionPool {

	public static DBResult<LinkedList<Item>> getAllItems(int retryCnt) {
		/*
		 * InitialContext ctx = new InitialContext(); /* Create a JNDI Initial context
		 * to be able to lookup the DataSource
		 *
		 * In production-level code, this should be cached as an instance or static
		 * variable, as it can be quite expensive to create a JNDI context.
		 *
		 * Note: This code only works when you are using servlets or EJBs in a J2EE
		 * application server. If you are using connection pooling in standalone Java
		 * code, you will have to create/configure datasources using whatever mechanisms
		 * your particular connection pooling library provides.
		 */
		// DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/MySQLDB");
		LinkedList<Item> results = new LinkedList<Item>();
		Connection con = null;
		try {
			con = getConnection();
			ResultSet rs = con.createStatement().executeQuery("SELECT * FROM items");
			results = getList(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("getAllItems resulted in an Error retry " + retryCnt + "times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<LinkedList<Item>>(null, false);
			} else {
				return getAllItems(retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<LinkedList<Item>>(results, true);
	}

	public static DBResult<LinkedList<Item>> getItems(String name, int retryCnt) {
		LinkedList<Item> list = null;
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from items where itemName LIKE ?");
			prep.setString(1, "%" + name + "%");
			ResultSet rs = prep.executeQuery();
			list = getList(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("getItemsByName resulted in an Error retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<LinkedList<Item>>(null, false);
			} else {
				return getItems(name, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<LinkedList<Item>>(list, true);

	}

	public static DBResult<Item> getItemByID(int id, int retryCnt) {
		Connection con = null;
		Item item = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from items where itemID = ?");
			prep.setInt(1, id);
			ResultSet rs = prep.executeQuery();
			rs.next();
			item = mapItem(rs);
			rs.close();
		} catch (Exception e) {
			System.out.println("getItemByID resulted in an Error retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<Item>(null, false);
			} else {
				return getItemByID(id, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<Item>(item, true);
	}

	public static LinkedList<Item> getList(ResultSet rs) {
		LinkedList<Item> list = new LinkedList<Item>();
		try {
			while (rs.next()) {
				list.add(mapItem(rs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static Item mapItem(ResultSet set) throws SQLException {
		int id = set.getInt("itemID");
		String itemName = set.getString("itemName");
		String itemDesc = set.getString("itemdesc");
		double price = set.getDouble("price");
		return new Item(id, itemName, itemDesc, price);

	}
}
