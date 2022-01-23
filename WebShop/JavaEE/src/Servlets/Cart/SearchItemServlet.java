package Servlets.Cart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import DBConnection.ResponeState;
import entities.Item;

@WebServlet(description = "Cart Servlet Search Item", urlPatterns = { "/SearchItemServlet" })
public class SearchItemServlet extends CartServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	private void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
		setAccessControlHeaders(response);
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		if (searchItem(request)) {
			json.put("State", ResponeState.Success);
			HttpSession session = request.getSession(false);
			LinkedList<Item> list = (LinkedList<Item>) session.getAttribute("Search");
			json.put("search", list);
		} else {
			json.put("State", ResponeState.Failure);

		}
		out.print(json.toString());
		out.flush();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		search(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		search(request, response);
	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	private void setAccessControlHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
	}
}
