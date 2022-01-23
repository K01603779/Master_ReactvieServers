package Servlets.Transactions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import DBConnection.DBCart;
import DBConnection.DBTransaction;
import DBConnection.DBResult;
import DBConnection.ResponeState;
import Mocking.CheckCard;
import entities.ShoppingCart;
import entities.User;

@WebServlet(description = "Transaction Servlet", urlPatterns = { "/CheckOutServlet" })
public class CheckOutServletDB extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doCheckOut(request, response);
	}

	private void doCheckOut(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/json");
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		boolean result = checkOut(request, response);
		if (result == true) {
			json.put("State", ResponeState.Success);
		} else {
			json.put("State", ResponeState.Failure);
		}
		out.print(json.toString());
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doCheckOut(request, response);
	}

	private boolean checkOut(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			User user = (User) session.getAttribute("user");
			if (user != null) {
				DBResult<ShoppingCart> dbCart = DBCart.getCart(user, 3);
				ShoppingCart cart = dbCart.result;
				if (dbCart.success && cart != null) {
					if (CheckCard.isValidCard(user.getCreditCard())) {
						if (DBTransaction.checkOut(user, cart, 3)) {
							if (DBCart.deleteCart(cart, 3)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}
