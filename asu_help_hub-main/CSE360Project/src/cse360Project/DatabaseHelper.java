package cse360Project;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.sql.*;


import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.sql.ResultSet;

/*******
 * <p> DatabaseHelper Class </p>
 * 
 * <p> Description: An database utility class which helps with various controls surrounding the database. </p>
 * 
 * <p> Copyright: Carlos Hernandez © 2024 </p>
 * 
 * @author Carlos Hernandez
 * 
 * @version 1.0.0	2024-10-09 Updated for Phase 1
 * 
 */
class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/firstDatabase;AUTO_SERVER=TRUE";  
	

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			createTables();  // Create the necessary tables if they don't exist
			insertDefaultRoles(); // Insert the default roles if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		// Updated user table creation
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
		        + "randSalt VARCHAR(255) NOT NULL, "
		        + "otp VARCHAR(10), " // Column for storing the one-time password
		        + "otp_expiration TIMESTAMP" // Column for storing the expiration time of the OTP
		        + ")";
		statement.execute(userTable);
		
		// Create topics table
		String topicsTable = "CREATE TABLE IF NOT EXISTS user_topics ("
		        + "user_id INT NOT NULL, "
		        + "topic_name VARCHAR(255) NOT NULL, "
		        + "proficiency_level VARCHAR(20) DEFAULT 'intermediate', "
		        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " // Optional
		        + "PRIMARY KEY (user_id, topic_name))";
		

		statement.execute(topicsTable);
		
		// Create roles table
		String rolesTable = "CREATE TABLE IF NOT EXISTS roles ("
		        + "id INT AUTO_INCREMENT PRIMARY KEY, "
		        + "role_name VARCHAR(20) UNIQUE NOT NULL)";
		statement.execute(rolesTable);
		
		// Create invitations table
		String invitationsTable = "CREATE TABLE IF NOT EXISTS invitations ("
		        + "invite_code VARCHAR(255) PRIMARY KEY, "
		        + "used BOOLEAN DEFAULT false)";  // Indicates whether the invitation has been used
		statement.execute(invitationsTable);
		
	    // Create invitations roles relationship[ table
		String invitationsRolesTable = "CREATE TABLE IF NOT EXISTS invitation_roles ("
		        + "invite_code VARCHAR(255), "
		        + "role_id INT, "
		        + "FOREIGN KEY (invite_code) REFERENCES invitations(invite_code) ON DELETE CASCADE, "
		        + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE, "
		        + "PRIMARY KEY (invite_code, role_id))";

		statement.execute(invitationsRolesTable);


		// Create user_roles join table
		String userRolesTable = "CREATE TABLE IF NOT EXISTS user_roles ("
		        + "user_id INT NOT NULL, "
		        + "role_id INT NOT NULL, "
		        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
		        + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE, "
		        + "PRIMARY KEY (user_id, role_id))";
		statement.execute(userRolesTable);
		
		 // Create help_articles table
	    String helpArticlesTable = "CREATE TABLE IF NOT EXISTS help_articles ("
	            + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
	            + "header VARCHAR(255), "
	            + "title VARCHAR(255) NOT NULL, "
	            + "short_description TEXT, "
	            + "body_content TEXT, "
	            + "keywords TEXT, " // Can store keywords as a comma-separated string
	            + "links TEXT"      // Can store links as a comma-separated string
	            + ")";
	    statement.execute(helpArticlesTable);

	    // Create article_groups table
	    String articleGroupsTable = "CREATE TABLE IF NOT EXISTS article_groups ("
	            + "article_id BIGINT NOT NULL, "
	            + "group_name VARCHAR(255) NOT NULL, "
	            + "FOREIGN KEY (article_id) REFERENCES help_articles(id) ON DELETE CASCADE, "
	            + "PRIMARY KEY (article_id, group_name))";
	    statement.execute(articleGroupsTable);
	}
	
	// insert default roles into roles table if not already there 
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
	
	// Method to validate an invitation code and store the roles in the session
	public boolean validateInvitationCode(String inviteCode) throws SQLException {
	    String query = "SELECT used FROM invitations WHERE invite_code = ?";
	    String updateQuery = "UPDATE invitations SET used = true WHERE invite_code = ?";
	    String rolesQuery = "SELECT r.role_name FROM roles r "
	                      + "JOIN invitation_roles ir ON r.id = ir.role_id "
	                      + "WHERE ir.invite_code = ?";

	    // Check if the invitation exists and is not used
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, inviteCode);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                boolean isUsed = rs.getBoolean("used");
	                if (!isUsed) {
	                    // Mark the invitation as used
	                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
	                        updatePstmt.setString(1, inviteCode);
	                        updatePstmt.executeUpdate();
	                    }

	                    // Retrieve all associated roles for this invitation
	                    try (PreparedStatement rolesPstmt = connection.prepareStatement(rolesQuery)) {
	                        rolesPstmt.setString(1, inviteCode);
	                        try (ResultSet rolesRs = rolesPstmt.executeQuery()) {
	                            List<String> roleNames = new ArrayList<>();
	                            while (rolesRs.next()) {
	                                roleNames.add(rolesRs.getString("role_name"));
	                            }

	                            // Store the roles in the session
	                            Session session = Session.getInstance();
	                            session.setInvitedRoles(roleNames); // Store roles as a comma-separated string
	                        }
	                    }
	                    return true; // Invitation code is valid and marked as used
	                }
	            }
	        }
	    }
	    return false; // Invitation code is invalid or already used
	}
	

	// Method to store an invitation code and associated roles
	public void storeInvite(String inviteCode, List<String> roles) throws SQLException {
	    // Insert invitation code into the invitations table (no role_id here)
	    String insertInviteQuery = "INSERT INTO invitations (invite_code, used) VALUES (?, false)";
	    
	    try (PreparedStatement insertInviteStmt = connection.prepareStatement(insertInviteQuery)) {
	        insertInviteStmt.setString(1, inviteCode);
	        insertInviteStmt.executeUpdate();
	    }

	    // Insert each role associated with the invitation into the invitation_roles table
	    String getRoleIdQuery = "SELECT id FROM roles WHERE role_name = ?";
	    String insertRoleQuery = "INSERT INTO invitation_roles (invite_code, role_id) VALUES (?, ?)";

	    for (String role : roles) {
	        int roleId;

	        // Get the role ID for each role
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

	        // Insert the invite code and role ID into the invitation_roles table
	        try (PreparedStatement insertRoleStmt = connection.prepareStatement(insertRoleQuery)) {
	            insertRoleStmt.setString(1, inviteCode);
	            insertRoleStmt.setInt(2, roleId);
	            insertRoleStmt.executeUpdate();
	        }
	    }
	}

	
	// Method to delete the tables from the database
	public void dropTables() throws SQLException {

	    String dropQuery = "DROP TABLE IF EXISTS user_topics"; 
	    statement.executeUpdate(dropQuery);
	    
	    dropQuery = "DROP TABLE IF EXISTS user_roles"; 
	    statement.executeUpdate(dropQuery);
	    
	    dropQuery = "DROP TABLE IF EXISTS invitation_roles";
	    statement.executeUpdate(dropQuery);

	    dropQuery = "DROP TABLE IF EXISTS invitations";
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


	public void register(String username, String password, List<String> roles) throws Exception {
	    var passwd = new Password(password);
	    String hashedPassword = Base64.getEncoder().encodeToString(passwd.getHashedPass());
	    String randSalt = Base64.getEncoder().encodeToString(passwd.getSalt());

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

	        // Iterate through the roles and insert each role for the user
	        for (String role : roles) {
	            int roleId;
	            try (PreparedStatement pstmtGetRoleId = connection.prepareStatement(getRoleId)) {
	                pstmtGetRoleId.setString(1, role);
	                try (ResultSet rs = pstmtGetRoleId.executeQuery()) {
	                    if (rs.next()) {
	                        roleId = rs.getInt("id");
	                    } else {
	                        throw new Exception("Invalid role provided: " + role);
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

	        connection.commit(); // Commit transaction
	    } catch (Exception e) {
	        connection.rollback(); // Rollback transaction if something goes wrong
	        throw e;
	    } finally {
	        connection.setAutoCommit(true); // Restore auto-commit mode
	    }
	}
	
	public boolean login(String username, String passwordOrOtp) throws Exception {
	    String query = "SELECT * FROM users WHERE username = ?";
	    String getUserRoles = "SELECT r.role_name FROM roles r "
	                        + "JOIN user_roles ur ON r.id = ur.role_id "
	                        + "WHERE ur.user_id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                // Attempt to log in with the password
	                String hashedPassword = rs.getString("hashedPassword");
	                String randSalt = rs.getString("randSalt");

	                // Use your password verification method to compare
	                boolean isValidPassword = Password.verifyPassword(passwordOrOtp, 
	                                        Base64.getDecoder().decode(randSalt), 
	                                        Base64.getDecoder().decode(hashedPassword));

	                if (isValidPassword) {
	                    // Password login successful; proceed to set user session
	                    setUserSession(rs);
	                    Session.getInstance().setOTPUsed(false);
	                    return true; // Successful login
	                } else {
	                    // Check if the user is trying to log in with an OTP
	                    String storedOtp = rs.getString("otp");
	                    Timestamp otpExpiration = rs.getTimestamp("otp_expiration");

	                    // Verify the OTP and check for expiration
	                    if (storedOtp != null && storedOtp.equals(passwordOrOtp) && 
	                        (otpExpiration != null && otpExpiration.after(new Timestamp(System.currentTimeMillis())))) {
	                        // Clear the OTP to make it one-time use
	                        clearOtp(username);

	                        // Proceed to set user session
	                        setUserSession(rs);
	                        Session.getInstance().setOTPUsed(true);
	                        return true; // Successful OTP login
	                    }
	                }
	            }
	        }
	    }
	    return false; // Invalid login
	}

	private void setUserSession(ResultSet rs) throws SQLException {
	    // Retrieve user details
	    int userId = rs.getInt("id");
	    String email = rs.getString("email");
	    String firstName = rs.getString("firstName");
	    String lastName = rs.getString("lastName");
	    String preferredName = rs.getString("preferredName");

	    // Fetch user roles from user_roles table
	    List<String> roles = new ArrayList<>();
	    String getUserRoles = "SELECT r.role_name FROM roles r "
	                        + "JOIN user_roles ur ON r.id = ur.role_id "
	                        + "WHERE ur.user_id = ?";
	    
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
	        Session.getInstance().setUser(userId, rs.getString("username"), email, firstName, lastName, preferredName, roles);
	    } else {
	        // Only guaranteed values
	        System.out.println("missing set up");
	        Session.getInstance().setUser(userId, rs.getString("username"), roles);
	    }
	}

	private void clearOtp(String username) throws SQLException {
	    String clearOtpSql = "UPDATE users SET otp = NULL, otp_expiration = NULL WHERE username = ?";
	    try (PreparedStatement clearOtpStmt = connection.prepareStatement(clearOtpSql)) {
	        clearOtpStmt.setString(1, username);
	        clearOtpStmt.executeUpdate();
	    }
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
	

	public void resetUserPassword(String username) throws Exception {
	    String selectUserSql = "SELECT id FROM users WHERE username = ?";
	    String newPassword = generateRandomPassword(); // Generate a new password
	    Password pass = new Password(newPassword);
	    
	    // Generate OTP and expiration time
	    String otp = generateOneTimePassword();
	    Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + (24 * 3600000)); // 24 hour expiration

	    try (PreparedStatement selectStmt = connection.prepareStatement(selectUserSql)) {
	        selectStmt.setString(1, username);
	        ResultSet rs = selectStmt.executeQuery();

	        if (rs.next()) {
	            int userId = rs.getInt("id");

	            // Update the user's OTP and expiration time
	            String updatePasswordSql = "UPDATE users SET hashedPassword = ?, randSalt = ?, otp = ?, otp_expiration = ? WHERE id = ?";
	            try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordSql)) {
	                updateStmt.setString(1, Base64.getEncoder().encodeToString(pass.getHashedPass()));
	                updateStmt.setString(2, Base64.getEncoder().encodeToString(pass.getSalt())); // Generate a new salt if necessary
	                updateStmt.setString(3, otp);
	                updateStmt.setTimestamp(4, expirationTime);
	                updateStmt.setInt(5, userId);
	                updateStmt.executeUpdate();
	            }
	            System.out.println("Password reset successfully for user: " + username);
	            // Optionally, send the OTP to the user via email or display it
	        } else {
	            System.out.println("User not found: " + username);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateUserPassword(String username, String newPassword) throws Exception {
	    String selectUserSql = "SELECT id FROM users WHERE username = ?";
	    Password pass = new Password(newPassword); // Create Password instance with the new password

	    try (PreparedStatement selectStmt = connection.prepareStatement(selectUserSql)) {
	        selectStmt.setString(1, username);
	        ResultSet rs = selectStmt.executeQuery();

	        if (rs.next()) {
	            int userId = rs.getInt("id");

	            // Update the user's password
	            String updatePasswordSql = "UPDATE users SET hashedPassword = ?, randSalt = ? WHERE id = ?";
	            try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordSql)) {
	                updateStmt.setString(1, Base64.getEncoder().encodeToString(pass.getHashedPass()));
	                updateStmt.setString(2, Base64.getEncoder().encodeToString(pass.getSalt())); // Generate a new salt if necessary
	                updateStmt.setInt(3, userId);
	                updateStmt.executeUpdate();
	            }
	            System.out.println("Password updated successfully for user: " + username);
	            // Optionally, notify the user about the successful password change
	        } else {
	            System.out.println("User not found: " + username);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	private String generateOneTimePassword() {
	    int length = 6; // Length of the OTP
	    Random random = new Random();
	    StringBuilder otp = new StringBuilder();
	    for (int i = 0; i < length; i++) {
	        otp.append(random.nextInt(10)); // Append random digit
	    }
	    System.out.println("OTP Generated: " + otp.toString());
	    return otp.toString();
	}
	
	// Method to generate a random password
	private String generateRandomPassword() {
	    byte[] randomBytes = new byte[16];
	    SecureRandom random = new SecureRandom();
	    random.nextBytes(randomBytes);
	    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, 16); // Return a substring to ensure length
	}

	// method to delete a user account based on username
	public void deleteUserAccount(String username) {
	    String deleteUserSql = "DELETE FROM users WHERE username = ?";

	    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteUserSql)) {
	        deleteStmt.setString(1, username);
	        int rowsAffected = deleteStmt.executeUpdate();

	        if (rowsAffected > 0) {
	            System.out.println("User account deleted successfully: " + username);
	        } else {
	            System.out.println("User not found: " + username);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Command line display Users. Using for debug purposes.
	public void displayUsersByUser() throws UnsupportedEncodingException, Exception {
	    String sql = "SELECT users.id, users.email, users.username, users.hashedPassword, users.randSalt, "
	               + "GROUP_CONCAT(roles.role_name SEPARATOR ', ') AS roles, users.firstName "
	               + "FROM users "
	               + "LEFT JOIN user_roles ON users.id = user_roles.user_id "
	               + "LEFT JOIN roles ON user_roles.role_id = roles.id "
	               + "GROUP BY users.id"; 

	    Statement stmt = connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql); 

	    while (rs.next()) { 
	        int id = rs.getInt("id"); 
	        String email = rs.getString("email"); 
	        String username = rs.getString("username");
	        String password = rs.getString("hashedPassword"); 
	        String randSalt = rs.getString("randSalt");  
	        String roles = rs.getString("roles"); 
	        String firstName = rs.getString("firstName");

	        // Display values 
	        System.out.println("ID: " + id); 
	        System.out.println("Username: " + username); 
	        System.out.println("Email: " + email); 
	        System.out.println("Hashed Password: " + Base64.getDecoder().decode(password)); 
	        System.out.println("Rand Salt: " + Base64.getDecoder().decode(randSalt));
	        System.out.println("Roles: " + roles); 
	        System.out.println("First Name: " + firstName); 
	        System.out.println("----------------------------------------"); // Separator for clarity
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
	
	public int getRoleId(String roleName) throws SQLException {
	    String sql = "SELECT id FROM roles WHERE role_name = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, roleName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("id"); // Return the role ID
	            } else {
	                throw new SQLException("Role not found: " + roleName); // Handle role not found
	            }
	        }
	    }
	}
	
	// Load users into the TableView
	public void loadUsersIntoTable(TableView<List<String>> tableView) {
	    String sql = "SELECT users.username, CONCAT(users.firstName, ' ', users.lastName) AS name, GROUP_CONCAT(roles.role_name) AS roles " +
	                 "FROM users " +
	                 "LEFT JOIN user_roles ON users.id = user_roles.user_id " +
	                 "LEFT JOIN roles ON user_roles.role_id = roles.id " +
	                 "GROUP BY users.id";

	    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
	        while (rs.next()) {
	            String username = rs.getString("username");
	            String name = rs.getString("name");
	            String roles = rs.getString("roles");

	            // Create a list of user details
	            List<String> userDetails = new ArrayList<>();
	            userDetails.add(username);
	            userDetails.add(name);
	            userDetails.add(roles);

	            // Add the user details list to the TableView
	            tableView.getItems().add(userDetails);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public void listUserAccounts() throws SQLException {
	    String sql = "SELECT users.id, users.username, users.firstName, roles.role_name " +
	                 "FROM users " +
	                 "LEFT JOIN user_roles ON users.id = user_roles.user_id " +
	                 "LEFT JOIN roles ON user_roles.role_id = roles.id";
	    
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String username = rs.getString("username");
	            String firstName = rs.getString("firstName");
	            String role = rs.getString("role_name");

	            System.out.println("ID: " + id + ", Username: " + username + 
	                               ", First Name: " + firstName + ", Role: " + role);
	        }
	    }
	}
	
	
	public void manageUserRole(int userId, String role, boolean addRole) throws Exception {
	    String roleSQL;
	    
	    if (addRole) {
	        roleSQL = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
	        int roleId = getRoleId(role); // Method to get role ID based on role name
	        try (PreparedStatement pstmt = connection.prepareStatement(roleSQL)) {
	            pstmt.setInt(1, userId);
	            pstmt.setInt(2, roleId);
	            pstmt.executeUpdate();
	        }
	    } else {
	        roleSQL = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
	        int roleId = getRoleId(role);
	        try (PreparedStatement pstmt = connection.prepareStatement(roleSQL)) {
	            pstmt.setInt(1, userId);
	            pstmt.setInt(2, roleId);
	            pstmt.executeUpdate();
	        }
	    }
	}
	
	public long createHelpArticle(HelpArticle article) throws SQLException {
	    String insertArticle = "INSERT INTO help_articles (header, title, short_description, body_content, keywords, links) "
	            + "VALUES (?, ?, ?, ?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(insertArticle, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setString(1, article.getHeader());
	        pstmt.setString(2, article.getTitle());
	        pstmt.setString(3, article.getShortDescription());
	        pstmt.setString(4, article.getBodyContent());
	        pstmt.setString(5, String.join(",", article.getKeywords())); // Convert list to comma-separated string
	        pstmt.setString(6, String.join(",", article.getLinks()));    // Convert list to comma-separated string

	        int affectedRows = pstmt.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Creating article failed, no rows affected.");
	        }

	        // Get the generated article ID
	        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                long articleId = generatedKeys.getLong(1);

	                // Handle groups
	                addArticleGroups(articleId, article.getGroups());

	                return articleId;
	            } else {
	                throw new SQLException("Creating article failed, no ID obtained.");
	            }
	        }
	    }
	}

	private void addArticleGroups(long articleId, List<String> groupNames) throws SQLException {
	    String insertGroup = "INSERT INTO article_groups (article_id, group_name) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertGroup)) {
	        for (String groupName : groupNames) {
	            pstmt.setLong(1, articleId);
	            pstmt.setString(2, groupName);
	            pstmt.addBatch();
	        }
	        pstmt.executeBatch();
	    }
	}
	public List<HelpArticle> getAllHelpArticles() throws SQLException {
	    String query = "SELECT * FROM help_articles";
	    List<HelpArticle> articles = new ArrayList<>();

	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            HelpArticle article = extractHelpArticleFromResultSet(rs);
	            articles.add(article);
	        }
	    }
	    return articles;
	}

	private HelpArticle extractHelpArticleFromResultSet(ResultSet rs) throws SQLException {
	    HelpArticle article = new HelpArticle();
	    article.setId(rs.getLong("id"));
	    article.setHeader(rs.getString("header"));
	    article.setTitle(rs.getString("title"));
	    article.setShortDescription(rs.getString("short_description"));
	    article.setBodyContent(rs.getString("body_content"));

	    // Convert comma-separated strings back to lists
	    String keywords = rs.getString("keywords");
	    if (keywords != null && !keywords.isEmpty()) {
	        article.setKeywords(Arrays.asList(keywords.split(",")));
	    }

	    String links = rs.getString("links");
	    if (links != null && !links.isEmpty()) {
	        article.setLinks(Arrays.asList(links.split(",")));
	    }

	    // Get groups associated with the article
	    article.setGroups(getGroupsForArticle(article.getId()));

	    return article;
	}

	private List<String> getGroupsForArticle(long articleId) throws SQLException {
	    String query = "SELECT group_name FROM article_groups WHERE article_id = ?";
	    List<String> groups = new ArrayList<>();
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setLong(1, articleId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                groups.add(rs.getString("group_name"));
	            }
	        }
	    }
	    return groups;
	}
	public boolean updateHelpArticle(HelpArticle article) throws SQLException {
	    String updateArticle = "UPDATE help_articles SET header = ?, title = ?, short_description = ?, "
	            + "body_content = ?, keywords = ?, links = ? WHERE id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(updateArticle)) {
	        pstmt.setString(1, article.getHeader());
	        pstmt.setString(2, article.getTitle());
	        pstmt.setString(3, article.getShortDescription());
	        pstmt.setString(4, article.getBodyContent());
	        pstmt.setString(5, String.join(",", article.getKeywords()));
	        pstmt.setString(6, String.join(",", article.getLinks()));
	        pstmt.setLong(7, article.getId());

	        int affectedRows = pstmt.executeUpdate();

	        // Update groups
	        updateArticleGroups(article.getId(), article.getGroups());

	        return affectedRows > 0;
	    }
	}

	private void updateArticleGroups(long articleId, List<String> groupNames) throws SQLException {
	    // First, delete existing groups
	    String deleteGroups = "DELETE FROM article_groups WHERE article_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteGroups)) {
	        pstmt.setLong(1, articleId);
	        pstmt.executeUpdate();
	    }

	    // Then, add new groups
	    addArticleGroups(articleId, groupNames);
	}
	public boolean deleteHelpArticle(long articleId) throws SQLException {
	    String deleteArticle = "DELETE FROM help_articles WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
	        pstmt.setLong(1, articleId);
	        int affectedRows = pstmt.executeUpdate();
	        return affectedRows > 0;
	        		}
	    }
	
	public void backupHelpArticles(String filePath, List<HelpArticle> articlesToBackup) throws IOException {
	    ObjectMapper objectMapper = new ObjectMapper(); // From Jackson library
	    objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // For readable JSON
	    objectMapper.writeValue(new File(filePath), articlesToBackup);
	}
	public void restoreHelpArticles(String filePath, boolean removeExisting) throws IOException, SQLException {
	    ObjectMapper objectMapper = new ObjectMapper();
	    List<HelpArticle> articlesFromBackup = objectMapper.readValue(new File(filePath), new TypeReference<List<HelpArticle>>() {});

	    if (removeExisting) {
	        // Delete all existing help articles
	        String deleteAllArticles = "DELETE FROM help_articles";
	        try (Statement stmt = connection.createStatement()) {
	            stmt.executeUpdate(deleteAllArticles);
	        }
	    }

	    // Insert or merge articles
	    for (HelpArticle article : articlesFromBackup) {
	        if (!doesHelpArticleExist(article.getId())) {
	            // Insert new article
	            createHelpArticle(article);
	        } else {
	            // Update existing article or skip
	            updateHelpArticle(article);
	        }
	    }
	}

	private boolean doesHelpArticleExist(long articleId) throws SQLException {
	    String query = "SELECT COUNT(*) FROM help_articles WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setLong(1, articleId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	    }
	    return false;
	}
	public List<String> getAllGroups() throws SQLException {
	    String query = "SELECT DISTINCT group_name FROM article_groups";
	    List<String> groups = new ArrayList<>();
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            groups.add(rs.getString("group_name"));
	        }
	    }
	    return groups;
	}
	public List<HelpArticle> getHelpArticlesByGroup(String groupName) throws SQLException {
	    String query = "SELECT ha.* FROM help_articles ha "
	            + "JOIN article_groups ag ON ha.id = ag.article_id "
	            + "WHERE ag.group_name = ?";
	    List<HelpArticle> articles = new ArrayList<>();
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, groupName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                HelpArticle article = extractHelpArticleFromResultSet(rs);
	                articles.add(article);
	            }
	        }
	    }
	    return articles;
	}

}
