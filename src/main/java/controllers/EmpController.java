package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Employee;
import services.EmployeeService;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EmpController {

    @FXML private Label usernameLabel;
    @FXML private Label departmentLabel;
    @FXML private Label positionLabel;
    @FXML private Label salaryLabel;
    @FXML private Label hoursLabel;
    @FXML private Label titleLabel;
    @FXML private Label personIcon;
    @FXML private Label clockLabel;

    private int employeeId;
    private final EmployeeService employeeService = new EmployeeService();


    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
        loadEmployeeDetails();
    }

    private void loadEmployeeDetails() {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            usernameLabel.setText("Username: " + employee.getUsername());
            departmentLabel.setText("Department: " + employee.getDepartment());
            positionLabel.setText("Position: " + employee.getPosition());
            salaryLabel.setText("Salary: $" + employee.getSalary());
            hoursLabel.setText("Hours: " + employee.getHours() + " hrs");
        }
    }

    @FXML
    public void generatePayslip() {
        String payslip = employeeService.generatePayslip(employeeId);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payslip Generated");
        alert.setHeaderText("Your Payslip Details");
        alert.setContentText(payslip);
        alert.getDialogPane().setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: #e8f5e9;" +
                        "-fx-border-color: #26a69a;" +
                        "-fx-border-width: 2px;"
        );
        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("Stylesheet not loaded: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error during logout: " + e.getMessage());
        }
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLabel.setText("Current Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }


    @FXML
    public void onButtonHover() {
        // Can add hover effects here
    }

    @FXML
    public void onButtonExit() {
        // Can add exit effects here
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}