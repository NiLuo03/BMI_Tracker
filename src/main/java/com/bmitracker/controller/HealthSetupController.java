package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.UserService;
import com.bmitracker.util.ParticleTextCanvas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
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

    private final UserService userService = new UserService();
    private final Set<String> selectedAllergens = new HashSet<>();
    private final Set<String> selectedDiseases = new HashSet<>();

    @FXML
    void initialize() {
        ParticleTextCanvas canvas = new ParticleTextCanvas(1200, 800,
                new String[]{"HEALTH", "PROFILE", "ALLERGEN", "DISEASE", "RECORD"});
        particlePane.getChildren().clear();
        particlePane.getChildren().add(canvas);

        allergenSearchField.setOnAction(e -> searchAllergens());
        diseaseSearchField.setOnAction(e -> searchDiseases());

        showAllOptions(allergenOptionsPane, ALL_ALLERGENS, selectedAllergens, true);
        showAllOptions(diseaseOptionsPane, ALL_DISEASES, selectedDiseases, false);
    }

    @FXML
    void searchAllergens() {
        String kw = allergenSearchField.getText().trim().toLowerCase();
        String[] filtered = kw.isEmpty() ? ALL_ALLERGENS :
            Arrays.stream(ALL_ALLERGENS).filter(s -> s.contains(kw) || s.toLowerCase().contains(kw)).toArray(String[]::new);
        showAllOptions(allergenOptionsPane, filtered, selectedAllergens, true);
    }

    @FXML
    void searchDiseases() {
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
                ? "-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 12; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.8); -fx-background-radius: 12; -fx-font-size: 12; -fx-cursor: hand;"
            );
            btn.setOnAction(e -> toggleItem(item, isAllergen));
            pane.getChildren().add(btn);
        }
    }

    private void toggleItem(String item, boolean isAllergen) {
        Set<String> selected = isAllergen ? selectedAllergens : selectedDiseases;
        if (selected.contains(item)) {
            selected.remove(item);
        } else {
            selected.add(item);
        }
        refreshPanes();
    }

    private void refreshPanes() {
        searchAllergens();
        searchDiseases();
        refreshSelectedPane(allergenSelectedPane, selectedAllergens, true);
        refreshSelectedPane(diseaseSelectedPane, selectedDiseases, false);
    }

    private void refreshSelectedPane(FlowPane pane, Set<String> selected, boolean isAllergen) {
        pane.getChildren().clear();
        for (String item : selected) {
            Button tag = new Button("✕ " + item);
            tag.setStyle("-fx-background-color: #2a6b4c; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 11; -fx-cursor: hand;");
            tag.setOnAction(e -> {
                (isAllergen ? selectedAllergens : selectedDiseases).remove(item);
                refreshPanes();
            });
            pane.getChildren().add(tag);
        }
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
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText(err);
            alert.showAndWait();
            return;
        }

        navigateToMain();
    }

    @FXML
    void handleSkip() {
        navigateToMain();
    }

    private void navigateToMain() {
        try {
            Stage stage = (Stage) particlePane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene newScene = new Scene(loader.load(), 1200, 800);
            newScene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            newScene.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, javafx.scene.paint.Color.web("#050f0a")),
                new Stop(1, javafx.scene.paint.Color.web("#000000"))));

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
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("页面加载失败：" + e.getMessage());
            alert.showAndWait();
        }
    }
}
