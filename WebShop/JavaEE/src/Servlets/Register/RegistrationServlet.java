package Servlets.Register;

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
import DBConnection.DBUser;
import DBConnection.ResponeState;
import entities.ShoppingCart;
import entities.User;

@WebServlet(description = "Logout Servlet", urlPatterns = { "/RegisterServlet" })
public class RegistrationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	public int doRegister(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("user") != null) {
			return -1;
		} else {
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String address = request.getParameter("address");
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String card = request.getParameter("card");
			if (!(lastName.isBlank() || firstName.isBlank() || address.isBlank() || email.isBlank()
					|| password.isBlank() || card.isBlank())) {
				User user = new User(0, email, firstName, lastName, address, password, card);
				session = request.getSession();
				int id = DBUser.createUser(user, 3);
				if (id != -1) {
					user.setID(id);
					session.setAttribute("user", user);
					ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
					if (cart != null) {
						boolean success = DBCart.updateCart(cart, user, 3);
						if (success) {
							session.removeAttribute("cart");
						} else {
							return -2;
						}
					}
					return 0;
				}
				return -2;
			} else {
				return -2;
			}
		}
	}

	private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		int result = doRegister(request, response);
		// TODO add more response
		if (result == 0) {
			json.put("State", ResponeState.Success);
		} else if (result == -1) {
			json.put("State", ResponeState.Failure);
		} else if (result == -2) {
			json.put("State", ResponeState.Failure);
		}
		out.print(json.toString());
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		register(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		register(request, response);
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
