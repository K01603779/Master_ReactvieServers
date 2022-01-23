package Entities;

import java.util.List;

public class Group extends Respondent {
    
    public String manager;
    List<String> accepted;
    List<String> requested;
    public Group(String username,String manager) {
    	this.id=username;
        this.manager=manager;
        this.type = RespondentType.Group;
    }
}
