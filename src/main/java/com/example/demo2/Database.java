package com.example.demo2;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/payroll";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    static {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Employees Table
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "emp_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "department VARCHAR(30), " +
                    "position VARCHAR(30), " +
                    "salary DOUBLE, " +
                    "hours INT)");

            // Users Table (includes role for both admin and employee)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "emp_id INT, " +
                    "FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE SET NULL)");

            // Payroll Table
            stmt.execute("CREATE TABLE IF NOT EXISTS payroll (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "emp_id INT, " +
                    "date DATE, " +
                    "gross DOUBLE, " +
                    "deductions DOUBLE, " +
                    "net DOUBLE, " +
                    "FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
