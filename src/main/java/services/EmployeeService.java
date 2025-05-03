package services;

import com.example.demo2.Database;
import models.Employee;
import models.Payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {

    public String generatePayslip(int empId) {
        Employee employee = getEmployeeById(empId);

        if (employee == null) {
            return "Employee not found.";
        }

        Payroll payroll = employee.calculatePayroll();

        return "Payslip for " + employee.getUsername() + ":\n" +
                "Employee ID: " + employee.getEmpId() + "\n" +
                "Department: " + employee.getDepartment() + "\n" +
                "Position: " + employee.getPosition() + "\n" +
                "Hours Worked: " + employee.getHours() + "\n" +
                "Gross Salary: $" + String.format("%.2f", payroll.getGrossSalary()) + "\n" +
                "Deductions: $" + String.format("%.2f", payroll.getDeductions()) + "\n" +
                "Net Salary: $" + String.format("%.2f", payroll.getNetSalary());
    }

    public Employee getEmployeeById(int empId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM employees WHERE emp_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployeeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee authenticateEmployee(String username, String password) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM employees WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployeeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM employees";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(extractEmployeeFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    public boolean addEmployee(Employee employee) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO employees (username, department, position, salary, hours, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, employee.getUsername());
            stmt.setString(2, employee.getDepartment());
            stmt.setString(3, employee.getPosition());
            stmt.setDouble(4, employee.getSalary());
            stmt.setInt(5, employee.getHours());
            stmt.setString(6, employee.getPassword());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("emp_id"),
                rs.getString("username"),
                rs.getString("department"),
                rs.getString("position"),
                rs.getDouble("salary"),
                rs.getInt("hours"),
                rs.getString("password")
        );
    }
}
