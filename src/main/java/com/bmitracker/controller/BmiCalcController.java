package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BmiCalcController {

    @FXML
    private TextField heightField;

    @FXML
    private TextField weightField;

    @FXML
    private Button calcButton;

    @FXML
    private Label resultLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label errorLabel;

}
