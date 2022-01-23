package Servlets.Transactions;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DBConnection.DBTransaction;
import DBConnection.DBResult;
import entities.ShoppingCart;
import entities.Transaction;
import entities.TransactionEntry;
import entities.User;

public abstract class CheckOutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	protected int getTransactions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			User user = (User) session.getAttribute("user");
			if (user != null) {
				DBResult<LinkedList<Transaction>> res = DBTransaction.getTransactions(user.getUserID(), 3);
				if (res.success) {
					LinkedList<Transaction> list = res.result;
					// ConnectionPoolTransactions.getTransactions(user.getUserID());
					DBResult<LinkedList<TransactionEntry>> transactions;
					for (Transaction action : list) {
						transactions = DBTransaction.getEntries(action.getTranscationID(), 3);
						if (transactions.success) {
							action.setList(transactions.result);
						} else {
							return -1;
						}
					}
					session.setAttribute("transactions", list);
					return 0;
				}
			}
		}
		return -1;

	}

	protected int checkOut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			User user = (User) session.getAttribute("user");
			ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
			if (user == null) {
				return -2;
			} else if (cart == null) {
				return -3;
			} else if (cart.list.isEmpty()) {
				return -4;
			} else {
				boolean ret = DBTransaction.checkOut(user, cart, 3);
				if (ret) {
					session.setAttribute("cart", null);
					return 0;
				}
			}
		}
		return -1;
	}

}
