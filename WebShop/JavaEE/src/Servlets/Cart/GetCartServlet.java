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
import DBConnection.ResponeState;
import entities.CartEntry;
import entities.ShoppingCart;
import entities.User;

@WebServlet(description = "Cart Modification Servlet", urlPatterns = { "/GetCartServlet" })
public class GetCartServlet extends CartServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}


	private void getCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setContentType("application/json");
			setAccessControlHeaders(response);
			PrintWriter out = response.getWriter();
			JSONObject json = new JSONObject();
			HttpSession session =request.getSession();
			User user =(User)session.getAttribute("user");
			ShoppingCart cart = null;
			if(user != null) {
				cart = DBCart.getCart(user,3).result;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getCart(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getCart(request, response);
	}
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	private void setAccessControlHeaders(HttpServletResponse resp) {	
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Credentials" , "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
	}

}
