module com.example.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.example.test to javafx.fxml;
    exports com.example.test;
    exports com.bmitracker.util;
    exports com.bmitracker.model;
    exports com.bmitracker.dao;
}