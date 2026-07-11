module com.bmitracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.net.http;

    opens com.bmitracker to javafx.fxml;
    opens com.bmitracker.controller to javafx.fxml;
    exports com.bmitracker;
    exports com.bmitracker.util;
    exports com.bmitracker.model;
    exports com.bmitracker.dao;
    exports com.bmitracker.controller;
    exports com.bmitracker.service;
}