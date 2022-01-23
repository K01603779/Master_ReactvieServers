package Servlets.Transactions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import entities.Transaction;

@WebServlet(description = "Transaction Servlet", urlPatterns = { "/TransactionServlet" })
public class TransactionServlet extends CheckOutServlet {

	private static final long serialVersionUID = 1L;
	public void init() throws ServletException {
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
	private void transactions(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json");
		setAccessControlHeaders(response);
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		int result = getTransactions(request, response);
		if(result ==0 ) {
			HttpSession session =request.getSession(false);
			LinkedList<Transaction> list = (LinkedList<Transaction>) session.getAttribute("transactions");
			json.put("list", list);			
		}
		out.print(json.toString());
		out.flush();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		transactions(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		transactions(request, response);
	}

}
