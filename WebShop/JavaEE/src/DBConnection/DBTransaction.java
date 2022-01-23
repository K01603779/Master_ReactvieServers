package DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import entities.CartEntry;
import entities.Item;
import entities.ShoppingCart;
import entities.Transaction;
import entities.TransactionEntry;
import entities.User;

public class DBTransaction extends ConnectionPool {

	public static DBResult<LinkedList<Transaction>> getTransactions(int userID, int retryCnt) {
		LinkedList<Transaction> list = new LinkedList<Transaction>();
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("select * from transactions  where userID = ?");
			prep.setInt(1, userID);
			ResultSet rset = prep.executeQuery();
			Date date;
			int transactionID;
			while (rset.next()) {
				transactionID = rset.getInt("transactionID");
				date = rset.getDate("orderDate");
				list.add(new Transaction(transactionID, userID, date));
			}
			rset.close();
			prep.close();
		} catch (Exception e) {
			System.out.println("getTransactions resulted in an Error retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<LinkedList<Transaction>>(null, false);
			} else {
				return getTransactions(userID, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<LinkedList<Transaction>>(list, true);
	}

	public static DBResult<LinkedList<TransactionEntry>> getEntries(int transactionID, int retryCnt) {
		LinkedList<TransactionEntry> list = new LinkedList<TransactionEntry>();
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement(
					"select * from transactionEntries,items  where transactionID = ? and transactionEntries.itemID = items.itemID");
			prep.setInt(1, transactionID);
			ResultSet rset = prep.executeQuery();
			int itemID, amount;
			String itemName, itemDescription;
			double price;
			while (rset.next()) {
				itemID = rset.getInt("itemID");
				amount = rset.getInt("amount");
				itemName = rset.getString("itemName");
				itemDescription = rset.getString("itemdesc");
				price = rset.getDouble("amount");
				Item item = new Item(itemID, itemName, itemDescription, price);
				list.add(new TransactionEntry(transactionID, item, amount));
			}
			rset.close();
			prep.close();
		} catch (Exception e) {
			System.out.println("getEntries resulted in an Error retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<LinkedList<TransactionEntry>>(null, false);
			} else {
				return getEntries(transactionID, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<LinkedList<TransactionEntry>>(list, true);
	}

	public static boolean checkOut(User user, ShoppingCart cart, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			con.setAutoCommit(false);
			PreparedStatement prep = con
					.prepareStatement("insert into  transactions (userID, orderDate) values (?,?);");
			prep.setInt(1, user.getUserID());
			prep.setDate(2, new Date(System.currentTimeMillis()));
			prep.execute();
			prep.close();
			prep = con.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rset = prep.executeQuery();
			rset.next();
			int transcationID = rset.getInt("LAST_INSERT_ID()");
			for (CartEntry entry : cart.list.values()) {
				prep = con.prepareStatement(
						"INSERT INTO transactionEntries (transactionID,itemID,amount) VALUES (?,?,?);");
				prep.setInt(1, transcationID);
				prep.setInt(2, entry.item.getItemID());
				prep.setInt(3, entry.amount);
				prep.execute();
				prep.close();
			}
			con.commit();
			rset.close();
		} catch (Exception e) {
			System.out.println("checkOut resulted in an Erorr retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return false;
			} else {
				return checkOut(user, cart, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}
}
