package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.Food;
import com.bmitracker.model.MealRecord;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import com.bmitracker.service.MealRecordService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MealRecordController {

    @FXML private Label dateLabel, foodCountLabel, mealCalLabel, totalCalLabel, savedLabel;
    @FXML private Button mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn;
    @FXML private TextField searchField;
    @FXML private FlowPane foodGrid, selectedFlow;
    @FXML private ScrollPane selectedScrollPane;
    @FXML private VBox foodSection, weekPanel, weekSummaryList;

    private final FoodService foodService = new FoodServiceImpl();
    private final MealRecordService recordService = new MealRecordService();

    private List<Food> allFoods = Collections.emptyList();

    static class FoodEntry {
        Food food; double grams;
        FoodEntry(Food f, double g) { food = f; grams = g; }
        double getCalories() { return food.getCalories() * grams / 100.0; }
    }

    private final Map<String, List<FoodEntry>> entries = new HashMap<>();

    private enum Meal { BREAKFAST, LUNCH, DINNER, SNACK }
    private Meal currentMeal = Meal.BREAKFAST;

    private LocalDate currentDate = LocalDate.now();
    private boolean loaded = false;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FMT_CN = DateTimeFormatter.ofPattern("M月d日");

    @FXML
    void initialize() {
        entries.put("BREAKFAST", new ArrayList<>());
        entries.put("LUNCH", new ArrayList<>());
        entries.put("DINNER", new ArrayList<>());
        entries.put("SNACK", new ArrayList<>());

        updateDateLabel();
        updateLayoutForDate();

        new Thread(() -> {
            allFoods = foodService.getAllFoods();
            Platform.runLater(() -> {
                if (allFoods.isEmpty()) {
                    showAlert("食物库为空，请确认数据库已正确初始化");
                }
                loadRecordsForDate();
                searchField.textProperty().addListener((o, ov, nv) -> updateFoodGrid(nv));
            });
        }).start();
    }

    @FXML void selectBreakfast() { setMeal(Meal.BREAKFAST); }
    @FXML void selectLunch() { setMeal(Meal.LUNCH); }
    @FXML void selectDinner() { setMeal(Meal.DINNER); }
    @FXML void selectSnack() { setMeal(Meal.SNACK); }

    @FXML void prevDay() {
        currentDate = currentDate.minusDays(1);
        updateDateLabel();
        updateLayoutForDate();
        loadRecordsForDate();
    }

    @FXML void nextDay() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (currentDate.isBefore(LocalDate.now())) {
            currentDate = currentDate.plusDays(1);
            updateDateLabel();
            updateLayoutForDate();
            loadRecordsForDate();
        }
    }

    private void updateLayoutForDate() {
        boolean isToday = currentDate.equals(LocalDate.now());
        foodSection.setVisible(isToday);
        foodSection.setManaged(isToday);
        selectedScrollPane.setPrefHeight(isToday ? 115 : 200);
    }

    @FXML void handleSave() {
        int uid = BMIApplication.currentUserId;
        if (uid < 0) { showAlert("请先登录"); return; }

        savedLabel.setText("保存中...");
        new Thread(() -> {
            try {
                List<MealRecord> all = new ArrayList<>();
                for (Map.Entry<String, List<FoodEntry>> e : entries.entrySet()) {
                    for (FoodEntry fe : e.getValue()) {
                        MealRecord mr = new MealRecord();
                        mr.setUserId(uid);
                        mr.setFoodId(fe.food.getFoodId());
                        mr.setMealType(e.getKey());
                        mr.setGrams(fe.grams);
                        mr.setRecordDate(currentDate);
                        all.add(mr);
                    }
                }
                recordService.saveRecords(uid, currentDate, all);
                Platform.runLater(() -> savedLabel.setText("已保存 " + DATE_FMT.format(currentDate)));
            } catch (Exception ex) {
                Platform.runLater(() -> savedLabel.setText("保存失败"));
            }
        }).start();
    }

    @FXML void showWeekSummary() {
        boolean vis = weekPanel.isVisible();
        weekPanel.setVisible(!vis);
        weekPanel.setManaged(!vis);
        if (!vis) loadWeekSummary();
    }

    private void loadWeekSummary() {
        int uid = BMIApplication.currentUserId;
        if (uid < 0) return;

        new Thread(() -> {
            LocalDate today = LocalDate.now();
            List<MealRecord> records = recordService.getRecordsInRange(uid, today.minusDays(6), today);
            Platform.runLater(() -> {
                weekSummaryList.getChildren().clear();
                if (records.isEmpty()) {
                    weekSummaryList.getChildren().add(new Label("近7天暂无记录"));
                    return;
                }

                Map<LocalDate, List<MealRecord>> byDate = records.stream()
                        .collect(Collectors.groupingBy(MealRecord::getRecordDate));

                for (LocalDate d : byDate.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    List<MealRecord> dayRecs = byDate.get(d);
                    double total = dayRecs.stream().mapToDouble(MealRecord::getCalories).sum();
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label dateLbl = new Label(DATE_FMT_CN.format(d));
                    dateLbl.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 50;");
                    Label calLbl = new Label(String.format("%.0f kcal", total));
                    calLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
                    Label detailLbl = new Label(formatDaySummary(dayRecs));
                    detailLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                    row.getChildren().addAll(dateLbl, calLbl, detailLbl);
                    weekSummaryList.getChildren().add(row);
                }
            });
        }).start();
    }

    private String formatDaySummary(List<MealRecord> records) {
        Map<String, Integer> count = new HashMap<>();
        for (MealRecord r : records) {
            count.merge(r.getMealType(), 1, Integer::sum);
        }
        return count.entrySet().stream()
                .map(e -> e.getKey().charAt(0) + e.getKey().substring(1).toLowerCase() + " " + e.getValue() + "种")
                .collect(Collectors.joining(" · "));
    }

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
        return entries.get(currentMeal.name());
    }

    private String getMealName() {
        return switch (currentMeal) {
            case BREAKFAST -> "早餐";
            case LUNCH -> "午餐";
            case DINNER -> "晚餐";
            case SNACK -> "加餐";
        };
    }

    private void updateDateLabel() {
        dateLabel.setText(DATE_FMT.format(currentDate));
        savedLabel.setText("");
    }

    private void loadRecordsForDate() {
        int uid = BMIApplication.currentUserId;
        if (uid < 0) return;

        for (var list : entries.values()) list.clear();

        new Thread(() -> {
            List<MealRecord> records = recordService.getRecords(uid, currentDate);
            Platform.runLater(() -> {
                if (!records.isEmpty()) {
                    savedLabel.setText("已加载 " + DATE_FMT.format(currentDate));
                }
                for (MealRecord mr : records) {
                    List<FoodEntry> list = entries.get(mr.getMealType());
                    if (list == null) continue;
                    Food food = allFoods.stream()
                            .filter(f -> f.getFoodId() == mr.getFoodId())
                            .findFirst().orElse(null);
                    if (food == null) continue;
                    list.add(new FoodEntry(food, mr.getGrams()));
                }
                refreshSelected();
                updateFoodGrid(searchField.getText());
                loaded = true;
            });
        }).start();
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
                List<FoodEntry> entryList = getEntries();
                Optional<FoodEntry> existing = entryList.stream()
                        .filter(en -> en.food.getFoodId() == fid).findFirst();
                if (existing.isPresent()) {
                    entryList.remove(existing.get());
                } else {
                    entryList.add(new FoodEntry(food, 100));
                }
                refreshSelected();
                updateFoodGrid(searchField.getText());
            });
            foodGrid.getChildren().add(btn);
        }

        foodCountLabel.setText(String.format("共 %d 种食物", shown.size()));
    }

    private void refreshSelected() {
        List<FoodEntry> entryList = getEntries();
        selectedFlow.getChildren().clear();

        if (entryList.isEmpty()) {
            Label empty = new Label("暂无选择");
            empty.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
            selectedFlow.getChildren().add(empty);
            mealCalLabel.setText("当前餐小计：0 kcal");
            updateTotal();
            selectedScrollPane.setVvalue(0);
            return;
        }

        double mealCal = 0;
        for (int i = 0; i < entryList.size(); i++) {
            FoodEntry entry = entryList.get(i);
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
            removeBtn.setOnAction(e -> { entryList.remove(idx); refreshSelected(); updateFoodGrid(searchField.getText()); });

            card.getChildren().addAll(name, gramField, unit, cal, removeBtn);
            selectedFlow.getChildren().add(card);
        }

        mealCalLabel.setText(String.format("%s小计：%.0f kcal", getMealName(), mealCal));
        updateTotal();
        selectedScrollPane.setVvalue(0);
    }

    private void updateTotal() {
        double total = 0;
        for (var list : entries.values())
            for (FoodEntry e : list) total += e.getCalories();
        totalCalLabel.setText(String.format("%.0f kcal", total));
    }

    @FXML
    void handleAiAdvice() {
        boolean empty = true;
        for (var list : entries.values()) if (!list.isEmpty()) { empty = false; break; }
        if (empty) { showAlert("请先添加食物记录"); return; }

        StringBuilder sb = new StringBuilder("请评价我今天的膳食并提供改进建议：\n\n");
        String[][] mealData = {
            {"早餐", mealToString(entries.get("BREAKFAST"))},
            {"午餐", mealToString(entries.get("LUNCH"))},
            {"晚餐", mealToString(entries.get("DINNER"))},
            {"加餐", mealToString(entries.get("SNACK"))}
        };
        for (var md : mealData) {
            if (!md[1].isEmpty()) sb.append("【").append(md[0]).append("】\n").append(md[1]);
        }

        double total = 0;
        for (var list : entries.values())
            for (FoodEntry e : list) total += e.getCalories();
        sb.append(String.format("\n总热量：%.0f kcal\n\n请给出简洁的营养评价和改进建议，用中文回复。", total));

        AIChatController.getInstance().sendUserMessage(sb.toString());
    }

    private String mealToString(List<FoodEntry> list) {
        if (list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (FoodEntry e : list)
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
