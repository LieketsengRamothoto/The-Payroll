package models;

public class Employee {
    private int empId;
    private String username;
    private String department;
    private String position;
    private double salary;
    private int hours;
    private String password;

    public Employee(int empId, String username, String department, String position, double salary, int hours, String password) {
        this.empId = empId;
        this.username = username;
        this.department = department;
        this.position = position;
        this.salary = salary;
        this.hours = hours;
        this.password = password;
    }

    public boolean authenticate(String enteredUsername, String enteredPassword) {
        return this.username.equals(enteredUsername) && this.password.equals(enteredPassword);
    }

    public Payroll calculatePayroll() {
        double gross = this.salary;
        double deductions = gross * 0.10; // 10% deduction
        double net = gross - deductions;
        return new Payroll(username, gross, deductions, net);
    }

    // Getters
    public int getEmpId() {
        return empId;
    }

    public String getUsername() {
        return username;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public double getSalary() {
        return salary;
    }

    public int getHours() {
        return hours;
    }

    public String getPassword() {
        return password;
    }

    // Optional setter if you want to update empId (rare)
    public void setEmpId(int empId) {
        this.empId = empId;
    }
}
