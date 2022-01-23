package entities;

public class TransactionEntry {
	int transactionID;
	int amount;
	Item item;
	
	public int getAmount() {
		return amount;
	}
	public int getTransactionID() {
		return transactionID;
	}
	public TransactionEntry(int transactionID, Item item, int amount) {
		this.transactionID=transactionID;
		this.item=item;
		this.amount=amount;
	}
	public Item getItem() {
		return item;
	}
	public void setAmount(int amount) {
		this.amount=amount;
	}
}
