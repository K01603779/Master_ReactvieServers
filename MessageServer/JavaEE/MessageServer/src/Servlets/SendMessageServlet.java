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

@WebServlet(description = "Login Servlet", urlPatterns = { "/MessageServlet" })
public class SendMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected ResponeState postMessage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// get request parameters for userID and password

		// logging example
		HttpSession session = request.getSession(false);
		User user = null;
		String senderID = request.getParameter("senderID");
		String receiverID = request.getParameter("receiverID");
		String content = request.getParameter("content");
		int type = Integer.parseInt(request.getParameter("type"));
		if (session != null) {
			user = (User) session.getAttribute("user");
		} else {
			session = request.getSession();

			String pwd = request.getParameter("pwd");
			log("User=" + senderID + "::password=" + pwd);
			if (senderID != "" && pwd != "") {
				user = DBMessage.getUser(senderID, pwd, 3).result;

			}
		}
		if (user != null) {
			session.setAttribute("user", user);
			session.setMaxInactiveInterval(10 * 60);
			DBResult<List<Message>> dbResult = DBMessage.getMessageOfUser(user.id, 3);
			if (dbResult.success) {
				List<Message> list = dbResult.result;
				if (receiverID != "" && content != "") {
					Message m = new Message(user.id, receiverID, content, type);
					boolean result = DBMessage.storeMsg(m, 3);
					if (result) {
						session.setAttribute("messages", list);
						return ResponeState.Success;
					}
				}
			}

		}
		return ResponeState.Failure;

	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAccessControlHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	public void doMessage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setStatus(200);
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		ResponeState state = postMessage(request, response);
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
		doMessage(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doMessage(request, response);
	}

	private void setAccessControlHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
	}

}
