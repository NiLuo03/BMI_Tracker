package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class FoodRankController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button submitBtn;
    @FXML private VBox selectPanel, rankPanel;
    @FXML private VBox nutrientGroup, rankList;
    @FXML private Label rankTitle;

    private final FoodService foodService = new FoodServiceImpl();
    private ToggleGroup nutrientToggle;
    private String selectedCategory;
    private String selectedNutrient;
    private boolean selectedAsc;

    @FXML
    void initialize() {
        nutrientToggle = new ToggleGroup();
        for (var node : nutrientGroup.getChildren()) {
            if (node instanceof RadioButton rb) {
                rb.setToggleGroup(nutrientToggle);
            }
        }

        Runnable checkReady = () -> {
            boolean ready = selectedCategory != null && nutrientToggle.getSelectedToggle() != null;
            submitBtn.setDisable(!ready);
        };

        nutrientToggle.selectedToggleProperty().addListener((obs, old, neo) -> {
            if (neo != null) {
                String ud = (String) neo.getUserData();
                String[] parts = ud.split(":");
                selectedNutrient = parts[0];
                selectedAsc = Boolean.parseBoolean(parts[1]);
            }
            checkReady.run();
        });

        new Thread(() -> {
            List<String> categories = foodService.getAllCategories();
            Platform.runLater(() -> {
                if (categories != null) categoryCombo.getItems().addAll(categories);
                categoryCombo.setOnAction(e -> {
                    selectedCategory = categoryCombo.getValue();
                    checkReady.run();
                });
            });
        }).start();
    }

    @FXML
    void onSubmit() {
        if (selectedCategory == null || selectedNutrient == null) return;

        List<Food> list = foodService.getRankByCategoryAndNutrient(selectedCategory, selectedNutrient, selectedAsc);
        if (list == null || list.isEmpty()) return;
        if (list.size() > 10) list = list.subList(0, 10);

        buildRankList(list);

        selectPanel.setVisible(false);
        selectPanel.setManaged(false);
        rankPanel.setVisible(true);
        rankPanel.setManaged(true);

        String dirLabel = selectedAsc ? "从低到高" : "从高到低";
        rankTitle.setText("「" + selectedCategory + "」" + nutrientLabel(selectedNutrient) + "排行 " + dirLabel);
    }

    @FXML
    void onReset() {
        rankPanel.setVisible(false);
        rankPanel.setManaged(false);
        selectPanel.setVisible(true);
        selectPanel.setManaged(true);
    }

    private void buildRankList(List<Food> list) {
        rankList.getChildren().clear();

        for (int i = 0; i < list.size(); i++) {
            Food f = list.get(i);
            int rank = i + 1;

            boolean isTop3 = rank <= 3;
            int cardHeight = isTop3 ? 140 : 64;
            int imgSize = isTop3 ? 100 : 52;

            HBox inner = new HBox(16);
            inner.setAlignment(Pos.CENTER_LEFT);
            inner.setPadding(new Insets(isTop3 ? 18 : 6, 20, isTop3 ? 18 : 6, 20));
            inner.setPrefHeight(cardHeight);
            inner.setMinHeight(cardHeight);
            inner.setMaxHeight(cardHeight);
            inner.getStyleClass().add("gradient-card-inner");

            StackPane outer = new StackPane(inner);
            outer.getStyleClass().add("gradient-card");
            outer.setMaxHeight(cardHeight + 4);

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

            inner.getChildren().addAll(rankLabel, imgView, info, valueLabel);
            rankList.getChildren().add(outer);
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
