package Servlets.Login;

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

/**
 * Servlet Tutorial - Servlet Example
 */

@WebServlet(description = "Login Servlet", urlPatterns = { "/LoginServlet" })
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected ResponeState login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// get request parameters for userID and password
		String email = request.getParameter("user");
		String pwd = request.getParameter("pwd");
		log("User=" + email + "::password=" + pwd);
		if (email != "" && pwd != "") {
			User user = DBUser.getUser(email, pwd, 3);
			if (user != null) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				session.setMaxInactiveInterval(10 * 60);
				ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
				if (cart != null) {
					boolean rest = DBCart.updateCart(cart, user, 3);
					if (rest) {
						session.removeAttribute("cart");
					} else {
						return ResponeState.Failure;
					}
				}
				return ResponeState.Success;
			}
		}
		return ResponeState.Failure;
	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setStatus(200);
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		ResponeState state = login(request, response);
		JSONObject json = new JSONObject();
		json.put("State", state.toString());
		if (state == ResponeState.Success) {
			json.put("SessionID", request.getSession().getId());
		}
		out.print(json.toString());
		out.flush();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doLogin(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doLogin(request, response);
	}

	private void setAccessControlHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
	}

}