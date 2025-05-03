package utils;

import models.Payroll;

public class Calculator {
    public static Payroll calculateSalary(String employeeName, double basicSalary, int hoursWorked) {
        double overtimePay = 0;
        if (hoursWorked > 40) { // Assuming 40 hours is full-time
            int overtimeHours = hoursWorked - 40;
            overtimePay = overtimeHours * (basicSalary / 40 * 1.5); // Time and a half for overtime
        }

        double grossSalary = basicSalary + overtimePay;
        double deductions = (grossSalary * 0.2) + 100; // Assume 20% tax and flat $100 for insurance
        double netSalary = grossSalary - deductions;

        return new Payroll(employeeName, grossSalary, deductions, netSalary);
    }
}