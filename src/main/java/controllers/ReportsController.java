package controllers;

import com.example.demo2.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Payroll;
import utils.CSVExporter;
import utils.PDFGenerator;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {
    @FXML private PieChart salaryPieChart;
    @FXML private LineChart<String, Number> salaryLineChart;
    @FXML private VBox buttonContainer;

    public void initialize() {
        loadSalaryData();
        createExportButtons();
    }

    private void loadSalaryData() {
        salaryPieChart.getData().clear(); // Clear previous data
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT department, SUM(salary) AS total_salary FROM employees GROUP BY department")) {

            while (rs.next()) {
                String department = rs.getString("department");
                double totalSalary = rs.getDouble("total_salary");
                salaryPieChart.getData().add(new PieChart.Data(department, totalSalary));
            }
        } catch (SQLException e) {
            showAlert("Error loading salary data: " + e.getMessage());
        }
    }

    private List<Payroll> fetchPayrollData() {
        List<Payroll> payrollList = new ArrayList<>();
        String query = "SELECT emp_id, gross_salary, deductions, net_salary FROM payroll";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int empId = rs.getInt("emp_id");
                String employeeName = getEmployeeNameById(empId);
                double grossSalary = rs.getDouble("gross_salary");
                double deductions = rs.getDouble("deductions");
                double netSalary = rs.getDouble("net_salary");

                Payroll payroll = new Payroll(employeeName, grossSalary, deductions, netSalary);
                payrollList.add(payroll);
            }
        } catch (SQLException e) {
            showAlert("Error loading payroll data: " + e.getMessage());
        }
        return payrollList;
    }

    private String getEmployeeNameById(int empId) {
        String employeeName = "";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT employee_name FROM employees WHERE id = ?")) {
            pstmt.setInt(1, empId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                employeeName = rs.getString("employee_name");
            }
        } catch (SQLException e) {
            showAlert("Error fetching employee name: " + e.getMessage());
        }
        return employeeName;
    }

    private void createExportButtons() {
        Button exportCSVButton = new Button("Export to CSV");
        exportCSVButton.setOnAction(event -> exportCSV());

        Button exportPDFButton = new Button("Export to PDF");
        exportPDFButton.setOnAction(event -> exportPDF());

        buttonContainer.getChildren().addAll(exportCSVButton, exportPDFButton);
    }

    private void exportCSV() {
        List<Payroll> payrollList = fetchPayrollData();
        if (payrollList.isEmpty()) {
            showAlert("No payroll data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                CSVExporter.exportToCSV(file.getAbsolutePath(), payrollList);
                showAlert("CSV report generated successfully:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Error exporting CSV: " + e.getMessage());
            }
        }
    }

    private void exportPDF() {
        List<Payroll> payrollList = fetchPayrollData();
        if (payrollList.isEmpty()) {
            showAlert("No payroll data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                PDFGenerator.generateMultiple(payrollList, file.getAbsolutePath());
                showAlert("PDF report generated successfully:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Error exporting PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Status");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
