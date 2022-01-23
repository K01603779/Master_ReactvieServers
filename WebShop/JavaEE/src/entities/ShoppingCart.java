package entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShoppingCart {
	private int cartID;
	public HashMap<Integer,CartEntry> list= new HashMap<Integer, CartEntry>();
	public User user;
	
	public List<CartEntry> getList() {
		return new ArrayList<CartEntry>(list.values());
	}
	public User getUser() {
		return user;
	}
	public ShoppingCart(int id,User user) {
		this.cartID=id;
		this.user=user;
	}
	public ShoppingCart() {
		
	}
	public int getCartID() {
		return cartID;
	}
	
}
