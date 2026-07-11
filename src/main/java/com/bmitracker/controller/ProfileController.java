package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML
    private Label userIdLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private TextField ageField;

    @FXML
    private Label sexLabel;

    @FXML
    private TextField heightField;

    @FXML
    private TextField weightField;

    @FXML
    private CheckBox noSpicyCheck;

    @FXML
    private CheckBox noSeafoodCheck;

    @FXML
    private CheckBox lowCalCheck;

    @FXML
    private CheckBox lowCarbCheck;

    @FXML
    private Button saveButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Label errorLabel;

}
