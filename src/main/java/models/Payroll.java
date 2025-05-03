package models;

public class Payroll {
    private String employeeName; // Employee's name
    private double grossSalary;   // Total salary before deductions
    private double deductions;     // Total deductions from gross salary
    private double netSalary;      // Salary after deductions

    // Constructor
    public Payroll(String employeeName, double grossSalary, double deductions, double netSalary) {
        this.employeeName = employeeName;
        this.grossSalary = grossSalary;
        this.deductions = deductions;
        this.netSalary = netSalary;
    }

    // Getters
    public String getEmployeeName() {
        return employeeName;
    }

    public double getGrossSalary() {
        return grossSalary;
    }

    public double getDeductions() {
        return deductions;
    }

    public double getNetSalary() {
        return netSalary;
    }

    // Setters (optional, depending on your needs)
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setGrossSalary(double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public void setDeductions(double deductions) {
        this.deductions = deductions;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }
}