package Servlets.Cart;

import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DBConnection.DBItem;
import DBConnection.DBResult;
import DBConnection.ResponeState;
import entities.CartEntry;
import entities.Item;
import entities.ShoppingCart;

public abstract class CartServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected boolean searchItem(HttpServletRequest request) {
		String item = request.getParameter("item");
		DBResult<LinkedList<Item>> dbList;
		HttpSession session = request.getSession();
		if (item != null && !item.equals("")) {
			dbList = DBItem.getItems(item, 3);
			if (dbList.success) {
				session.setAttribute("Search", dbList.result);
				return true;
			}
		} else {
			dbList = DBItem.getAllItems(3);
			if (dbList.success) {
				session.setAttribute("Search", dbList.result);
				return true;
			}
		}
		return false;
	}

	protected ResponeState modifyCart(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		int itemid = Integer.parseInt(request.getParameter("itemid"));
		int amount = Integer.parseInt(request.getParameter("amount"));
		DBResult<Item> dbItem = DBItem.getItemByID(itemid, 3);
		if (dbItem.success) {
			Item item = dbItem.result;
			if (item != null) {
				if (request.getParameter("add") != null) {
					addEntry(item, session, amount);
				} else if (request.getParameter("remove") != null) {
					removeEntry(item, session, amount);
				}
				return ResponeState.Success;
			}
		}
		return ResponeState.Failure;
	}

	// TODO if logged or not add amount by number
	private void addEntry(Item item, HttpSession session, int amount) {
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
		if (cart == null) {
			cart = new ShoppingCart();
			CartEntry entry = new CartEntry();
			entry.amount = amount;
			entry.item = item;
			cart.list.put(item.getItemID(), entry);
		} else {
			CartEntry entry = cart.list.get(item.getItemID());
			if (entry != null) {
				entry.amount = entry.amount + amount;
			} else {
				entry = new CartEntry();
				entry.amount = amount;
				entry.item = item;
			}
			cart.list.put(item.getItemID(), entry);
		}
		session.setAttribute("cart", cart);

	}

	private void removeEntry(Item item, HttpSession session, int amount) {
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
		if (cart != null) {
			CartEntry entry = cart.list.get(item.getItemID());
			if (entry != null) {
				entry.amount = entry.amount - amount;
				if (entry.amount <= 0) {
					cart.list.remove(item.getItemID());
				} else {
					cart.list.put(item.getItemID(), entry);
				}
			}
			session.setAttribute("cart", cart);
		}
	}
}
