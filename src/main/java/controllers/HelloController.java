package controllers;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label loginStatus;
    @FXML private Label welcomeText;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            loginStatus.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                openDashboardBasedOnRole(user);
            } else {
                loginStatus.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            loginStatus.setText("Database error: Unable to authenticate user.");
        } catch (Exception e) {
            loginStatus.setText("An error occurred: " + e.getMessage());
        }
    }

    private void openDashboardBasedOnRole(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Parent root;

            if ("admin".equalsIgnoreCase(user.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin.fxml"));
                root = loader.load();
            } else if ("employee".equalsIgnoreCase(user.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/employee.fxml"));
                root = loader.load();
                EmpController controller = loader.getController();
                controller.setEmployeeId(user.getEmpId());
            } else {
                loginStatus.setText("Unknown role.");
                return;
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1) + " Dashboard");
            stage.show();
        } catch (IOException e) {
            loginStatus.setText("Error loading dashboard: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(3), welcomeText);
        transition.setFromX(0);
        transition.setToX(200);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();
    }
}
