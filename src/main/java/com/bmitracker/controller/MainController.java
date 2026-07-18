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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    private static MainController instance;

    @FXML private StackPane contentPane;
    @FXML private StackPane glassPanel;
    @FXML private Label pageTitle;
    @FXML private Region backdrop;
    @FXML private Button toggleNavBtn;
    @FXML private Button btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord;
    @FXML private VBox sidebar;
    @FXML private Label avatarLabel, sidebarUserName, dateLabel;
    @FXML private Label welcomeLabel, bmiStatusLabel, trendLabel;
    @FXML private Label dashBmi, dashStatus, dashIdealWeight, dashRecords, dashTrend;
    @FXML private Label historySummary;
    @FXML private VBox homeContent;

    private boolean navExpanded = true;
    private final BmiService bmiService = new BmiService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Pane rootPane;

    private static final String[] BACKDROP_COLORS = {
        "#050f0a", "#0a0a1a", "#100a1a", "#000000", "#111111", "#ffffff"
    };

    @FXML
    void toggleNav() {
        navExpanded = !navExpanded;
        double target = navExpanded ? 150 : 48;
        Button[] btns = {btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord};
        String[] icons = {"🏠", "📊", "📊", "🔮", "🥗", "🍎", "🏆", "📝"};
        String[] texts = {"首页", "BMI 记录", "数据分析", "趋势预测", "膳食推荐", "食物对比", "食物榜单", "膳食记录"};

        Timeline anim = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebar.prefWidthProperty(), target))
        );
        anim.play();

        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(navExpanded ? icons[i] + "  " + texts[i] : icons[i]);
            btns[i].setMaxWidth(navExpanded ? Double.MAX_VALUE : 38);
            btns[i].setAlignment(navExpanded ? javafx.geometry.Pos.CENTER_LEFT : javafx.geometry.Pos.CENTER);
        }
        toggleNavBtn.setText(navExpanded ? "« 收起" : "»");
    }

    public void changeBackdrop(String hexColor) {
        backdrop.setStyle("-fx-background-color: " + hexColor + ";");
        if (rootPane == null) return;
        boolean isLight = "#ffffff".equals(hexColor);
        rootPane.getStyleClass().removeAll("light-theme");
        if (isLight) {
            rootPane.getStyleClass().add("light-theme");
        }
    }

    public void changeBackdrop(int idx) {
        if (idx >= 0 && idx < BACKDROP_COLORS.length) {
            changeBackdrop(BACKDROP_COLORS[idx]);
        }
    }

    @FXML
    void initialize() {
        instance = this;
        loadSidebarUser();
        loadDashboardData();
        if (dateLabel != null) dateLabel.setText(LocalDate.now().format(DATE_FMT));
        if (backdrop != null && backdrop.getParent() instanceof Pane p) {
            rootPane = p;
        }
        changeBackdrop("#ffffff");
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
        if (homeContent != null && glassPanel != null) {
            glassPanel.getChildren().setAll(homeContent);
            loadDashboardData();
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
    void showPersonalize(ActionEvent event) { loadView("personalize.fxml"); setTitle("个性设置"); }
    @FXML
    void showDiet(ActionEvent event) { loadView("diet.fxml"); setTitle("AI 膳食推荐"); }

    public static MainController getInstance() { return instance; }
    public Region getBackdrop() { return backdrop; }
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
        glassPanel.getChildren().setAll(loadingLabel());
        Platform.runLater(() -> {
            try {
                Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
                glassPanel.getChildren().setAll(view);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                if (msg != null && msg.length() > 120) msg = msg.substring(0, 120) + "…";
                Label err = new Label("加载失败: " + (msg != null ? msg : e.getClass().getSimpleName()));
                err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                err.setWrapText(true);
                err.setMaxWidth(500);
                StackPane.setAlignment(err, javafx.geometry.Pos.CENTER);
                glassPanel.getChildren().setAll(err);
            }
        });
    }

    private Label loadingLabel() {
        Label l = new Label("加载中…");
        l.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 14px;");
        StackPane.setAlignment(l, javafx.geometry.Pos.CENTER);
        return l;
    }

    private Label errorLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
        StackPane.setAlignment(l, javafx.geometry.Pos.CENTER);
        return l;
    }

    private void setTitle(String title) {
        if (pageTitle != null) pageTitle.setText(title);
    }
}
