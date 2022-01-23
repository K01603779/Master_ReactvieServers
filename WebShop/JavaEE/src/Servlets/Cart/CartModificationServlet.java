package Servlets.Cart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import DBConnection.DBCart;
import DBConnection.DBItem;
import DBConnection.DBResult;
import DBConnection.ResponeState;
import entities.CartEntry;
import entities.Item;
import entities.ShoppingCart;
import entities.User;

@WebServlet(description = "Cart Modification Servlet", urlPatterns = { "/CartModificationServlet" })
public class CartModificationServlet extends CartServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	private void modify(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		ShoppingCart cart;
		if (user == null) {
			modifyCart(request, response);
			cart = (ShoppingCart) session.getAttribute("cart");
		} else {
			cart = modifyCartDB(request, response, user);
		}
		if (cart != null) {
			json.put("State", ResponeState.Success);
			ArrayList<CartEntry> list = (ArrayList<CartEntry>) cart.getList();
			json.put("cart", list);
		} else {
			json.put("State", ResponeState.Failure);
		}
		out.print(json.toString());
		out.flush();
	}

	private ShoppingCart modifyCartDB(HttpServletRequest request, HttpServletResponse response, User user) {
		int itemid = Integer.parseInt(request.getParameter("itemid"));
		int amount = Integer.parseInt(request.getParameter("amount"));
		DBResult<Item> dbItem = DBItem.getItemByID(itemid, 3);
		if (dbItem.success) {
			Item item = dbItem.result;
			boolean add = Boolean.parseBoolean(request.getParameter("add"));
			if (item != null && user != null) {
				DBResult<ShoppingCart> dbCart = DBCart.getCart(user, 3);
				if (dbCart.success) {
					ShoppingCart cart = dbCart.result;
					if (cart == null) {
						cart = DBCart.createCart(user, 3).result;
						if (cart == null) {
							return null;
						}
					}
					CartEntry entry = cart.list.get(itemid);
					if (entry == null) {
						entry = new CartEntry(item, amount);
					} else {
						if (add) {
							entry.amount += amount;
						} else {
							entry.amount -= amount;
						}
					}
					if (entry.amount <= 0) {
						cart = DBCart.removeEntry(entry, cart, 3).result;
					} else {
						cart = DBCart.modifyCartEntry(entry, cart, 3).result;
					}
					return cart;
				}
			}
		}
		return null;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		modify(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		modify(request, response);
	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	private void setAccessControlHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
	}
}
