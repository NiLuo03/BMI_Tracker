package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.BmiService;
import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class BmiController {

    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TextField allergensField;
    @FXML private TextField diseasesField;

    private final BmiService bmiService = new BmiService();
    private final UserService userService = new UserService();

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

        // 保存健康档案信息
        String allergens = allergensField.getText() != null ? allergensField.getText().trim() : "";
        String diseases = diseasesField.getText() != null ? diseasesField.getText().trim() : "";
        if (!allergens.isEmpty() || !diseases.isEmpty()) {
            User user = userService.getUserById(BMIApplication.currentUserId);
            if (user != null) {
                if (!allergens.isEmpty()) {
                    user.setAllergens(allergens.isEmpty() ? user.getAllergens() : allergens);
                }
                if (!diseases.isEmpty()) {
                    user.setChronicDiseases(diseases.isEmpty() ? user.getChronicDiseases() : diseases);
                }
                userService.updateProfile(user);
            }
        }

        // 保存 BMI 记录
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
