package cse360Project;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.sql.ResultSet;

class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/firstDatabase;AUTO_SERVER=TRUE";

    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName(JDBC_DRIVER); 
            System.out.println("Connecting to database...");
            
            // Establish the connection
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 

            // Create the necessary tables if they don't exist
            createTables(); 
        } catch (ClassNotFoundException e) {
            // Handle the case where the JDBC driver is not found
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Handle any SQL errors that occur during the connection process
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }

    // Method to create the necessary tables in the database
    private void createTables() throws SQLException {
        // Create users table
        String userTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "email VARCHAR(255) UNIQUE, "
                + "username VARCHAR(255) UNIQUE NOT NULL, "
                + "oneTimeFlag BOOLEAN, "
                + "firstName VARCHAR(255), "
                + "middleName VARCHAR(255), "
                + "lastName VARCHAR(255), "
                + "preferredName VARCHAR(255), "
                + "hashedPassword VARCHAR(255) NOT NULL, "
                + "randSalt VARCHAR(255) NOT NULL)";
        statement.execute(userTable);
        
        // Create roles table
        String rolesTable = "CREATE TABLE IF NOT EXISTS roles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "role_name VARCHAR(20) UNIQUE NOT NULL)";
        statement.execute(rolesTable);
        
        // Create invitations table
        String invitationsTable = "CREATE TABLE IF NOT EXISTS invitations ("
                + "invite_code VARCHAR(255) PRIMARY KEY, "
                + "role_id INT NOT NULL, "
                + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE)";
        statement.execute(invitationsTable);


        // Create user_roles join table
        String userRolesTable = "CREATE TABLE IF NOT EXISTS user_roles ("
                + "user_id INT NOT NULL, "
                + "role_id INT NOT NULL, "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE, "
                + "PRIMARY KEY (user_id, role_id))";
        statement.execute(userRolesTable);

        // Insert default roles
        insertDefaultRoles();
    }

    // Method to insert default roles into the roles table
    private void insertDefaultRoles() throws SQLException {
        // Insert Admin role if not exists
        String insertAdmin = "INSERT INTO roles (role_name) "
                + "SELECT * FROM (SELECT 'admin') AS tmp "
                + "WHERE NOT EXISTS (SELECT role_name FROM roles WHERE role_name = 'admin') LIMIT 1";
        statement.execute(insertAdmin);

        // Insert Student role if not exists
        String insertStudent = "INSERT INTO roles (role_name) "
                + "SELECT * FROM (SELECT 'student') AS tmp "
                + "WHERE NOT EXISTS (SELECT role_name FROM roles WHERE role_name = 'student') LIMIT 1";
        statement.execute(insertStudent);

        // Insert Instructor role if not exists
        String insertInstructor = "INSERT INTO roles (role_name) "
                + "SELECT * FROM (SELECT 'instructor') AS tmp "
                + "WHERE NOT EXISTS (SELECT role_name FROM roles WHERE role_name = 'instructor') LIMIT 1";
        statement.execute(insertInstructor);
    }

    // Method to store an invitation code and associated role
    public void storeInvite(String inviteCode, String role) throws SQLException {
        // Get role ID from the roles table
        String getRoleIdQuery = "SELECT id FROM roles WHERE role_name = ?";
        int roleId;

        try (PreparedStatement getRoleIdStmt = connection.prepareStatement(getRoleIdQuery)) {
            getRoleIdStmt.setString(1, role);
            try (ResultSet rs = getRoleIdStmt.executeQuery()) {
                if (rs.next()) {
                    roleId = rs.getInt("id");
                } else {
                    throw new SQLException("Role not found: " + role);
                }
            }
        }

        // Insert invitation code and role ID into the invitations table
        String insertInviteQuery = "INSERT INTO invitations (invite_code, role_id) VALUES (?, ?)";
        try (PreparedStatement insertInviteStmt = connection.prepareStatement(insertInviteQuery)) {
            insertInviteStmt.setString(1, inviteCode);
            insertInviteStmt.setInt(2, roleId);
            insertInviteStmt.executeUpdate();
        }
    }

    // Method to validate an invitation code
    public boolean validateInvitationCode(String inviteCode) throws SQLException {
        String query = "SELECT * FROM invitations WHERE invite_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, inviteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if the invitation code exists
            }
        }
    }

    // Method to complete registration with an invitation code
    public void completeRegistrationWithInvite(String username, String email, String firstName, String lastName, String password, String inviteCode) throws Exception {
        String getInviteQuery = "SELECT role_id FROM invitations WHERE invite_code = ?";
        String deleteInviteQuery = "DELETE FROM invitations WHERE invite_code = ?";

        int roleId;

        try {
            connection.setAutoCommit(false); // Start transaction

            // Get the role ID associated with the invitation code
            try (PreparedStatement getInviteStmt = connection.prepareStatement(getInviteQuery)) {
                getInviteStmt.setString(1, inviteCode);
                try (ResultSet rs = getInviteStmt.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("role_id");
                    } else {
                        throw new Exception("Invalid invitation code.");
                    }
                }
            }

            // Register the user
            registerUserWithRoleId(username, email, password, firstName, lastName, roleId);

            // Invalidate the invitation code
            try (PreparedStatement deleteInviteStmt = connection.prepareStatement(deleteInviteQuery)) {
                deleteInviteStmt.setString(1, inviteCode);
                deleteInviteStmt.executeUpdate();
            }

            connection.commit(); // Commit transaction
        } catch (Exception e) {
            connection.rollback(); // Rollback transaction if something goes wrong
            throw e;
        } finally {
            connection.setAutoCommit(true); // Restore auto-commit mode
        }
    }

    // Helper method to register a user with a specific role ID
    private void registerUserWithRoleId(String username, String email, String password, String firstName, String lastName, int roleId) throws Exception {
        var passwd = new Password(password);
        String hashedPassword = Base64.getEncoder().encodeToString(passwd.getHashedPass());
        String randSalt = Base64.getEncoder().encodeToString(passwd.getSalt());

        String insertUser = "INSERT INTO users (username, email, hashedPassword, randSalt, firstName, lastName) VALUES (?, ?, ?, ?, ?, ?)";
        String getUserId = "SELECT id FROM users WHERE username = ?";
        String insertUserRole = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";

        // Insert user into users table
        try (PreparedStatement pstmtUser = connection.prepareStatement(insertUser)) {
            pstmtUser.setString(1, username);
            pstmtUser.setString(2, email);
            pstmtUser.setString(3, hashedPassword);
            pstmtUser.setString(4, randSalt);
            pstmtUser.setString(5, firstName);
            pstmtUser.setString(6, lastName);
            pstmtUser.executeUpdate();
        }

        // Get user ID
        int userId;
        try (PreparedStatement pstmtGetUserId = connection.prepareStatement(getUserId)) {
            pstmtGetUserId.setString(1, username);
            try (ResultSet rs = pstmtGetUserId.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("id");
                } else {
                    throw new Exception("User registration failed. User ID not found.");
                }
            }
        }

        // Insert into user_roles table
        try (PreparedStatement pstmtUserRole = connection.prepareStatement(insertUserRole)) {
            pstmtUserRole.setInt(1, userId);
            pstmtUserRole.setInt(2, roleId);
            pstmtUserRole.executeUpdate();
        }
    }

    // Method to delete the tables from the database
    public void dropTables() throws SQLException {

        String dropQuery = "DROP TABLE IF EXISTS user_topics";
        statement.executeUpdate(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS user_roles";
        statement.executeUpdate(dropQuery);

        // Now drop the users and roles tables
        dropQuery = "DROP TABLE IF EXISTS users";
        statement.executeUpdate(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS roles";
        statement.executeUpdate(dropQuery);

        System.out.println("Tables have been dropped.");
    }

    // Check if the database is empty
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    // Method to empty the database by deleting all records from the relevant table(s)
    public void emptyDatabase() throws SQLException {
        String deleteQuery = "DELETE FROM users";
        // Execute the delete query
        statement.executeUpdate(deleteQuery);

        deleteQuery = "DELETE FROM user_topics";
        statement.executeUpdate(deleteQuery);

        System.out.println("Database has been emptied.");
    }

    public void register(String username, String password, String role) throws Exception {
        var passwd = new Password(password);
        String hashedPassword = Base64.getEncoder().encodeToString(passwd.getHashedPass());
        String randSalt = Base64.getEncoder().encodeToString(passwd.getSalt());

        // Insert user into users table
        String insertUser = "INSERT INTO users (username, hashedPassword, randSalt) VALUES (?, ?, ?)";
        String getUserId = "SELECT id FROM users WHERE username = ?";
        String getRoleId = "SELECT id FROM roles WHERE role_name = ?";
        String insertUserRole = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false); // Start transaction

            // Insert user into users table
            try (PreparedStatement pstmtUser = connection.prepareStatement(insertUser)) {
                pstmtUser.setString(1, username);
                pstmtUser.setString(2, hashedPassword);
                pstmtUser.setString(3, randSalt);
                pstmtUser.executeUpdate();
            }

            // Get user ID
            int userId;
            try (PreparedStatement pstmtGetUserId = connection.prepareStatement(getUserId)) {
                pstmtGetUserId.setString(1, username);
                try (ResultSet rs = pstmtGetUserId.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        throw new Exception("User registration failed. User ID not found.");
                    }
                }
            }

            // Get role ID
            int roleId;
            try (PreparedStatement pstmtGetRoleId = connection.prepareStatement(getRoleId)) {
                pstmtGetRoleId.setString(1, role);
                try (ResultSet rs = pstmtGetRoleId.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("id");
                    } else {
                        throw new Exception("Invalid role provided.");
                    }
                }
            }

            // Insert into user_roles table
            try (PreparedStatement pstmtUserRole = connection.prepareStatement(insertUserRole)) {
                pstmtUserRole.setInt(1, userId);
                pstmtUserRole.setInt(2, roleId);
                pstmtUserRole.executeUpdate();
            }

            connection.commit(); // Commit transaction
        } catch (Exception e) {
            connection.rollback(); // Rollback transaction if something goes wrong
            throw e;
        } finally {
            connection.setAutoCommit(true); // Restore auto-commit mode
        }
    }

    // Additional methods such as login, etc.


	public boolean login(String username, String password) throws Exception {
	    String query = "SELECT * FROM users WHERE username = ?";
	    String getUserRoles = "SELECT r.role_name FROM roles r "
	                        + "JOIN user_roles ur ON r.id = ur.role_id "
	                        + "WHERE ur.user_id = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                // Retrieve the salt and hashed password from the database
	                String hashedPassword = rs.getString("hashedPassword");
	                String randSalt = rs.getString("randSalt");

	                // Use your password verification method to compare
	                boolean isValidPassword = Password.verifyPassword(password, 
	                                        Base64.getDecoder().decode(randSalt), 
	                                        Base64.getDecoder().decode(hashedPassword));

	                if (isValidPassword) {
	                    // Retrieve user details
	                    int userId = rs.getInt("id");
	                    String email = rs.getString("email");
	                    String firstName = rs.getString("firstName");
	                    String lastName = rs.getString("lastName");
	                    String preferredName = rs.getString("preferredName");

	                    // Fetch user roles from user_roles tablex
	                    List<String> roles = new ArrayList<>();
	                    try (PreparedStatement pstmtRoles = connection.prepareStatement(getUserRoles)) {
	                        pstmtRoles.setInt(1, userId);
	                        try (ResultSet rsRoles = pstmtRoles.executeQuery()) {
	                            while (rsRoles.next()) {
	                                roles.add(rsRoles.getString("role_name"));
	                            }
	                        }
	                    }

	                    // Set user session
	                    if (firstName != null) {
	                        System.out.println("set up!");
	                        Session.getInstance().setUser(userId, username, email, firstName, lastName, preferredName, roles);
	                    } else {
	                        // Only guaranteed values
	                        System.out.println("missing set up");
	                        Session.getInstance().setUser(userId, username, roles);
	                    }
	                    return true; // Successful login
	                }
	            }
	        }
	    }
	    return false; // Invalid login
	}
	
	public boolean doesUserExist(String email) {
	    String query = "SELECT COUNT(*) FROM users WHERE email = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, email);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	public boolean updateUserById(int userId, String username, String firstName, String lastName, String preferredName) throws SQLException {
	    String selectQuery = "SELECT * FROM users WHERE id = ?";
	    
	    // Query to update user details
	    String updateQuery = "UPDATE users SET firstName = ?, lastName = ?, preferredName = ? WHERE id = ?";
	    
	    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
	        selectStmt.setInt(1, userId);
	        
	        try (ResultSet rs = selectStmt.executeQuery()) {
	            if (rs.next()) {
	                // User exists, proceed with updating their details
	                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
	                    updateStmt.setString(1, firstName);
	                    updateStmt.setString(2, lastName);
	                    updateStmt.setString(3, preferredName);
	                    updateStmt.setInt(4, userId);
	                    
	                    int rowsUpdated = updateStmt.executeUpdate();
	                    
	                    // Return true if update was successful
	                    return rowsUpdated > 0;
	                }
	            } else {
	                // User with the given ID dosnt exist
	                System.out.println("User with ID " + userId + " not found.");
	                return false;
	            }
	        }
	    }
	}

	
	public void displayUsersByUser() throws UnsupportedEncodingException, Exception {
	    String sql = "SELECT users.id, users.email, users.username, users.hashedPassword, users.randSalt, "
	               + "roles.role_name AS role, users.firstName "
	               + "FROM users "
	               + "LEFT JOIN user_roles ON users.id = user_roles.user_id "
	               + "LEFT JOIN roles ON user_roles.role_id = roles.id"; 
	    
	    Statement stmt = connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql); 

	    while (rs.next()) { 
	        int id = rs.getInt("id"); 
	        String email = rs.getString("email"); 
	        String username = rs.getString("username");
	        String password = rs.getString("hashedPassword"); 
	        String randSalt = rs.getString("randSalt");  
	        String role = rs.getString("role"); 
	        String firstName = rs.getString("firstName");

	        // Display values 
	        System.out.print("ID: " + id); 
	        System.out.print(", Username: " + username); 
	        System.out.print(", Email: " + email); 
	        System.out.println(", Hashed Password: " + Base64.getDecoder().decode(password)); 
	        System.out.println(", Rand Salt: " + Base64.getDecoder().decode(randSalt));
	        System.out.println(", Role: " + role); 
	        System.out.println(", First Name: " + firstName); 
	    }
	}


	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	public void displayAllTablesAndColumns() throws SQLException {
	    DatabaseMetaData metaData = connection.getMetaData();
	    
	    // Get all tables in the current database schema
	    ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
	    
	    // Loop through each table and get the columns
	    while (tables.next()) {
	        String tableName = tables.getString("TABLE_NAME");
	        System.out.println("Table: " + tableName);
	        
	        // Get all columns of the current table
	        ResultSet columns = metaData.getColumns(null, null, tableName, "%");
	        
	        while (columns.next()) {
	            String columnName = columns.getString("COLUMN_NAME");
	            String columnType = columns.getString("TYPE_NAME");
	            int columnSize = columns.getInt("COLUMN_SIZE");
	            System.out.println("\tColumn: " + columnName + " | Type: " + columnType + " | Size: " + columnSize);
	        }
	        
	        columns.close(); // Close the columns ResultSet
	    }
	    
	    tables.close(); // Close the tables ResultSet
	}
	

}