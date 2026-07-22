package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.util.*;

public class HealthEditController {

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

    @FXML private TextField allergenSearchField, diseaseSearchField;
    @FXML private FlowPane allergenOptionsPane, diseaseOptionsPane;

    private final UserService userService = new UserService();
    private final Set<String> selectedAllergens = new HashSet<>();
    private final Set<String> selectedDiseases = new HashSet<>();

    @FXML
    void initialize() {
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user != null) {
            if (user.getAllergens() != null && !user.getAllergens().trim().isEmpty()) {
                String[] parts = user.getAllergens().split(",");
                for (String a : parts) {
                    String t = a.trim();
                    if (!t.isEmpty()) selectedAllergens.add(t);
                }
            }
            if (user.getChronicDiseases() != null && !user.getChronicDiseases().trim().isEmpty()) {
                String[] parts = user.getChronicDiseases().split(",");
                for (String d : parts) {
                    String t = d.trim();
                    if (!t.isEmpty()) selectedDiseases.add(t);
                }
            }
        }

        showAllOptions(allergenOptionsPane, ALL_ALLERGENS, selectedAllergens);
        showAllOptions(diseaseOptionsPane, ALL_DISEASES, selectedDiseases);

        allergenSearchField.setOnAction(e -> searchAllergens());
        diseaseSearchField.setOnAction(e -> searchDiseases());
    }

    @FXML void searchAllergens() {
        String kw = allergenSearchField.getText().trim().toLowerCase();
        String[] filtered = kw.isEmpty() ? ALL_ALLERGENS :
            Arrays.stream(ALL_ALLERGENS).filter(s -> s.contains(kw) || s.toLowerCase().contains(kw)).toArray(String[]::new);
        showAllOptions(allergenOptionsPane, filtered, selectedAllergens);
    }

    @FXML void searchDiseases() {
        String kw = diseaseSearchField.getText().trim().toLowerCase();
        String[] filtered = kw.isEmpty() ? ALL_DISEASES :
            Arrays.stream(ALL_DISEASES).filter(s -> s.contains(kw) || s.toLowerCase().contains(kw)).toArray(String[]::new);
        showAllOptions(diseaseOptionsPane, filtered, selectedDiseases);
    }

    private void showAllOptions(FlowPane pane, String[] items, Set<String> selected) {
        pane.getChildren().clear();
        for (String item : items) {
            Button btn = new Button(item);
            boolean isSel = selected.contains(item);
            btn.setStyle(isSel
                ? "-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 14; -fx-padding: 6 14; -fx-cursor: hand;"
                : "-fx-background-color: -card-bg; -fx-text-fill: -text-primary; -fx-background-radius: 12; -fx-font-size: 14; -fx-padding: 6 14; -fx-cursor: hand; -fx-border-color: -card-border; -fx-border-width: 1px; -fx-border-radius: 12;"
            );
            btn.setOnAction(e -> {
                String item2 = ((Button) e.getSource()).getText();
                if (selected.contains(item2)) selected.remove(item2);
                else selected.add(item2);
                showAllOptions(pane, items, selected);
            });
            pane.getChildren().add(btn);
        }
    }

    @FXML
    void handleSave() {
        String allergens = String.join(",", selectedAllergens);
        String diseases = String.join(",", selectedDiseases);
        String err = userService.updateHealthProfile(BMIApplication.currentUserId, allergens, diseases);
        if (err != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("提示"); alert.setHeaderText(null); alert.setContentText(err);
            alert.showAndWait();
            return;
        }
        goBackToBmiRecord();
    }

    @FXML
    void handleBack() {
        goBackToBmiRecord();
    }

    private void goBackToBmiRecord() {
        MainController main = MainController.getInstance();
        if (main != null) main.showBmiRecord(null);
    }
}
