package Servlets.Logout;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import DBConnection.ResponeState;

/**
 * Servlet Tutorial - Servlet Example
 */
@WebServlet(description = "Logout Servlet", urlPatterns = { "/LogoutServlet" })
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected ResponeState logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
			return ResponeState.Success;
		} else {
			return ResponeState.Failure;
		}
	}
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void doLogOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		setAccessControlHeaders(response);
		ResponeState state = logout(request, response);
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		json.put("State", state.toString());		
		out.print(json.toString());
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("LogOutPost");
		doLogOut(request, response);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("LogOutPost");
		doLogOut(request, response);
	}
	
	private void setAccessControlHeaders(HttpServletResponse resp) {	
		resp.setHeader("Access-Control-Allow-Credentials" , "true");
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET");
	}
}
	

	

