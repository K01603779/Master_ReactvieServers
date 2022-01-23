package Entities;

public class User extends Respondent {

	public String password;

	public User(String username, String password) {
		this.id = username;
		this.password = password;
		this.type = RespondentType.User;
	}

}
