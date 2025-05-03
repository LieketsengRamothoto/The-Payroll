package controllers;

import com.example.demo2.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.*;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Basic input validation
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("Username and password cannot be empty!");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Retrieve stored password.
                String storedPassword = rs.getString("password");
                String role = rs.getString("role");

                // TODO: Use a proper hashing algorithm to compare passwords!
                // For demonstration, using direct comparison (not recommended for production)
                if (password.equals(storedPassword)) {
                    loadDashboard(role);
                } else {
                    showAlert("Invalid credentials!"); // Incorrect password
                }
            } else {
                showAlert("Invalid credentials!"); // Username not found
            }
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage());
        }
    }

    private void loadDashboard(String role) {
        try {
            String fxmlFile;
            // Use a more maintainable way for managing user roles.
            switch (role.toLowerCase()) {
                case "admin":
                    fxmlFile = "/views/admin.fxml"; // Path to your admin FXML
                    break;
                case "employee":
                    fxmlFile = "/views/employee.fxml"; // Path to your employee FXML
                    break;
                default:
                    showAlert("Unknown role: " + role);
                    return;
            }

            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Payroll Management System - " + role);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace for debugging
            showAlert("Error loading dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setHeaderText(null); // Optional: remove header
        alert.showAndWait();
    }
}