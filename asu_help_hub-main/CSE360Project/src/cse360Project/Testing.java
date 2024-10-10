package cse360Project;

public class Testing {
	public static void main(String[] args) throws Exception {
		// Create a Password object and hash the password "Someone1"
		Password pass = new Password("Someone1");

		// Print out the hashed password and salt (these will be in byte array format)
		System.out.println("Hashed Password (Base64 Encoded): " + java.util.Base64.getEncoder().encodeToString(pass.hashedPassword));
		System.out.println("Random Salt (Base64 Encoded): " + java.util.Base64.getEncoder().encodeToString(pass.randSalt));

		// Verify if the password "Someone" is correct (expected to fail)
		boolean isVerifiedIncorrect = pass.verifyPassword("Someone");
		System.out.println("Verification of 'Someone': " + (isVerifiedIncorrect ? "Success" : "Failure"));

		// Verify if the password "Someone1" is correct (expected to pass)
		boolean isVerifiedCorrect = pass.verifyPassword("Someone1");
		System.out.println("Verification of 'Someone1': " + (isVerifiedCorrect ? "Success" : "Failure"));
	}
}
