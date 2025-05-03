package controllers;

import com.example.demo2.Database;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button signUpButton;
    private Employee employee;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Employee");
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        if (selectedRole == null) {
            showAlert("Error", "Please select a role.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT username, password, role FROM users WHERE username = ? AND password = ? AND LOWER(role) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, selectedRole.toLowerCase());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if ("admin".equalsIgnoreCase(selectedRole)) {
                        loadAdminDashboard();
                    } else {
                        // Now fetch from employees table
                        String empSql = "SELECT * FROM employees WHERE username = ?";
                        try (PreparedStatement empStmt = conn.prepareStatement(empSql)) {
                            empStmt.setString(1, username);
                            ResultSet empRs = empStmt.executeQuery();

                            if (empRs.next()) {
                                Employee employee = new Employee(
                                        empRs.getInt("emp_id"),
                                        empRs.getString("username"),
                                        empRs.getString("department"),
                                        empRs.getString("position"),
                                        empRs.getDouble("salary"),
                                        empRs.getInt("hours"),
                                        empRs.getString("password")
                                );
                                loadEmployeeDashboard(employee);
                            } else {
                                showAlert("Error", "Employee details not found.");
                            }
                        }

                        loadEmployeeDashboard(employee);
                    }
                } else {
                    showAlert("Error", "Invalid credentials.");
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error accessing database: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Error loading dashboard: " + e.getMessage());
        }
    }

    private void loadAdminDashboard() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/admin.fxml"));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private void loadEmployeeDashboard(Employee employee) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/employee.fxml"));
        Parent root = loader.load();

        EmpController controller = loader.getController();
        controller.setEmployeeId(employee.getEmpId());

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Employee Dashboard - " + employee.getUsername());
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadSignUpPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/signup.fxml"));
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Could not load Sign Up page: " + e.getMessage());
        }
    }
}
