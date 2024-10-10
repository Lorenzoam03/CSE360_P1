package cse360Project;

import java.security.MessageDigest;
import java.security.SecureRandom;


public class Password {

    public byte[] hashedPassword;
    public byte[] randSalt;
    
    public Password(String password) throws Exception {
    	hashPassword(password);
    	
    }

    // hash a string password
    public byte[] hashPassword(String password) throws Exception {
        try {
            // Generate random salt using secure rng
            randSalt = new byte[16]; 
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(randSalt); // Fill the randSalt array with random values

            // Concatenate the salt and the password
            byte[] passwordBytes = password.getBytes();
            byte[] saltedPassword = new byte[randSalt.length + passwordBytes.length];

            System.arraycopy(randSalt, 0, saltedPassword, 0, randSalt.length); // Copy salt to the start
            System.arraycopy(passwordBytes, 0, saltedPassword, randSalt.length, passwordBytes.length); // Add password bytes after salt

            // Hash the concatenated salt and password using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hashedPassword = digest.digest(saltedPassword); // Hash salted password

            return hashedPassword;
        } catch (Exception e) {
            throw new Exception("Error during encryption: " + e.getMessage());
        }
    }
    
    // verify the password given the stored salt, the storedHashedPassword, and the new String
    public boolean verifyPassword(String password) throws Exception {
        try {
            // Concatenate the stored salt with the input password
            byte[] passwordBytes = password.getBytes();
            byte[] saltedPassword = new byte[randSalt.length + passwordBytes.length];

            System.arraycopy(randSalt, 0, saltedPassword, 0, randSalt.length); // Copy stored salt
            System.arraycopy(passwordBytes, 0, saltedPassword, randSalt.length, passwordBytes.length); // Add password

            // Hash the salted password
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedInputPassword = digest.digest(saltedPassword); // Hash salted password

            // Compare the hashed input password with the stored hashed password
            return MessageDigest.isEqual(hashedInputPassword, hashedPassword);
        } catch (Exception e) {
            throw new Exception("Error during password verification: " + e.getMessage());
        }
    }
    
    public static boolean verifyPassword(String password, byte[] randSalt, byte[] hashedPassword) throws Exception {
        try {
            // Concatenate the stored salt with the input password
            byte[] passwordBytes = password.getBytes();
            byte[] saltedPassword = new byte[randSalt.length + passwordBytes.length];

            System.arraycopy(randSalt, 0, saltedPassword, 0, randSalt.length); // Copy stored salt
            System.arraycopy(passwordBytes, 0, saltedPassword, randSalt.length, passwordBytes.length); // Add password

            // Hash the salted password
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedInputPassword = digest.digest(saltedPassword); // Hash salted password

            // Compare the hashed input password with the stored hashed password
            return MessageDigest.isEqual(hashedInputPassword, hashedPassword);
        } catch (Exception e) {
            throw new Exception("Error during password verification: " + e.getMessage());
        }
    }

    public byte[] getSalt() {
        return this.randSalt;
    }
    
    public byte[] getHashedPass() {
    	return this.hashedPassword;
    }
}