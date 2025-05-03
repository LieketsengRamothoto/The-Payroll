package controllers;

import com.example.demo2.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import models.Employee;
import models.Payroll;
import models.PayrollReportView;

import java.sql.*;

public class AdminController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> columnUsername;
    @FXML private TableColumn<Employee, String> columnDepartment;
    @FXML private TableColumn<Employee, String> columnPosition;
    @FXML private TableColumn<Employee, Double> columnSalary;
    @FXML private TableColumn<Employee, Integer> columnHours;
    @FXML private TableColumn<Employee, String> columnPassword;
    @FXML private Button addEmployeeButton;
    @FXML private Button updateEmployeeButton;
    @FXML private Button deleteEmployeeButton;
    @FXML private Button generatePayslipButton;
    @FXML private Button viewPayrollReportButton;
    @FXML private Button logoutButton;

    private ObservableList<Employee> employees;

    @FXML
    public void initialize() {
        // Initialize table columns
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        columnPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        columnSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        columnHours.setCellValueFactory(new PropertyValueFactory<>("hours"));
        columnPassword.setCellValueFactory(new PropertyValueFactory<>("password"));

        loadEmployees();

        // Set button actions
        addEmployeeButton.setOnAction(event -> showAddEmployeeDialog());
        updateEmployeeButton.setOnAction(event -> showUpdateEmployeeDialog());
        deleteEmployeeButton.setOnAction(event -> deleteEmployee());
        generatePayslipButton.setOnAction(event -> generatePayslip());
        viewPayrollReportButton.setOnAction(event -> viewPayrollReport());
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void showAddEmployeeDialog() {
        Dialog<Employee> dialog = createEmployeeDialog("Add New Employee", "Enter Employee Details", null);
        dialog.showAndWait().ifPresent(employee -> addEmployeeToDatabase(employee));
    }

    private void showUpdateEmployeeDialog() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No employee selected");
            return;
        }

        Dialog<Employee> dialog = createEmployeeDialog("Update Employee", "Edit Employee Details", selected);
        dialog.showAndWait().ifPresent(employee -> updateEmployeeInDatabase(selected, employee));
    }

    private Dialog<Employee> createEmployeeDialog(String title, String headerText, Employee employeeToEdit) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        TextField departmentField = new TextField();
        TextField positionField = new TextField();
        TextField salaryField = new TextField();
        TextField hoursField = new TextField();
        PasswordField passwordField = new PasswordField();

        if (employeeToEdit != null) {
            usernameField.setText(employeeToEdit.getUsername());
            departmentField.setText(employeeToEdit.getDepartment());
            positionField.setText(employeeToEdit.getPosition());
            salaryField.setText(String.valueOf(employeeToEdit.getSalary()));
            hoursField.setText(String.valueOf(employeeToEdit.getHours()));
            passwordField.setText(employeeToEdit.getPassword());
        }

        grid.add(new Label("USERNAME:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("DEPARTMENT:"), 0, 1);
        grid.add(departmentField, 1, 1);
        grid.add(new Label("POSITION:"), 0, 2);
        grid.add(positionField, 1, 2);
        grid.add(new Label("SALARY:"), 0, 3);
        grid.add(salaryField, 1, 3);
        grid.add(new Label("HOURS:"), 0, 4);
        grid.add(hoursField, 1, 4);
        grid.add(new Label("PASSWORD:"), 0, 5);
        grid.add(passwordField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(usernameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    return new Employee(
                            employeeToEdit != null ? employeeToEdit.getEmpId() : 0,
                            usernameField.getText(),
                            departmentField.getText(),
                            positionField.getText(),
                            Double.parseDouble(salaryField.getText()),
                            Integer.parseInt(hoursField.getText()),
                            passwordField.getText()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Invalid number format in salary or hours");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void addEmployeeToDatabase(Employee employee) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO employees (username, department, position, salary, hours, password) VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, employee.getUsername());
            stmt.setString(2, employee.getDepartment());
            stmt.setString(3, employee.getPosition());
            stmt.setDouble(4, employee.getSalary());
            stmt.setInt(5, employee.getHours());
            stmt.setString(6, employee.getPassword());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setEmpId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }

            employees.add(employee);
            employeeTable.refresh();

        } catch (SQLException e) {
            showAlert("Error adding employee: " + e.getMessage());
        }
    }

    private void updateEmployeeInDatabase(Employee oldEmployee, Employee updatedEmployee) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE employees SET username=?, department=?, position=?, salary=?, hours=?, password=? WHERE emp_id=?")) {

            stmt.setString(1, updatedEmployee.getUsername());
            stmt.setString(2, updatedEmployee.getDepartment());
            stmt.setString(3, updatedEmployee.getPosition());
            stmt.setDouble(4, updatedEmployee.getSalary());
            stmt.setInt(5, updatedEmployee.getHours());
            stmt.setString(6, updatedEmployee.getPassword());
            stmt.setInt(7, updatedEmployee.getEmpId());

            stmt.executeUpdate();

            int index = employees.indexOf(oldEmployee);
            employees.set(index, updatedEmployee);
            employeeTable.refresh();

        } catch (SQLException e) {
            showAlert("Error updating employee: " + e.getMessage());
        }
    }

    private void loadEmployees() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            employees = FXCollections.observableArrayList();
            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("emp_id"),
                        rs.getString("username"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getDouble("salary"),
                        rs.getInt("hours"),
                        rs.getString("password")
                ));
            }
            employeeTable.setItems(employees);
        } catch (SQLException e) {
            showAlert("Error loading employees: " + e.getMessage());
        }
    }

    private void deleteEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No employee selected");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Employee");
        confirmation.setContentText("Are you sure you want to delete " + selected.getUsername() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM employees WHERE emp_id=?")) {
                    stmt.setInt(1, selected.getEmpId());
                    stmt.executeUpdate();
                    employees.remove(selected);
                } catch (SQLException e) {
                    showAlert("Error deleting employee: " + e.getMessage());
                }
            }
        });
    }

    private void generatePayslip() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No employee selected for payslip generation");
            return;
        }

        Payroll payroll = selected.calculatePayroll();
        savePayrollToDatabase(selected.getEmpId(), payroll);

        String payslip = String.format(
                "========== Payslip ==========\n" +
                        "Employee Username: %s\n" +
                        "Department: %s\n" +
                        "Position: %s\n" +
                        "-----------------------------\n" +
                        "Basic Salary: $%.2f\n" +
                        "Hours Worked: %d\n" +
                        "Gross Salary: $%.2f\n" +
                        "Deductions:\n" +
                        "  - Tax (20%%): $%.2f\n" +
                        "  - Insurance: $100.00\n" +
                        "Total Deductions: $%.2f\n" +
                        "-----------------------------\n" +
                        "Net Salary: $%.2f\n" +
                        "=============================",
                selected.getUsername(),
                selected.getDepartment(),
                selected.getPosition(),
                selected.getSalary(),
                selected.getHours(),
                payroll.getGrossSalary(),
                payroll.getGrossSalary() * 0.2,
                payroll.getDeductions(),
                payroll.getNetSalary()
        );

        TextArea payslipArea = new TextArea(payslip);
        payslipArea.setWrapText(true);
        payslipArea.setEditable(false);

        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(employeeTable.getScene().getWindow())) {
            boolean printed = job.printPage(payslipArea);
            if (printed) {
                job.endJob();
                showAlert("Payslip printed successfully.");
            } else {
                showAlert("Failed to print payslip.");
            }
        }
    }

    private void savePayrollToDatabase(int empId, Payroll payroll) {
        String query = "INSERT INTO payroll (emp_id, date, gross, deductions, net) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, empId);
            stmt.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            stmt.setDouble(3, payroll.getGrossSalary());
            stmt.setDouble(4, payroll.getDeductions());
            stmt.setDouble(5, payroll.getNetSalary());
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Error saving payroll to database: " + e.getMessage());
        }
    }

    private void viewPayrollReport() {
        try {
            PayrollReportView reportView = new PayrollReportView(employees);
            Stage reportStage = new Stage();
            reportView.start(reportStage);
        } catch (Exception e) {
            showAlert("Error viewing payroll report: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to return to login: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}