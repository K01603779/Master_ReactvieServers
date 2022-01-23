package entities;

public class User {

	private int userID;
	String email;
	String firstName,lastName;
	private String address;
	String password;
	String creditCard;
	
	public String getFirstname(){
		return firstName;
	}
	public String getLastName(){
		return lastName;
	}
	void setFirstName(String firstName){
		this.firstName=firstName;
	}
	public String getAddress() {
		return address;
	}
	void setAddress(String address) {
		this.address = address;
	}
	
	public String getEmail(){
		return email;
	}
	void setEmail(String email){
		this.email=email;
	}
	public String getPassword(){
		return password;
	}
	public int getUserID(){
		return userID;
	}
	void setPassword(String password){
		this.password=password;
	}
	public void setID(int id) {
		this.userID=id;
	}
	public void setCreditCard(String card) {
		this.creditCard=card;
	}
	public String getCreditCard() {
		return creditCard;
	}
	
	public User(int userID,String email, String firstName, String lastName, String address, String password,String creditCard){
		this.userID=userID;
		this.email=email;
		this.firstName=firstName;
		this.lastName=lastName;
		this.address=address;
		this.password=password;
		this.creditCard=creditCard;
	}
	
	
}
