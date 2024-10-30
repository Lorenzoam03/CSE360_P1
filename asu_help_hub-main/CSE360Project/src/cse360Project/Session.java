package cse360Project;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Session Class </p>
 * 
 * <p> Description: A session class which keeps track of the current session and which user is logged in.  </p>
 * 
 * <p> Copyright: Carlos Hernandez © 2024 </p>
 * 
 * @author Lorenzo Martinez
 * 
 * @version 1.0.0	2024-10-09 Updated for Phase 1
 * 
 */

public class Session {
	/**********************************************************************************************

	Attributes
	
	**********************************************************************************************/
	private static Session instance;
	private int userId;
	private String email;
	private String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private String preferredName;
	private List<String> roles;
	private List<String> invitedRoles;
	private boolean OTPUsed;
    private User user;

	
	/**********************************************************************************************

	Constructors
	
	**********************************************************************************************/
	
	// empty constructor
	private Session() 		
	{
		roles = new ArrayList<>();
	}
	
	// constructor with 'guaranteed values'
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

	/**********************************************************************************************

	Methods
	
	**********************************************************************************************/
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
	

	/**********************************************************************************************

	Getters and Setters
	
	**********************************************************************************************/
	// 'set' multiple values when getting them. Either the required ones, or the ones when they update with additional details
	// required details set user session
	public void setUser(int userId, String username, List<String> roles)
	{
		this.userId = userId;
		this.username = username;
		this.roles = roles;
		this.user = user;
	}
	
	// additional details set user session
	public void setUser(int userId, String username, String email, String firstName, String lastName, String preferredName, List<String> roles) {
	    this.userId = userId;
	    this.username = username;
	    this.email = email;
	    this.firstName = firstName != null ? firstName : "";
	    this.lastName = lastName != null ? lastName : "";
	    this.preferredName = preferredName != null ? preferredName : "";
	    this.roles = roles != null ? roles : null;
	}
	public boolean hasRole(String roleName) {
	    return roles != null && roles.contains(roleName.toLowerCase());
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

	public List<String> getRoleNames() {
	    if (roles == null) {
	        return new ArrayList<>(); // prevents a iterable error when this is null 
	    }
	    return roles;
	}
	
	public void setRoleNames(List<String> role)
	{
		this.roles = role;
	}
	

    public List<String> getInvitedRoles() {
        return invitedRoles;
    }

    public void setInvitedRoles(List<String> invitedRoles) {
    	System.out.println("Session stored the following roles:");
    	for (var role : invitedRoles)
    	{
    		System.out.println(role);
    	}
        this.invitedRoles = invitedRoles;
    }

	public boolean getOTPUsed() {
		return OTPUsed;
	}

	public void setOTPUsed(boolean oTPJustUsed) {
		OTPUsed = oTPJustUsed;
	}
}
