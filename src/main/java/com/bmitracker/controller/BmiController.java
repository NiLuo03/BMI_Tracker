package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.BmiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class BmiController {

    @FXML private TextField heightField;
    @FXML private TextField weightField;

    private final BmiService bmiService = new BmiService();

    @FXML
    void handleCalculate(ActionEvent event) {
        String heightText = heightField.getText();
        String weightText = weightField.getText();
        double height, weight;
        try {
            height = Double.parseDouble(heightText);
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            showAlert("请输入有效的身高和体重");
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
            showAlert(error);
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("BMI结果"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
