package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label userLabel;

    @FXML
    void showHome(ActionEvent event) {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showBmiRecord(ActionEvent event) { loadView("bmi_record.fxml"); setTitle("BMI 记录"); }

    @FXML
    void showHistory(ActionEvent event) { loadView("history.fxml"); setTitle("历史记录"); }

    @FXML
    void showChart(ActionEvent event) { loadView("chart.fxml"); setTitle("BMI 折线图"); }

    @FXML
    void showPrediction(ActionEvent event) { loadView("prediction.fxml"); setTitle("趋势预测"); }

    @FXML
    void showDiet(ActionEvent event) { loadView("diet.fxml"); setTitle("AI 膳食推荐"); }

    @FXML
    void showFood(ActionEvent event) { loadView("food_compare.fxml"); setTitle("食物对比"); }

    @FXML
    void showFoodRank(ActionEvent event) { loadView("food_rank.fxml"); setTitle("食物榜单"); }

    @FXML
    void showProfile(ActionEvent event) { loadView("profile.fxml"); setTitle("个人信息"); }

    @FXML
    void showLogout(ActionEvent event) {
        BMIApplication.currentUserId = -1;
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(loader.load(), 400, 380));
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String title) {
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
    }
}
