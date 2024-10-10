package cse360Project;

public class UserTest {
    public static void main(String[] args) {
        try {
            // Test 1: Create a User
            Role role = new Role(); // Create a role
            role.assignRole("student");
            User user = new User("testEmail@example.com", "testUser", true, "John", "Doe", role, "TestPassword1!");

            // Test 2: Validate password
            System.out.println("Test 2: Validate Password");
            boolean isValidPassword = user.validatePassword("TestPassword1!");
            System.out.println("Password is valid: " + isValidPassword);  // Expected output: true

            // Test 3: Role assignment
            System.out.println("Test 3: Role Assignment");
            user.addRole(role); // Add role
            System.out.println("Role assigned: " + user.role.roleName);  // Expected output: "student"

            // Test 4: Remove role
            System.out.println("Test 4: Remove Role");
            user.removeRole();
            System.out.println("Role after removal: " + (user.role == null ? "None" : user.role.roleName));  // Expected output: "None"

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
