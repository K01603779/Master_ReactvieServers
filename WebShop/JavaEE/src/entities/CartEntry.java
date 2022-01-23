package entities;

public class CartEntry {
	public Item item;
	public int amount;
	public ShoppingCart cart;
	public CartEntry(){
		
	}
	public Item getItem() {
		return item;
	}
	public int getAmount() {
		return amount;
	}
	
	public CartEntry(Item i,int amount) {
		this.item=i;
		this.amount=amount;
	}
}
