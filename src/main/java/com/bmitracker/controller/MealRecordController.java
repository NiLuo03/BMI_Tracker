
package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.util.*;
import java.util.stream.Collectors;

public class MealRecordController {

    @FXML private Button mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn;
    @FXML private TextField searchField;
    @FXML private FlowPane foodGrid;
    @FXML private Label foodCountLabel;
    @FXML private ScrollPane selectedScrollPane;
    @FXML private FlowPane selectedFlow;
    @FXML private Label mealCalLabel, totalCalLabel;

    private final FoodService foodService = new FoodServiceImpl();

    private List<Food> allFoods = Collections.emptyList();

    static class FoodEntry {
        Food food; double grams;
        FoodEntry(Food f, double g) { food = f; grams = g; }
        double getCalories() { return food.getCalories() * grams / 100.0; }
    }

    private final List<FoodEntry> breakfastEntries = new ArrayList<>();
    private final List<FoodEntry> lunchEntries = new ArrayList<>();
    private final List<FoodEntry> dinnerEntries = new ArrayList<>();
    private final List<FoodEntry> snackEntries = new ArrayList<>();

    private enum Meal { BREAKFAST, LUNCH, DINNER, SNACK }
    private Meal currentMeal = Meal.BREAKFAST;

    @FXML
    void initialize() {
        new Thread(() -> {
            allFoods = foodService.getAllFoods();
            Platform.runLater(() -> {
                if (allFoods.isEmpty()) {
                    showAlert("食物库为空，请确认数据库已正确初始化");
                }
                updateFoodGrid("");
                searchField.textProperty().addListener((o, ov, nv) -> updateFoodGrid(nv));
            });
        }).start();
    }

    @FXML void selectBreakfast() { setMeal(Meal.BREAKFAST); }
    @FXML void selectLunch() { setMeal(Meal.LUNCH); }
    @FXML void selectDinner() { setMeal(Meal.DINNER); }
    @FXML void selectSnack() { setMeal(Meal.SNACK); }

    private void setMeal(Meal meal) {
        currentMeal = meal;
        Button[] tabs = {mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn};
        for (var b : tabs) b.getStyleClass().setAll("meal-tab");
        getTabBtn(meal).getStyleClass().setAll("meal-tab-active");
        refreshSelected();
        updateFoodGrid(searchField.getText());
    }

    private Button getTabBtn(Meal m) {
        return switch (m) {
            case BREAKFAST -> mealBreakfastBtn;
            case LUNCH -> mealLunchBtn;
            case DINNER -> mealDinnerBtn;
            case SNACK -> mealSnackBtn;
        };
    }

    private List<FoodEntry> getEntries() {
        return switch (currentMeal) {
            case BREAKFAST -> breakfastEntries;
            case LUNCH -> lunchEntries;
            case DINNER -> dinnerEntries;
            case SNACK -> snackEntries;
        };
    }

    private String getMealName() {
        return switch (currentMeal) {
            case BREAKFAST -> "早餐";
            case LUNCH -> "午餐";
            case DINNER -> "晚餐";
            case SNACK -> "加餐";
        };
    }

    private void updateFoodGrid(String keyword) {
        foodGrid.getChildren().clear();
        if (allFoods.isEmpty()) return;

        List<Food> shown;
        if (keyword == null || keyword.isEmpty()) {
            shown = allFoods;
        } else {
            shown = allFoods.stream()
                    .filter(f -> f.getFoodName().contains(keyword))
                    .collect(Collectors.toList());
        }

        Set<Integer> selectedIds = getEntries().stream()
                .map(e -> e.food.getFoodId()).collect(Collectors.toSet());

        for (Food food : shown) {
            boolean isSelected = selectedIds.contains(food.getFoodId());
            Button btn = new Button(food.getFoodName());
            btn.setStyle(isSelected
                ? "-fx-background-color: rgba(16,185,129,0.20); -fx-text-fill: #34d399; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: rgba(16,185,129,0.3); -fx-border-radius: 6;"
                : "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #b0b0b0; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 6;"
            );
            int fid = food.getFoodId();
            btn.setOnAction(e -> {
                List<FoodEntry> entries = getEntries();
                Optional<FoodEntry> existing = entries.stream()
                        .filter(en -> en.food.getFoodId() == fid).findFirst();
                if (existing.isPresent()) {
                    entries.remove(existing.get());
                } else {
                    entries.add(new FoodEntry(food, 100));
                }
                refreshSelected();
                updateFoodGrid(searchField.getText());
            });
            foodGrid.getChildren().add(btn);
        }

        foodCountLabel.setText(String.format("共 %d 种食物", shown.size()));
    }

    private void refreshSelected() {
        List<FoodEntry> entries = getEntries();
        selectedFlow.getChildren().clear();

        if (entries.isEmpty()) {
            Label empty = new Label("暂无选择");
            empty.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
            selectedFlow.getChildren().add(empty);
            mealCalLabel.setText("当前餐小计：0 kcal");
            updateTotal();
            selectedScrollPane.setVvalue(0);
            return;
        }

        double mealCal = 0;
        for (int i = 0; i < entries.size(); i++) {
            FoodEntry entry = entries.get(i);
            mealCal += entry.getCalories();
            final int idx = i;

            HBox card = new HBox(4);
            card.setStyle("-fx-padding: 4 6; -fx-background-color: rgba(16,185,129,0.06); -fx-background-radius: 6;");
            card.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(entry.food.getFoodName());
            name.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 12px; -fx-font-weight: bold;");
            name.setPrefWidth(80);
            name.setMaxWidth(80);

            TextField gramField = new TextField(String.valueOf((int) entry.grams));
            gramField.setPrefWidth(50);
            gramField.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #d0d0d0; -fx-background-radius: 4; -fx-font-size: 11px;");
            gramField.textProperty().addListener((obs, ov, nv) -> {
                try { double g = Double.parseDouble(nv); if (g > 0) { entry.grams = g; refreshSelected(); } }
                catch (NumberFormatException ignored) {}
            });

            Label unit = new Label("g");
            unit.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

            Label cal = new Label(String.format("%.0f", entry.getCalories()));
            cal.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
            cal.setPrefWidth(45);

            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 1 4;");
            removeBtn.setOnAction(e -> { entries.remove(idx); refreshSelected(); updateFoodGrid(searchField.getText()); });

            card.getChildren().addAll(name, gramField, unit, cal, removeBtn);
            selectedFlow.getChildren().add(card);
        }

        mealCalLabel.setText(String.format("%s小计：%.0f kcal", getMealName(), mealCal));
        updateTotal();
        selectedScrollPane.setVvalue(0);
    }

    private void updateTotal() {
        double total = 0;
        for (var list : List.of(breakfastEntries, lunchEntries, dinnerEntries, snackEntries))
            for (FoodEntry e : list) total += e.getCalories();
        totalCalLabel.setText(String.format("%.0f kcal", total));
    }

    @FXML
    void handleAiAdvice() {
        if (breakfastEntries.isEmpty() && lunchEntries.isEmpty() && dinnerEntries.isEmpty() && snackEntries.isEmpty()) {
            showAlert("请先添加食物记录");
            return;
        }

        StringBuilder sb = new StringBuilder("请评价我今天的膳食并提供改进建议：\n\n");
        String[][] mealData = {
            {"早餐", mealToString(breakfastEntries)},
            {"午餐", mealToString(lunchEntries)},
            {"晚餐", mealToString(dinnerEntries)},
            {"加餐", mealToString(snackEntries)}
        };
        for (var md : mealData) {
            if (!md[1].isEmpty()) sb.append("【").append(md[0]).append("】\n").append(md[1]);
        }

        double total = 0;
        for (var list : List.of(breakfastEntries, lunchEntries, dinnerEntries, snackEntries))
            for (FoodEntry e : list) total += e.getCalories();
        sb.append(String.format("\n总热量：%.0f kcal\n\n请给出简洁的营养评价和改进建议，用中文回复。", total));

        AIChatController.getInstance().sendUserMessage(sb.toString());
    }

    private String mealToString(List<FoodEntry> entries) {
        if (entries.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (FoodEntry e : entries)
            sb.append(String.format("- %s %.0fg (%.0f kcal)\n", e.food.getFoodName(), e.grams, e.getCalories()));
        return sb.toString();
    }

    @FXML void handleClearMeal() {
        getEntries().clear();
        refreshSelected();
        updateFoodGrid(searchField.getText());
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
