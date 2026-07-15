package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.BmiService;
import com.bmitracker.service.UserService;
import com.bmitracker.util.CozeClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DietController {

    @FXML private Label breakfastLabel;
    @FXML private Label lunchLabel;
    @FXML private Label dinnerLabel;
    @FXML private Label totalCalLabel;

    private final UserService userService = new UserService();
    private final BmiService bmiService = new BmiService();

    @FXML
    void handleGenerate(ActionEvent event) {
        // 获取用户资料并计算当前 BMI 与健康状态
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user == null) return;

        double bmi = bmiService.calculateBMI(user.getHeight(), user.getWeight());
        String status = bmiService.getHealthStatus(bmi);

        // 禁用按钮，组装 AI 请求参数
        Button btn = (Button) event.getSource();
        btn.setDisable(true);

        int age = user.getUserAge();
        int sex = user.getSex();
        double height = user.getHeight();
        double weight = user.getWeight();
        String preferences = user.getPreferences();

        // 后台线程请求 AI，避免阻塞 UI 线程
        new Thread(() -> {
            try {
                String json = CozeClient.getDietRecommendation(age, sex, height, weight, bmi, status, preferences);
                Platform.runLater(() -> {
                    if (json == null || json.isEmpty()) {
                        showAlert("AI 服务繁忙，请稍后再试");
                    } else {
                        try {
                            // 手工解析简易 JSON 键值对，填充三餐和总热量
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
                    btn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("AI 服务繁忙，请稍后再试");
                    btn.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
