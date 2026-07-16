package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private StackPane glassPanel;
    @FXML private Label pageTitle;
    @FXML private Region backdrop;
    @FXML private Button toggleNavBtn;
    @FXML private Button btnHome, btnBmi, btnHistory, btnChart, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord;
    @FXML private VBox sidebar;
    @FXML private Label avatarLabel, sidebarUserName, dateLabel;
    @FXML private Label welcomeLabel, bmiStatusLabel, trendLabel;
    @FXML private Label dashBmi, dashStatus, dashIdealWeight, dashRecords, dashTrend;
    @FXML private Label historySummary;

    private boolean navExpanded = true;
    private final BmiService bmiService = new BmiService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    void toggleNav() {
        navExpanded = !navExpanded;
        double target = navExpanded ? 150 : 48;
        Button[] btns = {btnHome, btnBmi, btnHistory, btnChart, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord};
        String[] icons = {"🏠", "📊", "📋", "📈", "🔮", "🥗", "🍎", "🏆", "📝"};
        String[] texts = {"首页", "BMI 记录", "历史记录", "折线图", "趋势预测", "膳食推荐", "食物对比", "食物榜单", "膳食记录"};

        Timeline anim = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebar.prefWidthProperty(), target))
        );
        anim.play();

        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(navExpanded ? "  " + texts[i] : icons[i]);
            btns[i].setMaxWidth(navExpanded ? Double.MAX_VALUE : 38);
            btns[i].setAlignment(navExpanded ? javafx.geometry.Pos.CENTER_LEFT : javafx.geometry.Pos.CENTER);
        }
        toggleNavBtn.setText(navExpanded ? "« 收起" : "»");
    }

    @FXML void setBackdrop1() { backdrop.setStyle("-fx-background-color: #050f0a;"); }
    @FXML void setBackdrop2() { backdrop.setStyle("-fx-background-color: #0a0a1a;"); }
    @FXML void setBackdrop3() { backdrop.setStyle("-fx-background-color: #100a1a;"); }
    @FXML void setBackdrop4() { backdrop.setStyle("-fx-background-color: #000000;"); }
    @FXML void setBackdrop5() { backdrop.setStyle("-fx-background-color: #111111;"); }
    @FXML void setBackdrop6() { backdrop.setStyle("-fx-background-color: #ffffff;"); }

    @FXML
    void initialize() {
        loadSidebarUser();
        loadDashboardData();
        if (dateLabel != null) dateLabel.setText(LocalDate.now().format(DATE_FMT));
        Platform.runLater(() -> {
            AIChatController ai = AIChatController.getInstance();
            ai.setMainStage((Stage) contentPane.getScene().getWindow());
            ai.show();
        });
    }

    private void loadSidebarUser() {
        if (sidebarUserName != null) sidebarUserName.setText("用户 " + BMIApplication.currentUserId);
        if (avatarLabel != null) avatarLabel.setText(String.valueOf(BMIApplication.currentUserId).charAt(0) + "");
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
                Platform.runLater(() -> {
                    if (records == null || records.isEmpty()) {
                        if (dashRecords != null) dashRecords.setText("0");
                        return;
                    }
                    BmiRecord latest = records.get(0);
                    double bmi = latest.getBmi();
                    String status = latest.getStatus();
                    if (dashBmi != null) dashBmi.setText(String.format("%.1f", bmi));
                    if (dashStatus != null) {
                        dashStatus.setText(status);
                        dashStatus.getStyleClass().clear();
                        if ("正常".equals(status)) dashStatus.getStyleClass().add("data-value-green");
                        else if ("偏瘦".equals(status) || "超重".equals(status)) dashStatus.getStyleClass().add("data-value-yellow");
                        else dashStatus.getStyleClass().add("data-value-red");
                    }
                    if (dashRecords != null) dashRecords.setText(String.valueOf(records.size()));
                    if (bmiStatusLabel != null) bmiStatusLabel.setText(String.format("BMI %.1f · %s", bmi, status));
                    if (dashIdealWeight != null) dashIdealWeight.setText(String.format("%.0f–%.0f kg", latest.getHeight() * 0.185 * latest.getHeight() / 100, latest.getHeight() * 0.24 * latest.getHeight() / 100));

                    if (records.size() >= 2) {
                        double prev = records.get(1).getBmi();
                        if (dashTrend != null) {
                            if (bmi > prev) { dashTrend.setText("↑ 上升"); dashTrend.getStyleClass().setAll("data-value-red"); }
                            else if (bmi < prev) { dashTrend.setText("↓ 下降"); dashTrend.getStyleClass().setAll("data-value-green"); }
                            else { dashTrend.setText("→ 持平"); dashTrend.getStyleClass().setAll("data-value"); }
                        }
                        if (trendLabel != null) trendLabel.setText(bmi > prev ? "📈 趋势上升" : bmi < prev ? "📉 趋势下降" : "➡ 趋势平稳");
                    }

                    if (historySummary != null) historySummary.setText(String.format("共%d条记录，最近BMI %.1f", records.size(), bmi));
                });
            } catch (Exception ignored) {}
        }).start();
    }

    @FXML
    void showHome(ActionEvent event) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("首页");
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
    void showMealRecord(ActionEvent event) { loadView("meal_record.fxml"); setTitle("膳食记录"); }
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

    private void loadView(String fxml) {
        new Thread(() -> {
            try {
                Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
                Platform.runLater(() -> glassPanel.getChildren().setAll(view));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setTitle(String title) {
        if (pageTitle != null) pageTitle.setText(title);
    }
}
