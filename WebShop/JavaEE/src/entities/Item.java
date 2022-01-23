package entities;



public class Item {

	private int itemID;
	private String itemName;
	private String itemDescription;
	private double price;
	
	public Item(int itemID,String itemName,String itemDescription,double price){
		this.itemID=itemID;
		this.setItemDescription(itemDescription);
		this.setItemName(itemName);
		this.setPrice(price);
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getItemID() {
		return itemID;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
