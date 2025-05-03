package models;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.text.Font;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PayrollReportView extends Application {

    private final List<Employee> employees;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private PieChart pieChart;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private ComboBox<Employee> employeeComboBox;
    @FXML
    private Label totalPayrollLabel;

    public PayrollReportView(List<Employee> employees) {
        this.employees = employees;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("🌟 Payroll Reports Dashboard");

        barChart = createBarChart();
        pieChart = new PieChart();
        lineChart = createLineChart();

        employeeComboBox = new ComboBox<>(FXCollections.observableArrayList(employees));
        employeeComboBox.setOnAction(event -> updateCharts());
        employeeComboBox.setPromptText("🔍 Select Employee");
        employeeComboBox.setStyle("""
            -fx-font-size: 14px;
            -fx-padding: 8px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, #bbb, 5, 0, 2, 2);
            -fx-background-color: linear-gradient(to right, #e0eafc, #cfdef3);
        """);

        employeeComboBox.setCellFactory(param -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getUsername());
            }
        });
        employeeComboBox.setButtonCell(employeeComboBox.getCellFactory().call(null));

        Button generateReportButton = styledButton("📊 Generate Report", "#6A5ACD");
        generateReportButton.setOnAction(event -> updateCharts());

        Button exportCSVButton = styledButton("📁 Export CSV", "#2E8B57");
        exportCSVButton.setOnAction(event -> exportCSV());

        Button exportPDFButton = styledButton("🧾 Export PDF", "#DC143C");
        exportPDFButton.setOnAction(event -> exportPDF());

        HBox buttonContainer = new HBox(15, generateReportButton, exportCSVButton, exportPDFButton);
        buttonContainer.setStyle("-fx-alignment: center; -fx-padding: 10px;");

        totalPayrollLabel = new Label();
        totalPayrollLabel.setFont(Font.font("Segoe UI", 18));
        totalPayrollLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2f4f4f; -fx-padding: 10px;");
        totalPayrollLabel.setEffect(new DropShadow(2, Color.LIGHTGRAY));

        VBox controls = new VBox(15, employeeComboBox, buttonContainer, totalPayrollLabel);
        controls.setStyle("""
            -fx-padding: 20px;
            -fx-background-color: linear-gradient(to bottom, #ffffff, #f2f2f2);
            -fx-border-color: #dcdcdc;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
        """);

        BorderPane root = new BorderPane();
        root.setTop(controls);

        barChart.setPrefHeight(250);
        barChart.setPrefWidth(600);
        lineChart.setPrefHeight(250);
        lineChart.setPrefWidth(600);
        pieChart.setPrefHeight(250);
        pieChart.setPrefWidth(300);

        VBox chartContainer = new VBox(15, barChart, lineChart);
        chartContainer.setStyle("""
            -fx-padding: 20px;
            -fx-background-color: #ffffff;
            -fx-border-color: #cccccc;
            -fx-border-width: 1px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
        """);

        VBox pieContainer = new VBox(pieChart);
        pieContainer.setStyle("""
            -fx-padding: 20px;
            -fx-background-color: #ffffff;
            -fx-border-color: #cccccc;
            -fx-border-width: 1px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
        """);

        HBox mainContent = new HBox(20, chartContainer, pieContainer);
        mainContent.setStyle("-fx-padding: 20px; -fx-background-color: #f9f9f9;");

        root.setCenter(mainContent);

        Scene scene = new Scene(root, 960, 620);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setScene(scene);
        stage.show();

        updateCharts();
        updateTotalPayrollSummary();
    }

    private Button styledButton(String text, String bgColor) {
        Button button = new Button(text);
        button.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 30px;
            -fx-padding: 10px 20px;
            -fx-cursor: hand;
        """.formatted(bgColor));
        button.setEffect(new DropShadow(5, Color.GRAY));

        button.setOnMouseEntered(e -> button.setScaleX(1.05));
        button.setOnMouseEntered(e -> button.setScaleY(1.05));
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        return button;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Payroll Breakdown");
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: transparent;");
        return chart;
    }

    private LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Salary Trend Over Time");
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: transparent;");
        return chart;
    }

    private void updateCharts() {
        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee != null) {
            Payroll payroll = selectedEmployee.calculatePayroll();

            barChart.getData().clear();
            XYChart.Series<String, Number> grossSeries = new XYChart.Series<>();
            grossSeries.setName("Gross Salary");
            grossSeries.getData().add(new XYChart.Data<>(selectedEmployee.getUsername(), payroll.getGrossSalary()));
            barChart.getData().add(grossSeries);

            XYChart.Series<String, Number> deductionSeries = new XYChart.Series<>();
            deductionSeries.setName("Deductions");
            deductionSeries.getData().add(new XYChart.Data<>(selectedEmployee.getUsername(), payroll.getDeductions()));
            barChart.getData().add(deductionSeries);

            pieChart.getData().clear();
            pieChart.setTitle("Salary Breakdown - " + selectedEmployee.getUsername());
            pieChart.getData().addAll(
                    new PieChart.Data("Gross", payroll.getGrossSalary()),
                    new PieChart.Data("Deductions", payroll.getDeductions()),
                    new PieChart.Data("Net", payroll.getNetSalary())
            );

            lineChart.getData().clear();
            XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
            trendSeries.setName(selectedEmployee.getUsername() + " Salary Trend");
            trendSeries.getData().add(new XYChart.Data<>("Current Month", payroll.getGrossSalary()));
            lineChart.getData().add(trendSeries);

            animateCharts();
            updateTotalPayrollSummary();
        }
    }

    private void animateCharts() {
        FadeTransition ft1 = new FadeTransition(Duration.millis(600), barChart);
        ft1.setFromValue(0);
        ft1.setToValue(1);
        ft1.play();

        FadeTransition ft2 = new FadeTransition(Duration.millis(600), lineChart);
        ft2.setFromValue(0);
        ft2.setToValue(1);
        ft2.play();

        FadeTransition ft3 = new FadeTransition(Duration.millis(600), pieChart);
        ft3.setFromValue(0);
        ft3.setToValue(1);
        ft3.play();
    }

    private void updateTotalPayrollSummary() {
        double totalPayroll = employees.stream()
                .map(Employee::calculatePayroll)
                .mapToDouble(Payroll::getNetSalary)
                .sum();
        totalPayrollLabel.setText("💰 Total Payroll Expenses: $" + String.format("%.2f", totalPayroll));
    }

    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Employee Username,Gross Salary,Deductions,Net Salary");
                writer.newLine();

                for (Employee employee : employees) {
                    Payroll payroll = employee.calculatePayroll();
                    writer.write(String.format("%s,%.2f,%.2f,%.2f%n",
                            employee.getUsername(),
                            payroll.getGrossSalary(),
                            payroll.getDeductions(),
                            payroll.getNetSalary()));
                }
                showAlert("CSV report generated successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error exporting CSV: " + e.getMessage());
            }
        }
    }

    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                for (Employee employee : employees) {
                    Payroll payroll = employee.calculatePayroll();
                    String individualFilePath = file.getAbsolutePath().replace(".pdf", "_" + employee.getUsername().replaceAll(" ", "_") + ".pdf");
                    generatePDF(payroll, individualFilePath);
                }
                showAlert("PDF reports generated successfully: Check your files.");
            } catch (Exception e) {
                showAlert("Error exporting PDF: " + e.getMessage());
            }
        }
    }

    private void generatePDF(Payroll payroll, String filePath) {
        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(filePath);
             com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
             com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf)) {

            document.add(new com.itextpdf.layout.element.Paragraph("Payroll Report for: " + payroll.getEmployeeName()));
            document.add(new com.itextpdf.layout.element.Paragraph("Gross Salary: $" + String.format("%.2f", payroll.getGrossSalary())));
            document.add(new com.itextpdf.layout.element.Paragraph("Deductions: $" + String.format("%.2f", payroll.getDeductions())));
            document.add(new com.itextpdf.layout.element.Paragraph("Net Salary: $" + String.format("%.2f", payroll.getNetSalary())));

        } catch (Exception e) {
            showAlert("Error generating PDF: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
