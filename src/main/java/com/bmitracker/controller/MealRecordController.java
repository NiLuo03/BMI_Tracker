package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.Food;
import com.bmitracker.model.User;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import com.bmitracker.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class MealRecordController {

    private static final String API_KEY = "ark-bbc33ed4-cfb8-403d-bfa1-c180e8d9e02f-606ca";
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String MODEL = "ep-20260714154339-vkt22";

    @FXML private Button mealBreakfastBtn, mealLunchBtn, mealDinnerBtn, mealSnackBtn;
    @FXML private TextField searchField;
    @FXML private FlowPane foodGrid;
    @FXML private Label foodCountLabel;
    @FXML private ScrollPane selectedScrollPane;
    @FXML private FlowPane selectedFlow;
    @FXML private Label mealCalLabel, totalCalLabel;

    private final FoodService foodService = new FoodServiceImpl();
    private final UserService userService = new UserService();
    private final HttpClient httpClient = HttpClient.newHttpClient();

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

        StringBuilder sb = new StringBuilder("用户今天吃了以下食物，请给出营养评价和健康建议：\n\n");
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
        sb.append(String.format("\n总热量：%.0f kcal", total));

        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user != null) {
            sb.append("\n\n用户信息：");
            sb.append(String.format("年龄%d岁，性别%s", user.getUserAge(), user.getSex() == 1 ? "女" : "男"));
            if (user.getHeight() > 0) sb.append(String.format("，身高%.1fcm", user.getHeight()));
            if (user.getWeight() > 0) sb.append(String.format("，体重%.1fkg", user.getWeight()));
            if (user.getAllergens() != null && !user.getAllergens().isEmpty())
                sb.append("，过敏原：").append(user.getAllergens());
            if (user.getChronicDiseases() != null && !user.getChronicDiseases().isEmpty())
                sb.append("，慢性病史：").append(user.getChronicDiseases());
        }
        sb.append("\n\n请给出简洁的营养评价和改进建议（200字以内），用中文回复。");

        String prompt = sb.toString();
        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("AI 分析中");
        loading.setHeaderText(null);
        loading.setContentText("正在分析您的膳食记录，请稍候...");
        loading.show();

        new Thread(() -> {
            try {
                String response = callAi(prompt);
                Platform.runLater(() -> {
                    loading.close();
                    Alert result = new Alert(Alert.AlertType.INFORMATION);
                    result.setTitle("AI 膳食建议");
                    result.setHeaderText("营养评价");
                    TextArea area = new TextArea(response);
                    area.setWrapText(true); area.setEditable(false);
                    area.setPrefSize(400, 250);
                    area.setStyle("-fx-background-color: rgba(16,185,129,0.04); -fx-text-fill: #d0d0d0; -fx-control-inner-background: #0a0a1a;");
                    result.getDialogPane().setContent(area);
                    result.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> { loading.close(); showAlert("AI 服务繁忙，请稍后再试"); });
            }
        }).start();
    }

    private String mealToString(List<FoodEntry> entries) {
        if (entries.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (FoodEntry e : entries)
            sb.append(String.format("- %s %.0fg (%.0f kcal)\n", e.food.getFoodName(), e.grams, e.getCalories()));
        return sb.toString();
    }

    private String callAi(String userMessage) throws Exception {
        String json = "{\"model\":\"" + MODEL + "\",\"messages\":["
                + "{\"role\":\"system\",\"content\":\"你是一位专业的营养师，根据用户膳食记录给出评价和建议。用中文回答，简洁明了，200字以内。\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}"
                + "]}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL)).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) return extractMessage(response.body());
        throw new RuntimeException("API error: " + response.statusCode());
    }

    private String extractMessage(String body) {
        String key = "\"message\":{\"content\":\"";
        int start = body.indexOf(key);
        if (start < 0) return body;
        start += key.length();
        StringBuilder r = new StringBuilder();
        for (int i = start; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length()) {
                char n = body.charAt(i + 1);
                if (n == '"') { r.append('"'); i++; }
                else if (n == 'n') { r.append('\n'); i++; }
                else if (n == 'r') { r.append('\r'); i++; }
                else if (n == 't') { r.append('\t'); i++; }
                else if (n == '\\') { r.append('\\'); i++; }
                else r.append(c);
            } else if (c == '"') break;
            else r.append(c);
        }
        return r.toString();
    }

    private String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\""); case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r"); case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
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
