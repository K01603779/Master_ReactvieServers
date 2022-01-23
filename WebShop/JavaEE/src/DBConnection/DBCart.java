package DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import entities.CartEntry;
import entities.Item;
import entities.ShoppingCart;
import entities.User;

public final class DBCart extends ConnectionPool {

	public static DBResult<ShoppingCart> getCart(User user, int retryCnt) {
		ShoppingCart cart = null;
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("SELECT * FROM carts where userID = ?");
			prep.setInt(1, user.getUserID());
			ResultSet rs = prep.executeQuery();
			cart = getCartDB(rs, user);
			if (cart != null) {
				prep = con.prepareStatement(
						"SELECT * FROM cartEntries,items where cartID = ? && cartEntries.itemID = items.itemID ");
				prep.setInt(1, cart.getCartID());
				ResultSet rs2 = prep.executeQuery();
				cart.list = mapEntries(rs2);
				rs2.close();
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("getCartFromUser resulted in Error retry " + retryCnt + "times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<ShoppingCart>(cart, false);
			} else {
				return getCart(user, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return new DBResult<ShoppingCart>(cart, true);
	}

	private static HashMap<Integer, CartEntry> mapEntries(ResultSet rs) {
		HashMap<Integer, CartEntry> entries = new HashMap<Integer, CartEntry>();
		try {
			while (rs.next()) {
				CartEntry entry = mapEntry(rs);
				entries.put(entry.item.getItemID(), entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entries;
	}

	private static ShoppingCart getCartDB(ResultSet rs, User user) {
		ShoppingCart cart = null;
		try {
			if (rs.next()) {
				cart = mapCart(rs, user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cart;
	}

	private static CartEntry mapEntry(ResultSet set) throws SQLException {
		int amount = set.getInt("amount");
		Item i = DBItem.mapItem(set);
		CartEntry entry = new CartEntry(i, amount);
		return entry;
	}

	private static ShoppingCart mapCart(ResultSet set, User user) throws SQLException {
		int userID = set.getInt("userID");
		int cardID = set.getInt("cartID");
		if (userID == user.getUserID()) {
			return new ShoppingCart(cardID, user);
		} else {
			return null;
		}

	}

	private static boolean modifyEntry(CartEntry entry, ShoppingCart cart, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con
					.prepareStatement("replace into cartEntries (cartID, itemID,amount) values (?,?,?)");
			prep.setInt(1, cart.getCartID());
			prep.setInt(2, entry.item.getItemID());
			prep.setInt(3, entry.amount);
			prep.execute();
			// list = getList(rs,user);
		} catch (Exception e) {
			System.out.println("Modifing the Shoppingcart resulted in a Error retry " + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return false;
			} else {
				return modifyEntry(entry, cart, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static DBResult<ShoppingCart> modifyCartEntry(CartEntry entry, ShoppingCart cart, int retryCnt) {
		if (modifyEntry(entry, cart, retryCnt)) {
			return getCart(cart.getUser(), retryCnt);
		} else {
			return new DBResult<ShoppingCart>(null, false);
		}
	}

	private static boolean deleteEntry(CartEntry entry, ShoppingCart cart, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("delete from cartEntries where cartID = ? and itemID =?");
			prep.setInt(1, cart.getCartID());
			prep.setInt(2, entry.item.getItemID());
			prep.execute();
		} catch (Exception e) {
			System.out.println("delete CartEntry resulted in an Error retry " + retryCnt + "times");
			closeConnection(con);
			if (retryCnt == 0) {
				return false;
			} else {
				return deleteEntry(entry, cart, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static DBResult<ShoppingCart> removeEntry(CartEntry entry, ShoppingCart cart, int retryCnt) {
		if (deleteEntry(entry, cart, retryCnt)) {
			;
			return getCart(cart.getUser(), retryCnt);
		} else {
			return new DBResult<ShoppingCart>(null, false);
		}
	}

	public static DBResult<ShoppingCart> createCart(User user, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("insert into carts (userID) values (?)");
			prep.setInt(1, user.getUserID());
			prep.execute();
		} catch (Exception e) {
			System.out.println("createCart resulted in an Error retry" + retryCnt + " times");
			closeConnection(con);
			if (retryCnt == 0) {
				return new DBResult<ShoppingCart>(null, false);
			} else {
				return createCart(user, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return getCart(user, retryCnt);
	}

	public static boolean deleteCart(ShoppingCart cart, int retryCnt) {
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement prep = con.prepareStatement("delete from cartEntries where cartID = ?");
			prep.setInt(1, cart.getCartID());
			prep.execute();
			prep = con.prepareStatement("delete from carts where cartID = ?");
			prep.setInt(1, cart.getCartID());
			prep.execute();
		} catch (Exception e) {
			System.out.println("deleteCart resulted in an Error retry " + retryCnt + " times");
			if (retryCnt == 0) {
				return false;
			} else {
				return deleteCart(cart, retryCnt - 1);
			}
		} finally {
			closeConnection(con);
		}
		return true;
	}

	public static boolean updateCart(ShoppingCart cart, User user, int retryCnt) {
		DBResult<ShoppingCart> rest = getCart(user, retryCnt);
		if (rest.success) {
			ShoppingCart dbcart = rest.result;
			if (dbcart == null) {
				rest = createCart(user, retryCnt);
				if (rest.success) {
					cart = rest.result;
				} else {
					return false;
				}
			}
			for (CartEntry entry : cart.list.values()) {
				if (!modifyEntry(entry, dbcart, retryCnt)) {
					return false;
				}

			}
		} else {
			return false;

		}
		return true;

	}
}
