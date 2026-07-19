package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.BmiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class BmiController {

    @FXML private TextField heightField;
    @FXML private TextField weightField;

    private final BmiService bmiService = new BmiService();
    private Label heightError;
    private Label weightError;

    @FXML
    void handleCalculate(ActionEvent event) {
        clearFieldError(heightField);
        clearFieldError(weightField);
        String heightText = heightField.getText();
        String weightText = weightField.getText();
        double height, weight;
        try {
            height = Double.parseDouble(heightText);
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            showFieldError(heightField, "请输入有效的身高");
            showFieldError(weightField, "请输入有效的体重");
            return;
        }
        String error = bmiService.saveRecord(BMIApplication.currentUserId, height, weight);
        if (error == null) {
            double bmi = bmiService.calculateBMI(height, weight);
            String status = bmiService.getHealthStatus(bmi);
            showInfo(String.format("您的 BMI 为 %.1f，状态：%s", bmi, status));
            heightField.clear();
            weightField.clear();
        } else {
            showFieldError(heightField, error);
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("BMI结果"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showFieldError(TextField field, String msg) {
        field.getStyleClass().add("text-field-error");
        Label errorLabel = (field == heightField) ? heightError : weightError;
        if (errorLabel == null) {
            errorLabel = new Label(msg);
            errorLabel.getStyleClass().add("error-label");
            VBox parent = (VBox) field.getParent();
            int idx = parent.getChildren().indexOf(field);
            parent.getChildren().add(idx + 1, errorLabel);
            if (field == heightField) heightError = errorLabel;
            else weightError = errorLabel;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearFieldError(TextField field) {
        field.getStyleClass().removeAll("text-field-error");
        Label errorLabel = (field == heightField) ? heightError : weightError;
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }
}

