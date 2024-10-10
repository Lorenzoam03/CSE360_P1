package cse360Project;

import java.util.ArrayList;
import java.util.List;

public class Session {
	private static Session instance;
	private int userId;
	private String email;
	private String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private String preferredName;
	private List<String> roles;
	
	
	// empty constructor
	private Session() 		
	{
		roles = new ArrayList<>();
	}
	
	private Session(int userId, String username) 
	{
		this.userId = userId;
		this.username = username;
	}
	
	public static Session getInstance()
	{
		if(instance == null)
		{
			instance = new Session();
		}
		return instance;
	}

	
	// method to clear session
	public void clear()
	{
		this.userId = -1;
		this.username = null;
		this.firstName = null;
		this.middleName = null;
		this.lastName = null;
		this.preferredName = null;
		this.roles = null;
		this.email = null;
	}
	
	// getters and setters

	public void setUser(int userId, String username, List<String> roles)
	{
		this.userId = userId;
		this.username = username;
		this.roles = roles;
	}
	// Set user information (including email)
		public void setUser(int userId, String username, String email, List<String> roles) {
			this.userId = userId;
			this.username = username;
			this.email = email; // Store the email when setting user
			this.roles = roles;
		}
	
	public void setUser(int userId, String username, String email, String firstName, String lastName, String preferredName, List<String> roles) {
	    this.userId = userId;
	    this.username = username;
	    this.email = email;
	    this.firstName = firstName != null ? firstName : "";
	    this.lastName = lastName != null ? lastName : "";
	    this.preferredName = preferredName != null ? preferredName : "";
	    this.roles = roles != null ? roles : null;
	}
	// Getters and setters for email
		public String getEmail() {
			return this.email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	
	public int getUserId()
	{
		return this.userId;
	}
	
	public void setUserId(int userId)
	{
		this.userId = userId;
	}
	
	public String getUsername()
	{
		return this.username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getFirstName()
	{
		return this.firstName;
	}
	
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	public String getMiddleName()
	{
		return this.middleName;
	}
	
	public void setMiddleName(String middleName)
	{
		this.middleName = middleName;
	}
	
	public String getLastName()
	{
		return this.lastName;
	}
	
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	public String getPreferredName()
	{
		return this.preferredName;
	}
	
	public void setPreferredName(String preferredName)
	{
		this.preferredName = preferredName;
	}
	
	public List<String> getRoleNames()
	{
		return this.roles;
	}
	
	public void setRoleNames(List<String> role)
	{
		this.roles = role;
	}
}