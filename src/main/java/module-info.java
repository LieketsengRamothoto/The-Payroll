module com.example.demo2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires kernel;
    requires layout;

    opens com.example.demo2 to javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base;

    exports com.example.demo2;
    exports controllers;
}
