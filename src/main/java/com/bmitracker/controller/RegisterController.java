package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> sexCombo;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink backLink;

    @FXML
    private Label errorLabel;

}
