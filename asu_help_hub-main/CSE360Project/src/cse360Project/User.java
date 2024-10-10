package cse360Project;

import cse360Project.Role;

public class User {
    // user attributes
    public String email;
    public String username;
    public Boolean oneTimeFlag;
    public String firstName;
    public String middleName;
    public String lastName;
    public String preferredName;
    public Role role;  // Role object used for role management
    public Password password;

    // constructor with all values including password
    public User(String email, String username, Boolean oneTimeFlag, String firstName, String middleName, String lastName, String preferredName, Role role1, String password) throws Exception {
        this.email = email;
        this.username = username;
        this.oneTimeFlag = oneTimeFlag;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.role = role1;
        this.preferredName = preferredName;
        this.password = new Password(password);  // Hash and store the password
    }

    // constructor in case student with no middle name and no preferred name
    public User(String email, String username, Boolean oneTimeFlag, String firstName, String lastName, Role role1, String password) throws Exception {
        this.email = email;
        this.username = username;
        this.oneTimeFlag = oneTimeFlag;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role1;
        this.password = new Password(password);  // Hash and store the password
    }

    // constructor with no middle name but preferred name
    public User(String email, String username, Boolean oneTimeFlag, String firstName, String lastName, String preferredName, Role role1, String password) throws Exception {
        this.email = email;
        this.username = username;
        this.oneTimeFlag = oneTimeFlag;
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.role = role1;
        this.password = new Password(password);  // Hash and store the password
    }

    // Add role to the user
    public void addRole(Role role1) {
        this.role = role1;
        System.out.println("Role " + role1.roleName + " assigned to user " + this.username);
    }

    // Remove the current role from the user
    public void removeRole() {
        this.role = null;
        System.out.println("Role removed from user " + this.username);
    }

    // Get the current role of the user
    public String getRole() {
        return this.role != null ? this.role.roleName : "No role assigned";
    }

    // Validate password method
    public boolean validatePassword(String inputPassword) throws Exception {
        return this.password.verifyPassword(inputPassword);
    }

	public void addRole1(Role role1) {
		// TODO Auto-generated method stub
		
	}
}
