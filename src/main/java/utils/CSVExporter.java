package utils;

import models.Payroll;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {
    public static void exportToCSV(String filePath, List<Payroll> payrollList) {
        try (FileWriter csvWriter = new FileWriter(filePath)) {
            csvWriter.append("Employee Name,Gross Salary,Deductions,Net Salary\n");

            for (Payroll payroll : payrollList) {
                csvWriter.append(payroll.getEmployeeName()).append(",")
                        .append(String.valueOf(payroll.getGrossSalary())).append(",")
                        .append(String.valueOf(payroll.getDeductions())).append(",")
                        .append(String.valueOf(payroll.getNetSalary())).append("\n");
            }
            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}