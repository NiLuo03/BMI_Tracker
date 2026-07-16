package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class FoodRankController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button step1Next, step2Next;
    @FXML private VBox step1Panel, step2Panel, step3Panel;
    @FXML private VBox nutrientGroup, rankList;
    @FXML private Label step1Indicator, step2Indicator, step3Indicator;
    @FXML private Label rankTitle;

    private final FoodService foodService = new FoodServiceImpl();
    private ToggleGroup nutrientToggle;
    private String selectedCategory;
    private String selectedNutrient;
    private boolean selectedAsc;

    @FXML
    void initialize() {
        List<String> categories = foodService.getAllCategories();
        if (categories != null) categoryCombo.getItems().addAll(categories);

        categoryCombo.setOnAction(e -> {
            selectedCategory = categoryCombo.getValue();
            step1Next.setDisable(selectedCategory == null);
        });

        nutrientToggle = new ToggleGroup();
        for (var node : nutrientGroup.getChildren()) {
            if (node instanceof RadioButton rb) {
                rb.setToggleGroup(nutrientToggle);
            }
        }
        nutrientToggle.selectedToggleProperty().addListener((obs, old, neo) -> {
            step2Next.setDisable(neo == null);
            if (neo != null) {
                String ud = (String) neo.getUserData();
                String[] parts = ud.split(":");
                selectedNutrient = parts[0];
                selectedAsc = Boolean.parseBoolean(parts[1]);
            }
        });
    }

    @FXML
    void onStep1Next() {
        step1Panel.setVisible(false);
        step1Panel.setManaged(false);
        step2Panel.setVisible(true);
        step2Panel.setManaged(true);
        step1Indicator.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13;");
        step2Indicator.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13; -fx-font-weight: bold;");
    }

    @FXML
    void onStep2Back() {
        step2Panel.setVisible(false);
        step2Panel.setManaged(false);
        step1Panel.setVisible(true);
        step1Panel.setManaged(true);
        step2Indicator.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13;");
    }

    @FXML
    void onStep2Next() {
        if (selectedCategory == null || selectedNutrient == null) return;

        List<Food> list = foodService.getRankByCategoryAndNutrient(selectedCategory, selectedNutrient, selectedAsc);
        if (list == null || list.isEmpty()) return;
        if (list.size() > 10) list = list.subList(0, 10);

        buildRankList(list);

        step2Panel.setVisible(false);
        step2Panel.setManaged(false);
        step3Panel.setVisible(true);
        step3Panel.setManaged(true);
        step2Indicator.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13;");
        step3Indicator.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13; -fx-font-weight: bold;");

        String dirLabel = selectedAsc ? "从低到高" : "从高到低";
        rankTitle.setText("「" + selectedCategory + "」" + nutrientLabel(selectedNutrient) + "排行 " + dirLabel);
    }

    @FXML
    void onStep3Back() {
        step3Panel.setVisible(false);
        step3Panel.setManaged(false);
        step1Panel.setVisible(true);
        step1Panel.setManaged(true);
        step2Indicator.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13;");
        step3Indicator.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13;");
    }

    private void buildRankList(List<Food> list) {
        rankList.getChildren().clear();

        for (int i = 0; i < list.size(); i++) {
            Food f = list.get(i);
            int rank = i + 1;

            boolean isTop3 = rank <= 3;
            int cardHeight = isTop3 ? 140 : 64;
            int imgSize = isTop3 ? 100 : 52;

            HBox card = new HBox(16);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(isTop3 ? 18 : 6, 20, isTop3 ? 18 : 6, 20));
            card.setPrefHeight(cardHeight);
            card.setMinHeight(cardHeight);
            card.setMaxHeight(cardHeight);

            if (rank == 1) {
                card.setStyle("-fx-background-color: linear-gradient(to right, rgba(255,215,0,0.15), rgba(255,215,0,0.05)); -fx-background-radius: 12; -fx-border-color: rgba(255,215,0,0.30); -fx-border-width: 1; -fx-border-radius: 12;");
            } else if (rank == 2) {
                card.setStyle("-fx-background-color: linear-gradient(to right, rgba(192,192,192,0.12), rgba(192,192,192,0.04)); -fx-background-radius: 12; -fx-border-color: rgba(192,192,192,0.25); -fx-border-width: 1; -fx-border-radius: 12;");
            } else if (rank == 3) {
                card.setStyle("-fx-background-color: linear-gradient(to right, rgba(205,127,50,0.12), rgba(205,127,50,0.04)); -fx-background-radius: 12; -fx-border-color: rgba(205,127,50,0.25); -fx-border-width: 1; -fx-border-radius: 12;");
            } else {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1; -fx-border-radius: 10;");
            }

            Label rankLabel = new Label(isTop3 ? medal(rank) : "#" + rank);
            rankLabel.setFont(Font.font("System", isTop3 ? FontWeight.BOLD : FontWeight.NORMAL, isTop3 ? 26 : 14));
            rankLabel.setMinWidth(44);
            rankLabel.setAlignment(Pos.CENTER);
            if (rank == 1) rankLabel.setStyle("-fx-text-fill: #ffd700;");
            else if (rank == 2) rankLabel.setStyle("-fx-text-fill: #c0c0c0;");
            else if (rank == 3) rankLabel.setStyle("-fx-text-fill: #cd7f32;");
            else rankLabel.setStyle("-fx-text-fill: #6b7280;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(imgSize);
            imgView.setFitHeight(imgSize);
            imgView.setPreserveRatio(true);
            try {
                String imgUrl = getClass().getResource("/images/foods/" + f.getImage()).toExternalForm();
                imgView.setImage(new Image(imgUrl, imgSize, imgSize, true, true, true));
            } catch (Exception ignored) {}

            VBox info = new VBox(isTop3 ? 6 : 2);
            info.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(f.getFoodName());
            nameLabel.setFont(Font.font("System", isTop3 ? FontWeight.BOLD : FontWeight.NORMAL, isTop3 ? 18 : 14));
            nameLabel.setStyle("-fx-text-fill: " + (isTop3 ? "#d0d0d0" : "#9ca3af") + ";");

            String valStr = formatNutrient(selectedNutrient, f);
            Label valLabel = new Label(valStr);
            valLabel.setFont(Font.font("System", isTop3 ? 15 : 12));
            valLabel.setStyle("-fx-text-fill: #6b7280;");

            info.getChildren().addAll(nameLabel, valLabel);

            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            Label valueLabel = new Label(nutrientShortValue(selectedNutrient, f));
            valueLabel.setFont(Font.font("System", FontWeight.BOLD, isTop3 ? 24 : 16));
            valueLabel.setStyle("-fx-text-fill: " + (isTop3 ? "#34d399" : "#6b7280") + ";");

            card.getChildren().addAll(rankLabel, imgView, info, valueLabel);
            rankList.getChildren().add(card);
        }
    }

    private String medal(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "";
        };
    }

    private String nutrientLabel(String nutrient) {
        return switch (nutrient) {
            case "calories" -> "热量";
            case "protein" -> "蛋白质";
            case "fat" -> "脂肪";
            case "carb" -> "碳水化合物";
            default -> nutrient;
        };
    }

    private String formatNutrient(String nutrient, Food f) {
        return switch (nutrient) {
            case "calories" -> String.format("每100g  %.0f 大卡", f.getCalories());
            case "protein" -> String.format("每100g  %.1f g", f.getProtein());
            case "fat" -> String.format("每100g  %.1f g", f.getFat());
            case "carb" -> String.format("每100g  %.1f g", f.getCarb());
            default -> "";
        };
    }

    private String nutrientShortValue(String nutrient, Food f) {
        return switch (nutrient) {
            case "calories" -> String.format("%.0f", f.getCalories());
            case "protein" -> String.format("%.1f", f.getProtein());
            case "fat" -> String.format("%.1f", f.getFat());
            case "carb" -> String.format("%.1f", f.getCarb());
            default -> "";
        };
    }
}
