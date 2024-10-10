package cse360Project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class App extends Application {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    @Override
    public void start(Stage primaryStage) throws Exception {
        databaseHelper.connectToDatabase();
        primaryStage.setTitle("ASU Help Hub");

        // Labels and Text Fields
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        // Invitation code input field
        Label invitationCodeLabel = new Label("Invitation Code:");
        TextField invitationCodeField = new TextField();

        // Buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        loginButton.setOnAction(e -> {
            try {
                // Add logic to check invitation code if entered
                if (!invitationCodeField.getText().isEmpty()) {
                    // Validate invitation code and take the user to the setup page
                    if (databaseHelper.validateInvitationCode(invitationCodeField.getText())) {
                        showFinishSetupWithInvite(primaryStage, invitationCodeField.getText());
                    } else {
                        System.out.println("Invalid invitation code.");
                    }
                } else {
                    loginFlow(usernameField.getText(), passwordField.getText(), primaryStage);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        registerButton.setOnAction(e -> showAdminInvitePage(primaryStage));

        // Layouts
        VBox layout = new VBox(10);
        layout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, invitationCodeLabel, invitationCodeField);

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(loginButton, registerButton);
        layout.getChildren().add(buttonLayout);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showAdminInvitePage(Stage stage) {
        stage.setTitle("Generate Invitation");

        // GridPane layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Role dropdown
        Label roleLabel = new Label("Select Role:");
        ChoiceBox<String> roleChoiceBox = new ChoiceBox<>();
        roleChoiceBox.getItems().addAll("student", "admin", "instructor");

        // Text field for the invitation code
        Label inviteCodeLabel = new Label("Enter Invitation Code:");
        TextField inviteCodeField = new TextField();

        // Generate button
        Button generateButton = new Button("Generate Invitation");
        Label messageLabel = new Label();

        generateButton.setOnAction(e -> {
            String selectedRole = roleChoiceBox.getValue();
            String inviteCode = inviteCodeField.getText();
            if (selectedRole != null && !inviteCode.isEmpty()) {
                try {
                    // Store invitation code with selected role in the database
                    databaseHelper.storeInvite(inviteCode, selectedRole);
                    messageLabel.setText("Invitation generated successfully!");
                    messageLabel.setTextFill(Color.GREEN);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Error generating invitation.");
                    messageLabel.setTextFill(Color.RED);
                }
            } else {
                messageLabel.setText("Please select a role and enter a valid invite code.");
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Add elements to grid
        gridPane.add(roleLabel, 0, 0);
        gridPane.add(roleChoiceBox, 1, 0);
        gridPane.add(inviteCodeLabel, 0, 1);
        gridPane.add(inviteCodeField, 1, 1);
        gridPane.add(generateButton, 1, 2);
        gridPane.add(messageLabel, 1, 3);

        Scene scene = new Scene(gridPane, 400, 250);
        stage.setScene(scene);
    }

    public void showFinishSetupWithInvite(Stage stage, String inviteCode) {
        stage.setTitle("Finish Setup");

        // Assume user was invited via a one-time code, allow them to finish setup
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();

        Button finishSetupButton = new Button("Finish Setup");

        finishSetupButton.setOnAction(e -> {
            try {
                String username = firstNameField.getText().toLowerCase() + "." + lastNameField.getText().toLowerCase(); // Example username generation
                String role = "student"; // Assuming a default role or get it from another field

                // Passing the 6 arguments required for completeRegistrationWithInvite
                databaseHelper.completeRegistrationWithInvite(emailField.getText(), firstNameField.getText(), lastNameField.getText(), inviteCode, username, role);
                start(stage); // Redirect back to main page
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        gridPane.add(emailLabel, 0, 0);
        gridPane.add(emailField, 1, 0);
        gridPane.add(firstNameLabel, 0, 1);
        gridPane.add(firstNameField, 1, 1);
        gridPane.add(lastNameLabel, 0, 2);
        gridPane.add(lastNameField, 1, 2);
        gridPane.add(finishSetupButton, 1, 3);

        Scene scene = new Scene(gridPane, 400, 250);
        stage.setScene(scene);
    }

    // Define the loginFlow method to handle login
    public void loginFlow(String username, String password, Stage primaryStage) throws Exception {
        if (databaseHelper.login(username, password)) {
            System.out.println("Login successful for user: " + username);
            showStudentAndInstructorHomePage(primaryStage);
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    public void showStudentAndInstructorHomePage(Stage stage) {
        stage.setTitle("Home Page");

        Label welcomeLabel = new Label("Welcome to ASU Help Hub!");

        VBox layout = new VBox(10);
        layout.getChildren().add(welcomeLabel);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
