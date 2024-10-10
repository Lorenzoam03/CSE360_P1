package cse360Project;

import cse360Project.User;

public class Role {

    public String roleName;

    // Assign a role to a user
    public void assignRoleToUser(User user, String role) {
        switch(role.toLowerCase()) {
            case "instructor":
                this.roleName = "Instructor";
                break;
            case "professor":
                this.roleName = "Professor";
                break;
            case "student":
                this.roleName = "Student";
                break;
            default:
                this.roleName = "Undefined";
                break;
        }
        user.addRole(this);  // Use the User object to assign the role
    }

    public void removeRoleFromUser(User user) {
        this.roleName = "Undefined";
        user.removeRole();  // Use the User object to remove the role
    }
}
