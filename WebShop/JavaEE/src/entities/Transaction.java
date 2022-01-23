package entities;

import java.sql.Date;
import java.util.LinkedList;

public class Transaction {
	int transactionID;
	int userID;
	Date date;
	LinkedList<TransactionEntry> entries;
	
	public int getUserID() {
		return userID;
	}
	public Date getDate() {
		return date;
	}
	public int getTranscationID() {
		return transactionID;
	}
	public LinkedList<TransactionEntry> getEntries(){
		return entries;
	}
	public void setList(LinkedList<TransactionEntry> list) {
		this.entries=list;
	}
	public Transaction(int transactionID,int userID, Date date) {
		this.transactionID=transactionID;
		this.userID=userID;
		this.date=date;
	}
}
