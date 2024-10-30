package cse360Project;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HelpArticleController {
    private Stage primaryStage;
    private DatabaseHelper dbHelper;

    public HelpArticleController(Stage primaryStage, DatabaseHelper dbHelper) {
        this.primaryStage = primaryStage;
        this.dbHelper = dbHelper;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showHelpArticleManagement() {
        // Check if user has the necessary role
        //if (!Session.getInstance().hasRole("admin") && !Session.getInstance().hasRole("instructor")) {
            //showAlert("Access Denied", "You do not have permission to access this feature.");
            //return;
        //}

        // Create a table to display articles
        TableView<HelpArticle> articleTable = new TableView<>();
        // Define table columns
        TableColumn<HelpArticle, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<HelpArticle, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<HelpArticle, String> groupsColumn = new TableColumn<>("Groups");
        groupsColumn.setCellValueFactory(cellData -> {
            List<String> groups = cellData.getValue().getGroups();
            String groupsString = String.join(", ", groups);
            return new SimpleStringProperty(groupsString);
        });

        // Add columns to the table
        articleTable.getColumns().addAll(idColumn, titleColumn, groupsColumn);

        // Fetch articles from the database
        try {
            List<HelpArticle> articles = dbHelper.getAllHelpArticles();
            articleTable.getItems().addAll(articles);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load articles: " + e.getMessage());
        }

        // Add buttons for CRUD operations
        Button createButton = new Button("Create New Article");
        Button editButton = new Button("Edit Article");
        Button deleteButton = new Button("Delete Article");
        Button backupButton = new Button("Backup Articles");
        Button restoreButton = new Button("Restore Articles");

        // Set up event handlers
        createButton.setOnAction(e -> showArticleEditor(null));
        editButton.setOnAction(e -> {
            HelpArticle selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) {
                showArticleEditor(selectedArticle);
            } else {
                showAlert("No Selection", "Please select an article to edit.");
            }
        });
        deleteButton.setOnAction(e -> {
            HelpArticle selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) {
                deleteArticle(selectedArticle);
                articleTable.getItems().remove(selectedArticle);
            } else {
                showAlert("No Selection", "Please select an article to delete.");
            }
        });
        backupButton.setOnAction(e -> backupArticles());
        restoreButton.setOnAction(e -> restoreArticles());

        // Layout the components
        HBox buttonBox = new HBox(10, createButton, editButton, deleteButton, backupButton, restoreButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, articleTable, buttonBox);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Help Article Management");
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION); // Or AlertType.ERROR based on context
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showArticleEditor(HelpArticle article) {
        boolean isEditMode = (article != null);

        // Create form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        if (isEditMode) titleField.setText(article.getTitle());

        TextArea shortDescArea = new TextArea();
        shortDescArea.setPromptText("Short Description");
        if (isEditMode) shortDescArea.setText(article.getShortDescription());

        TextArea bodyContentArea = new TextArea();
        bodyContentArea.setPromptText("Body Content");
        if (isEditMode) bodyContentArea.setText(article.getBodyContent());

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");
        if (isEditMode && article.getKeywords() != null) {
            keywordsField.setText(String.join(", ", article.getKeywords()));
        }

        TextField linksField = new TextField();
        linksField.setPromptText("Links (comma-separated)");
        if (isEditMode && article.getLinks() != null) {
            linksField.setText(String.join(", ", article.getLinks()));
        }

        // Groups (multi-select)
        List<String> allGroups = new ArrayList<>();
        try {
            allGroups = dbHelper.getAllGroups();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ObservableList<String> groupOptions = FXCollections.observableArrayList(allGroups);
        ListView<String> groupListView = new ListView<>(groupOptions);
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (isEditMode && article.getGroups() != null) {
            for (String group : article.getGroups()) {
                groupListView.getSelectionModel().select(group);
            }
        }

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            // Validate inputs
            if (titleField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Title is required.");
                return;
            }

            // Update or create article
            if (isEditMode) {
                article.setTitle(titleField.getText().trim());
                article.setShortDescription(shortDescArea.getText().trim());
                article.setBodyContent(bodyContentArea.getText().trim());
                article.setKeywords(Arrays.asList(keywordsField.getText().split(",")));
                article.setLinks(Arrays.asList(linksField.getText().split(",")));
                article.setGroups(groupListView.getSelectionModel().getSelectedItems());

                try {
                    dbHelper.updateHelpArticle(article);
                    showAlert("Success", "Article updated successfully.");
                    showHelpArticleManagement();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to update article: " + ex.getMessage());
                }
            } else {
                HelpArticle newArticle = new HelpArticle();
                newArticle.setTitle(titleField.getText().trim());
                newArticle.setShortDescription(shortDescArea.getText().trim());
                newArticle.setBodyContent(bodyContentArea.getText().trim());
                newArticle.setKeywords(Arrays.asList(keywordsField.getText().split(",")));
                newArticle.setLinks(Arrays.asList(linksField.getText().split(",")));
                newArticle.setGroups(groupListView.getSelectionModel().getSelectedItems());

                try {
                    long newId = dbHelper.createHelpArticle(newArticle);
                    newArticle.setId(newId);
                    showAlert("Success", "Article created successfully.");
                    showHelpArticleManagement();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to create article: " + ex.getMessage());
                }
            }
        });

        cancelButton.setOnAction(e -> showHelpArticleManagement());

        // Layout the form
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.setPadding(new Insets(10));

        form.add(new Label("Title:"), 0, 0);
        form.add(titleField, 1, 0);

        form.add(new Label("Short Description:"), 0, 1);
        form.add(shortDescArea, 1, 1);

        form.add(new Label("Body Content:"), 0, 2);
        form.add(bodyContentArea, 1, 2);

        form.add(new Label("Keywords:"), 0, 3);
        form.add(keywordsField, 1, 3);

        form.add(new Label("Links:"), 0, 4);
        form.add(linksField, 1, 4);

        form.add(new Label("Groups:"), 0, 5);
        form.add(groupListView, 1, 5);

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        form.add(buttonBox, 1, 6);

        Scene scene = new Scene(form, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle(isEditMode ? "Edit Article" : "Create New Article");
        primaryStage.show();
    }

    private void backupArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.setInitialFileName(Session.getInstance().getUsername() + "_backup.json");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                List<HelpArticle> articles = dbHelper.getAllHelpArticles();
                dbHelper.backupHelpArticles(file.getAbsolutePath(), articles);
                showAlert("Success", "Backup saved successfully.");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to backup articles: " + e.getMessage());
            }
        }
    }

    private void restoreArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            // Ask user whether to remove existing articles
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Restore Options");
            alert.setHeaderText("Choose Restore Option");
            alert.setContentText("Do you want to remove existing articles before restoring?");
            ButtonType removeButton = new ButtonType("Remove Existing");
            ButtonType mergeButton = new ButtonType("Merge");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(removeButton, mergeButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == cancelButton) {
                    return;
                }
                boolean removeExisting = (result.get() == removeButton);
                try {
                    dbHelper.restoreHelpArticles(file.getAbsolutePath(), removeExisting);
                    showAlert("Success", "Articles restored successfully.");
                    // Refresh the article list
                    showHelpArticleManagement();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to restore articles: " + e.getMessage());
                }
            }
        }
    }

    private void deleteArticle(HelpArticle article) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Article");
        alert.setHeaderText("Are you sure you want to delete this article?");
        alert.setContentText("Article ID: " + article.getId() + "\nTitle: " + article.getTitle());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = dbHelper.deleteHelpArticle(article.getId());
                if (success) {
                    showAlert("Success", "Article deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete article.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete article: " + e.getMessage());
            }
        }
    }
}
