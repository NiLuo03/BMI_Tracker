package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label userLabel;
    @FXML private Region backdrop;

    // 背景主题切换，让用户自定义界面氛围
    @FXML void setBackdrop1() { backdrop.setStyle("-fx-background-color: #050f0a;"); removeLightTheme(); }
    @FXML void setBackdrop2() { backdrop.setStyle("-fx-background-color: #0a0a1a;"); removeLightTheme(); }
    @FXML void setBackdrop3() { backdrop.setStyle("-fx-background-color: #100a1a;"); removeLightTheme(); }
    @FXML void setBackdrop4() { backdrop.setStyle("-fx-background-color: #000000;"); removeLightTheme(); }
    @FXML void setBackdrop5() { backdrop.setStyle("-fx-background-color: #111111;"); removeLightTheme(); }
    @FXML void setBackdrop6() { backdrop.setStyle("-fx-background-color: #ffffff;"); addLightTheme(); }

    private void addLightTheme() {
        if (backdrop.getScene() != null && backdrop.getScene().getRoot() != null) {
            backdrop.getScene().getRoot().getStyleClass().add("light-theme");
        }
    }
    private void removeLightTheme() {
        if (backdrop.getScene() != null && backdrop.getScene().getRoot() != null) {
            backdrop.getScene().getRoot().getStyleClass().remove("light-theme");
        }
    }

    @FXML
    void initialize() {
        // 应用启动后自动弹出 AI 悬浮助手
        Platform.runLater(() -> {
            AIChatController ai = AIChatController.getInstance();
            ai.setMainStage((Stage) contentPane.getScene().getWindow());
            ai.show();
        });
    }

    @FXML
    void showHome(ActionEvent event) {
        // 刷新首页时完整重载主布局，保留用户选中的背景色
        try {
            String savedColor = backdrop.getStyle();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            MainController newCtrl = loader.getController();
            if (newCtrl != null && newCtrl.backdrop != null) {
                newCtrl.backdrop.setStyle(savedColor);
            }
            stage.setScene(scene);
            stage.centerOnScreen();
            // 恢复浅色主题 class
            if (newCtrl != null && savedColor != null && savedColor.contains("#ffffff")) {
                newCtrl.addLightTheme();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 各功能导航按钮：统一走 loadView 切换内容区、更新标题
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
        AIChatController.getInstance().hide();
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            scene.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#050f0a")),
                    new Stop(1, Color.web("#000000"))));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将指定 fxml 渲染到中央内容区域，实现子页面切换
    private void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String title) {
        if (pageTitle != null) pageTitle.setText(title);
    }
}
