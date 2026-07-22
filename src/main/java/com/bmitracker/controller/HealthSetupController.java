package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.UserService;
import com.bmitracker.util.ParticleTextCanvas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class HealthSetupController {

    private static final String[] ALL_ALLERGENS = {
        "花生", "牛奶", "鸡蛋", "虾", "蟹", "鱼类", "大豆", "小麦",
        "坚果（核桃）", "坚果（杏仁）", "芒果", "桃子", "草莓", "海鲜",
        "花粉", "尘螨", "猫毛", "狗毛", "青霉素", "头孢菌素",
        "磺胺类药物", "阿司匹林", "碘伏", "乳胶", "镍", "紫外线"
    };

    private static final String[] ALL_DISEASES = {
        "高血压", "2型糖尿病", "1型糖尿病", "高血脂", "冠心病",
        "哮喘", "慢性支气管炎", "慢性胃炎", "胃溃疡",
        "骨关节炎", "类风湿关节炎", "甲状腺功能亢进", "甲状腺功能减退",
        "慢性肾病", "脂肪肝", "痛风", "贫血", "骨质疏松",
        "抑郁症", "焦虑症", "慢性乙型肝炎", "结核病",
        "克罗恩病", "溃疡性结肠炎", "鼻窦炎", "偏头痛",
        "腰椎间盘突出", "颈椎病"
    };

    @FXML private StackPane particlePane;
    @FXML private TextField allergenSearchField, diseaseSearchField;
    @FXML private FlowPane allergenOptionsPane, allergenSelectedPane;
    @FXML private FlowPane diseaseOptionsPane, diseaseSelectedPane;
    @FXML private Label stepTitle, stepDesc;
    @FXML private Label dot1, dot2, dot3, dot4;
    @FXML private VBox step1Panel, step2Panel, step3Panel, step4Panel;
    @FXML private Button step1Btn, step2Btn, step3Btn;
    @FXML private Label allergenNoneLabel, diseaseNoneLabel;
    @FXML private VBox root;
    @FXML private Rectangle rootClip;

    private final UserService userService = new UserService();
    private final Set<String> selectedAllergens = new HashSet<>();
    private final Set<String> selectedDiseases = new HashSet<>();
    private boolean editMode = false;

    public void setEditMode(boolean editMode) { this.editMode = editMode; }

    @FXML
    void initialize() {
        rootClip.widthProperty().bind(root.widthProperty());
        rootClip.heightProperty().bind(root.heightProperty());
        ParticleTextCanvas canvas = new ParticleTextCanvas(1200, 800,
                new String[]{"HEALTH", "PROFILE", "ALLERGEN", "DISEASE", "RECORD"});
        particlePane.getChildren().clear();
        particlePane.getChildren().add(canvas);

        allergenSearchField.setOnAction(e -> searchAllergens());
        diseaseSearchField.setOnAction(e -> searchDiseases());

        showAllOptions(allergenOptionsPane, ALL_ALLERGENS, selectedAllergens, true);
    }

    public void prefillData(String allergens, String diseases) {
        if (allergens != null && !allergens.trim().isEmpty()) {
            String[] parts = allergens.split(",");
            for (String a : parts) {
                String t = a.trim();
                if (!t.isEmpty()) selectedAllergens.add(t);
            }
            showAllOptions(allergenOptionsPane, ALL_ALLERGENS, selectedAllergens, true);
        }
        if (diseases != null && !diseases.trim().isEmpty()) {
            String[] parts = diseases.split(",");
            for (String d : parts) {
                String t = d.trim();
                if (!t.isEmpty()) selectedDiseases.add(t);
            }
        }
    }

    @FXML void searchAllergens() {
        String kw = allergenSearchField.getText().trim().toLowerCase();
        String[] filtered = kw.isEmpty() ? ALL_ALLERGENS :
            Arrays.stream(ALL_ALLERGENS).filter(s -> s.contains(kw) || s.toLowerCase().contains(kw)).toArray(String[]::new);
        showAllOptions(allergenOptionsPane, filtered, selectedAllergens, true);
    }

    @FXML void searchDiseases() {
        String kw = diseaseSearchField.getText().trim().toLowerCase();
        String[] filtered = kw.isEmpty() ? ALL_DISEASES :
            Arrays.stream(ALL_DISEASES).filter(s -> s.contains(kw) || s.toLowerCase().contains(kw)).toArray(String[]::new);
        showAllOptions(diseaseOptionsPane, filtered, selectedDiseases, false);
    }

    private void showAllOptions(FlowPane pane, String[] items, Set<String> selected, boolean isAllergen) {
        pane.getChildren().clear();
        for (String item : items) {
            Button btn = new Button(item);
            boolean isSel = selected.contains(item);
            btn.setStyle(isSel
                ? "-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 14; -fx-padding: 6 14; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.8); -fx-background-radius: 12; -fx-font-size: 14; -fx-padding: 6 14; -fx-cursor: hand;"
            );
            btn.setOnAction(e -> {
                String item2 = ((Button) e.getSource()).getText();
                if (selected.contains(item2)) selected.remove(item2);
                else selected.add(item2);
                showAllOptions(pane, items, selected, isAllergen);
            });
            pane.getChildren().add(btn);
        }
    }

    private void refreshSelectedPane(FlowPane pane, Set<String> selected, Label noneLabel) {
        pane.getChildren().clear();
        if (selected.isEmpty()) {
            noneLabel.setVisible(true);
            return;
        }
        noneLabel.setVisible(false);
        for (String item : selected) {
            Button tag = new Button("✕ " + item);
            tag.setStyle("-fx-background-color: #2a6b4c; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 14; -fx-padding: 6 14; -fx-cursor: hand;");
            tag.setOnAction(e -> {
                selected.remove(item);
                refreshSelectedPane(pane, selected, noneLabel);
            });
            pane.getChildren().add(tag);
        }
    }

    // ====== 步骤切换 ======

    private void showStep(int step) {
        step1Panel.setVisible(step == 1); step1Panel.setManaged(step == 1);
        step2Panel.setVisible(step == 2); step2Panel.setManaged(step == 2);
        step3Panel.setVisible(step == 3); step3Panel.setManaged(step == 3);
        step4Panel.setVisible(step == 4); step4Panel.setManaged(step == 4);

        dot1.setStyle("-fx-text-fill: " + (step >= 1 ? "#10b981" : "#4b5563") + "; -fx-font-size: 10;");
        dot2.setStyle("-fx-text-fill: " + (step >= 2 ? "#10b981" : "#4b5563") + "; -fx-font-size: 10;");
        dot3.setStyle("-fx-text-fill: " + (step >= 3 ? "#10b981" : "#4b5563") + "; -fx-font-size: 10;");
        dot4.setStyle("-fx-text-fill: " + (step >= 4 ? "#10b981" : "#4b5563") + "; -fx-font-size: 10;");

        dot1.setText(step == 1 ? "●" : step > 1 ? "◉" : "○");
        dot2.setText(step == 2 ? "●" : step > 2 ? "◉" : "○");
        dot3.setText(step == 3 ? "●" : step > 3 ? "◉" : "○");
        dot4.setText(step == 4 ? "●" : "○");
    }

    @FXML void onStep1Next() {
        refreshSelectedPane(allergenSelectedPane, selectedAllergens, allergenNoneLabel);
        if (selectedAllergens.isEmpty()) stepTitle.setText("第二步：确认过敏原（无）");
        else stepTitle.setText("第二步：确认过敏原");
        stepDesc.setText("确认已选的过敏原，或点击标签移除");
        showStep(2);
    }

    @FXML void onStep2Back() {
        stepTitle.setText("第一步：选择过敏原");
        stepDesc.setText("点击下方标签选择您的过敏原（可多选）");
        showAllOptions(allergenOptionsPane, ALL_ALLERGENS, selectedAllergens, true);
        showStep(1);
    }

    @FXML void onStep2Next() {
        stepTitle.setText("第三步：选择慢性病史");
        stepDesc.setText("点击下方标签选择您的慢性病史（可多选）");
        showAllOptions(diseaseOptionsPane, ALL_DISEASES, selectedDiseases, false);
        showStep(3);
    }

    @FXML void onStep3Next() {
        refreshSelectedPane(diseaseSelectedPane, selectedDiseases, diseaseNoneLabel);
        if (selectedDiseases.isEmpty()) stepTitle.setText("第四步：确认慢性病史（无）");
        else stepTitle.setText("第四步：确认慢性病史");
        stepDesc.setText("确认已选的慢性病史，或点击标签移除");
        showStep(4);
    }

    @FXML void onStep4Back() {
        stepTitle.setText("第三步：选择慢性病史");
        stepDesc.setText("点击下方标签选择您的慢性病史（可多选）");
        showAllOptions(diseaseOptionsPane, ALL_DISEASES, selectedDiseases, false);
        showStep(3);
    }

    @FXML
    void handleSave() {
        int userId = BMIApplication.currentUserId;
        if (userId < 0) return;

        String allergens = String.join(",", selectedAllergens);
        String diseases = String.join(",", selectedDiseases);

        String err = userService.updateHealthProfile(userId, allergens, diseases);
        if (err != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("提示"); alert.setHeaderText(null); alert.setContentText(err);
            alert.showAndWait();
            return;
        }

        navigateToMain();
    }

    @FXML
    void handleSkip() {
        int userId = BMIApplication.currentUserId;
        if (userId > 0) {
            userService.updateHealthProfile(userId, "", "");
        }
        navigateToMain();
    }

    private void navigateToMain() {
        try {
            Stage stage = (Stage) particlePane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene newScene = new Scene(loader.load(), 1200, 800);
            newScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            newScene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

            Parent newRoot = newScene.getRoot();
            newRoot.setOpacity(0);

            stage.setScene(newScene);
            stage.setResizable(true);

            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(newRoot.opacityProperty(), 1))
            );
            fadeIn.play();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误"); alert.setHeaderText(null);
            alert.setContentText("页面加载失败：" + e.getMessage());
            alert.showAndWait();
        }
    }
}
