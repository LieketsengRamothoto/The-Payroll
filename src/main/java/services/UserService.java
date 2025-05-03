package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.User;
import com.example.demo2.Database;

public class UserService {

    // Authenticate user
    public User authenticate(String username, String password) throws SQLException {
        Connection conn = Database.getConnection(); // Assuming you have a Database connection method

        // Check in the 'users' table for admin role
        String queryAdmin = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'admin'";
        PreparedStatement stmtAdmin = conn.prepareStatement(queryAdmin);
        stmtAdmin.setString(1, username);
        stmtAdmin.setString(2, password);

        ResultSet rsAdmin = stmtAdmin.executeQuery();

        if (rsAdmin.next()) {
            User user = new User();
            user.setUsername(rsAdmin.getString("username"));
            user.setPassword(rsAdmin.getString("password"));
            user.setRole(rsAdmin.getString("role"));
            user.setEmpId(rsAdmin.getInt("emp_id"));
            return user;
        }

        // Check in the 'employees' table for employee role
        String queryEmployee = "SELECT * FROM employees WHERE username = ? AND password = ?";
        PreparedStatement stmtEmployee = conn.prepareStatement(queryEmployee);
        stmtEmployee.setString(1, username);
        stmtEmployee.setString(2, password);

        ResultSet rsEmployee = stmtEmployee.executeQuery();

        if (rsEmployee.next()) {
            User user = new User();
            user.setUsername(rsEmployee.getString("username"));
            user.setPassword(rsEmployee.getString("password"));
            user.setRole("employee"); // Hardcoded role as employee
            user.setEmpId(rsEmployee.getInt("emp_id"));
            return user;
        }

        // Return null if authentication fails
        return null;
    }
}
