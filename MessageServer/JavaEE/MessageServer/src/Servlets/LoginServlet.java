package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import DBConnection.DBMessage;
import DBConnection.DBResult;
import DBConnection.ResponeState;
import Entities.Message;
import Entities.User;

@WebServlet(description = "Login Servlet", urlPatterns = { "/LoginServlet" })
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected ResponeState Login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// get request parameters for userID and password
		String username = request.getParameter("user");
		String pwd = request.getParameter("pwd");
		// logging example
		log("User=" + username + "::password=" + pwd);
		if (username != "" && pwd != "") {
			User user = DBMessage.getUser(username, pwd, 3).result; 
			if (user != null) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				session.setMaxInactiveInterval(10 * 60);
				DBResult<List<Message>> result = DBMessage.getMessageOfUser(username, 3);
				if (result.success) {
					List<Message> list = result.result;
					list.add(new Message("DEmo", "Hugo", "DemoMessage Test", 1));
					session.setAttribute("messages", list);
					return ResponeState.Success;
				}
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
		ResponeState state = Login(request, response);
		JSONObject json = new JSONObject();

		json.put("State", state.toString());
		if (state == ResponeState.Success) {
			json.put("SessionID", request.getSession().getId());
			List<Message> message = (List<Message>) request.getSession().getAttribute("messages");
			JSONArray array = new JSONArray(message);
			json.put("messages", array);
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