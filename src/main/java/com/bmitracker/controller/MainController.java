package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.TitleBar;
import com.bmitracker.component.WheelPicker;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    private static MainController instance;

    @FXML private StackPane contentPane;
    @FXML private StackPane glassPanel;
    @FXML private Label pageTitle;
    @FXML private TitleBar titleBar;
    @FXML private StackPane root;
    @FXML private Rectangle rootClip;
    @FXML private Region backdrop;
    @FXML private Button toggleNavBtn;
    private Node quizView;
    @FXML private Button btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord, btnQuiz;
    @FXML private VBox sidebar;
    @FXML private Label avatarLabel, sidebarUserName, dateLabel;
    @FXML private Label welcomeLabel, bmiStatusLabel, trendLabel;
    @FXML private Label dashBmi, dashStatus, dashIdealWeight, dashRecords, dashTrend;
    @FXML private Label historySummary;
    @FXML private VBox homeContent;
    @FXML private TextField homeHeightField;
    @FXML private TextField homeWeightField;

    private Popup homeHeightPopup;
    private Popup homeWeightPopup;

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
        Button[] btns = {btnHome, btnBmi, btnHistory, btnPredict, btnDiet, btnCompare, btnRank, btnMealRecord, btnQuiz};
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
        rootClip.widthProperty().bind(root.widthProperty());
        rootClip.heightProperty().bind(root.heightProperty());
        instance = this;
        loadSidebarUser();
        loadDashboardData();
        if (dateLabel != null) dateLabel.setText(LocalDate.now().format(DATE_FMT));
        if (root != null) rootPane = root;
        changeBackdrop("#ffffff");

        setupHomePickers();

        Platform.runLater(() -> {
            AIChatController ai = AIChatController.getInstance();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            ai.setMainStage(stage);
            ai.show();
            stage.focusedProperty().addListener((o, ov, focused) -> {
                if (focused) return;
                if (homeHeightPopup != null) homeHeightPopup.hide();
                if (homeWeightPopup != null) homeWeightPopup.hide();
            });
        });
    }

    private void setupHomePickers() {
        homeHeightField.setEditable(false);
        homeHeightField.setFocusTraversable(false);
        homeWeightField.setEditable(false);
        homeWeightField.setFocusTraversable(false);

        homeHeightField.sceneProperty().addListener((obs, old, sc) -> {
            if (sc == null) return;
            sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissHomePopup(e, homeHeightPopup, homeHeightField));
            sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissHomePopup(e, homeWeightPopup, homeWeightField));
        });

        homeHeightField.setOnMouseClicked(e -> {
            if (homeHeightPopup != null && homeHeightPopup.isShowing()) { homeHeightPopup.hide(); return; }
            if (homeHeightField.getText() == null || homeHeightField.getText().trim().isEmpty())
                homeHeightField.setText("165");
            showHomePicker(homeHeightField, homeHeightPopup, 100, 220, 165, p -> homeHeightPopup = p);
        });

        homeWeightField.setOnMouseClicked(e -> {
            if (homeWeightPopup != null && homeWeightPopup.isShowing()) { homeWeightPopup.hide(); return; }
            if (homeWeightField.getText() == null || homeWeightField.getText().trim().isEmpty())
                homeWeightField.setText("60");
            showHomePicker(homeWeightField, homeWeightPopup, 25, 250, 60, p -> homeWeightPopup = p);
        });
    }

    private void dismissHomePopup(MouseEvent event, Popup popup, TextField field) {
        if (popup == null || !popup.isShowing()) return;
        EventTarget target = event.getTarget();
        if (target instanceof Node) {
            Node node = (Node) target;
            if (node == field) return;
            for (Node n : popup.getContent()) {
                if (isNodeChild(node, n)) return;
            }
        }
        popup.hide();
    }

    private void showHomePicker(TextField field, Popup existingPopup, int min, int max, int defaultVal,
                                java.util.function.Consumer<Popup> setter) {
        if (existingPopup != null) existingPopup.hide();
        double pw = Math.max(field.getWidth(), 100);
        WheelPicker<Integer> wheel = new WheelPicker<>();
        wheel.setVisibleItems(3);
        wheel.setItemHeight(32);
        wheel.setPrefWidth(pw);
        wheel.setPrefHeight(110);
        List<Integer> items = new ArrayList<>();
        for (int i = min; i <= max; i++) items.add(i);
        wheel.setItems(items);
        int current = defaultVal;
        try {
            current = Integer.parseInt(field.getText().trim());
            if (current < min || current > max) current = defaultVal;
        } catch (NumberFormatException ignored) {}
        final int def = current;
        wheel.valueProperty().addListener((obs, old, val) -> {
            if (val != null) field.setText(String.valueOf(val));
        });
        wheel.setLightTheme(isLightTheme());
        StackPane container = new StackPane(wheel);
        container.setPadding(new Insets(12, 0, 12, 0));
        container.setMinWidth(pw);
        container.getStyleClass().add("wheel-popup");
        if (isLightTheme()) container.getStyleClass().add("light-theme");
        container.setVisible(false);
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        popup.getContent().add(container);
        setter.accept(popup);
        Bounds bounds = field.localToScreen(field.getBoundsInLocal());
        popup.show(field, bounds.getMinX(), bounds.getMaxY());
        Platform.runLater(() -> {
            wheel.resize(pw, 180);
            wheel.setSelectedIndex(def - min);
            container.setVisible(true);
        });
    }

    private boolean isLightTheme() {
        if (rootPane != null) return rootPane.getStyleClass().contains("light-theme");
        return false;
    }

    private static boolean isNodeChild(Node target, Node root) {
        Node n = target;
        while (n != null) {
            if (n == root) return true;
            n = n.getParent();
        }
        return false;
    }

    @FXML
    void handleHomeRecord(ActionEvent event) {
        String heightText = homeHeightField.getText();
        String weightText = homeWeightField.getText();
        if (heightText == null || heightText.trim().isEmpty()) { homeHeightField.setText("165"); heightText = "165"; }
        if (weightText == null || weightText.trim().isEmpty()) { homeWeightField.setText("60"); weightText = "60"; }
        try {
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);
            String error = bmiService.saveRecord(BMIApplication.currentUserId, height, weight);
            if (error == null) {
                double bmi = bmiService.calculateBMI(height, weight);
                String status = bmiService.getHealthStatus(bmi);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("BMI结果"); a.setHeaderText(null);
                a.setContentText(String.format("您的 BMI 为 %.1f，状态：%s", bmi, status));
                a.showAndWait();
                loadDashboardData();
            }
        } catch (NumberFormatException ignored) {}
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
    void showQuiz(ActionEvent event) {
        glassPanel.getChildren().setAll(loadingLabel());
        Platform.runLater(() -> {
            try {
                if (quizView == null) {
                    quizView = FXMLLoader.load(getClass().getResource("/fxml/quiz.fxml"));
                }
                glassPanel.getChildren().setAll(quizView);
            } catch (Exception e) {
                e.printStackTrace();
                Label err = new Label("加载失败: " + e.getMessage());
                err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                StackPane.setAlignment(err, javafx.geometry.Pos.CENTER);
                glassPanel.getChildren().setAll(err);
            }
        });
        setTitle("健康问答");
    }
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
