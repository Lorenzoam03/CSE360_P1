package cse360Project;

public class DatabaseHelperTest {
    public static void main(String[] args) {
        DatabaseHelper dbHelper = new DatabaseHelper();
        
        try {
            dbHelper.connectToDatabase();  // Connect to the DB
            System.out.println("Connected to the database.");

            // Test 1: Register a user
            System.out.println("Test 1: Register a User");
            dbHelper.register("dbTestUser", "TestPassword123!", "student");

            // Test 2: Login a user
            System.out.println("Test 2: Login a User");
            boolean loginSuccess = dbHelper.login("dbTestUser", "TestPassword123!");
            System.out.println("Login success: " + loginSuccess);  // Expected output: true

            dbHelper.closeConnection();  // Close DB connection
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
