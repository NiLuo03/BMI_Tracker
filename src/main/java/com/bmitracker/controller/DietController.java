package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.BmiService;
import com.bmitracker.service.UserService;
import com.bmitracker.util.CozeClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class DietController {

    @FXML private Label breakfastLabel;
    @FXML private Label lunchLabel;
    @FXML private Label dinnerLabel;
    @FXML private Label totalCalLabel;

    private final UserService userService = new UserService();
    private final BmiService bmiService = new BmiService();

    @FXML
    void handleGenerate(ActionEvent event) {
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user == null) return;

        double bmi = bmiService.calculateBMI(user.getHeight(), user.getWeight());
        String status = bmiService.getHealthStatus(bmi);

        String json = CozeClient.getDietRecommendation(
                user.getUserAge(), user.getSex(), user.getHeight(), user.getWeight(),
                bmi, status, user.getPreferences());

        if (json == null || json.isEmpty()) {
            showAlert("AI 服务繁忙，请稍后再试");
            return;
        }

        // 简单 JSON 解析（不引入外部库）
        try {
            String clean = json.replaceAll("[{}\"]", "");
            String[] parts = clean.split(",");
            for (String part : parts) {
                String[] kv = part.split(":", 2);
                if (kv.length < 2) continue;
                String key = kv[0].trim().toLowerCase();
                String val = kv[1].trim();
                if (key.contains("breakfast")) breakfastLabel.setText(val);
                else if (key.contains("lunch")) lunchLabel.setText(val);
                else if (key.contains("dinner")) dinnerLabel.setText(val);
                else if (key.contains("totalcal") || key.contains("totalCal")) totalCalLabel.setText(val);
            }
        } catch (Exception e) {
            showAlert("AI 返回数据解析失败，请稍后再试");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
