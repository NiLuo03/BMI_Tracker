package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FoodCompareController implements Initializable {

    @FXML private ComboBox<Food> foodCombo1;
    @FXML private ComboBox<Food> foodCombo2;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox compareContent;
    @FXML private VBox foodEmptyState;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterMealType;
    @FXML private ComboBox<String> filterTexture;
    @FXML private ComboBox<String> filterFlavor;
    @FXML private ComboBox<String> filterStorage;
    @FXML private ComboBox<String> filterCooking;

    private final FoodService foodService = new FoodServiceImpl();
    private final ObservableList<Food> allFoods = FXCollections.observableArrayList();
    private Food f1, f2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        foodCombo1.setPromptText("选择食物 1");
        foodCombo2.setPromptText("选择食物 2");
        foodCombo1.setOnAction(e -> { f1 = foodCombo1.getValue(); buildComparison(); });
        foodCombo2.setOnAction(e -> { f2 = foodCombo2.getValue(); buildComparison(); });
        filterCategory.setOnAction(e -> applyFilters());
        filterMealType.setOnAction(e -> applyFilters());
        filterTexture.setOnAction(e -> applyFilters());
        filterFlavor.setOnAction(e -> applyFilters());
        filterStorage.setOnAction(e -> applyFilters());
        filterCooking.setOnAction(e -> applyFilters());

        new Thread(this::loadFoods).start();
    }

    private void loadFoods() {
        try {
            List<Food> list = foodService.getAllFoods();
            if (list == null || list.isEmpty()) {
                Platform.runLater(() -> {
                    foodEmptyState.setVisible(true);
                    foodEmptyState.setManaged(true);
                    compareContent.getChildren().clear();
                });
                return;
            }
            List<String> categories = foodService.getAllCategories();
            List<String> mealTypes = foodService.getAllMealTypes();
            List<String> textures = foodService.getAllTextures();
            List<String> flavors = foodService.getAllFlavors();
            List<String> storages = foodService.getAllStorages();
            List<String> cookings = foodService.getAllCookingMethods();
            Platform.runLater(() -> {
                allFoods.setAll(list);
                applyFilters();
                filterCategory.setItems(FXCollections.observableArrayList(categories));
                filterMealType.setItems(FXCollections.observableArrayList(mealTypes));
                filterTexture.setItems(FXCollections.observableArrayList(textures));
                filterFlavor.setItems(FXCollections.observableArrayList(flavors));
                filterStorage.setItems(FXCollections.observableArrayList(storages));
                filterCooking.setItems(FXCollections.observableArrayList(cookings));
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                foodEmptyState.setVisible(true);
                foodEmptyState.setManaged(true);
                compareContent.getChildren().clear();
            });
        }
    }

    @FXML
    private void resetFilters() {
        filterCategory.setValue(null); filterMealType.setValue(null); filterTexture.setValue(null);
        filterFlavor.setValue(null); filterStorage.setValue(null); filterCooking.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        String cat = filterCategory.getValue(), meal = filterMealType.getValue(), tex = filterTexture.getValue();
        String fla = filterFlavor.getValue(), sto = filterStorage.getValue(), coo = filterCooking.getValue();
        List<Food> filtered = allFoods.stream()
                .filter(f -> cat == null || cat.equals(f.getCategory()))
                .filter(f -> meal == null || meal.equals(f.getMealType()))
                .filter(f -> tex == null || tex.equals(f.getFoodTexture()))
                .filter(f -> fla == null || fla.equals(f.getFlavor()))
                .filter(f -> sto == null || sto.equals(f.getStorage()))
                .filter(f -> coo == null || coo.equals(f.getCookingMethod())).toList();
        ObservableList<Food> items = FXCollections.observableArrayList(filtered);
        foodCombo1.setItems(items);
        foodCombo2.setItems(items);
        if (f1 != null && !filtered.contains(f1)) { f1 = null; foodCombo1.setValue(null); }
        if (f2 != null && !filtered.contains(f2)) { f2 = null; foodCombo2.setValue(null); }
        buildComparison();
    }

    private void buildComparison() {
        compareContent.getChildren().clear();
        if (f1 == null && f2 == null) {
            foodEmptyState.setVisible(true);
            foodEmptyState.setManaged(true);
            return;
        }
        foodEmptyState.setVisible(false);
        foodEmptyState.setManaged(false);

        boolean light = isLightTheme();

        HBox cardRow = new HBox(50);
        cardRow.setAlignment(Pos.CENTER);
        cardRow.setPadding(new Insets(10, 0, 20, 0));
        cardRow.getChildren().addAll(foodCard(f1, light), foodCard(f2, light));
        compareContent.getChildren().add(cardRow);

        Separator sep = new Separator();
        sep.setStyle(light
                ? "-fx-background-color: rgba(0,0,0,0.10);"
                : "-fx-background-color: rgba(16,185,129,0.10);");
        compareContent.getChildren().add(sep);

        buildSpecRow("热量", f1, f2, light, f -> String.format("%.0f", f.getCalories()), "大卡");
        buildSpecRow("蛋白质", f1, f2, light, f -> String.format("%.1f", f.getProtein()), "g");
        buildSpecRow("脂肪", f1, f2, light, f -> String.format("%.1f", f.getFat()), "g");
        buildSpecRow("碳水化合物", f1, f2, light, f -> String.format("%.1f", f.getCarb()), "g");
        buildSpecRow("餐食类型", f1, f2, light, f -> nullToDash(f.getMealType()), null);
        buildSpecRow("口感", f1, f2, light, f -> nullToDash(f.getFoodTexture()), null);
        buildSpecRow("口味", f1, f2, light, f -> nullToDash(f.getFlavor()), null);
        buildSpecRow("储存方式", f1, f2, light, f -> nullToDash(f.getStorage()), null);
        buildSpecRow("烹饪方式", f1, f2, light, f -> nullToDash(f.getCookingMethod()), null);
        compareContent.getChildren().add(new Region() {{ setMinHeight(40); }});
    }

    private StackPane foodCard(Food f, boolean light) {
        VBox inner = new VBox(8);
        inner.setAlignment(Pos.TOP_CENTER);
        inner.setPrefWidth(260);
        inner.getStyleClass().add("gradient-card-inner");
        inner.setStyle("-fx-padding: 20;");

        if (f == null) {
            Label placeholder = new Label("未选择");
            placeholder.setStyle(light
                    ? "-fx-text-fill: #333333; -fx-font-size: 14;"
                    : "-fx-text-fill: #6b7280; -fx-font-size: 14;");
            inner.getChildren().add(placeholder);
            StackPane outer = new StackPane(inner);
            outer.getStyleClass().add("gradient-card");
            return outer;
        }
        Label nameLabel = new Label(f.getFoodName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setStyle(light
                ? "-fx-text-fill: #111111;"
                : "-fx-text-fill: #d0d0d0;");
        inner.getChildren().add(nameLabel);

        // 食物图片
        try {
            String imgPath = f.getImage();
            if (imgPath != null && !imgPath.isEmpty()) {
                ImageView imgView = new ImageView();
                imgView.setFitWidth(280);
                imgView.setFitHeight(280);
                imgView.setPreserveRatio(true);
                Image img = new Image(getClass().getResource("/images/foods/" + imgPath).toExternalForm(), 280, 280, true, true, true);
                imgView.setImage(img);
                inner.getChildren().add(imgView);
            }
        } catch (Exception ignored) {}

        Label catLabel = new Label(f.getCategory());
        catLabel.setStyle(light
                ? "-fx-background-color: rgba(16,185,129,0.10); -fx-text-fill: #0d9488; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 12;"
                : "-fx-background-color: rgba(16,185,129,0.12); -fx-text-fill: #10b981; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 12;");
        inner.getChildren().add(catLabel);

        StackPane outer = new StackPane(inner);
        outer.getStyleClass().add("gradient-card");
        return outer;
    }

    private void buildSpecRow(String label, Food f1, Food f2, boolean light,
                              java.util.function.Function<Food, String> extractor, String unit) {
        HBox row = new HBox(135);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(18, 40, 18, 40));

        String valColor = light ? "#111111" : "#d0d0d0";
        String nameColor = light ? "#555555" : "#6b7280";

        VBox leftCol = new VBox(2);
        leftCol.setAlignment(Pos.CENTER);
        leftCol.setPrefWidth(240);

        String leftText = f1 != null ? extractor.apply(f1) : "-";
        Label leftVal = new Label(unit != null ? leftText + " " + unit : leftText);
        leftVal.setFont(Font.font("System", FontWeight.BOLD, 22));
        leftVal.setStyle("-fx-text-fill: " + valColor + ";");

        Label leftName = new Label(label);
        leftName.setStyle("-fx-text-fill: " + nameColor + "; -fx-font-size: 12;");

        leftCol.getChildren().addAll(leftVal, leftName);

        VBox rightCol = new VBox(2);
        rightCol.setAlignment(Pos.CENTER);
        rightCol.setPrefWidth(240);

        String rightText = f2 != null ? extractor.apply(f2) : "-";
        Label rightVal = new Label(unit != null ? rightText + " " + unit : rightText);
        rightVal.setFont(Font.font("System", FontWeight.BOLD, 22));
        rightVal.setStyle("-fx-text-fill: " + valColor + ";");

        Label rightName = new Label(label);
        rightName.setStyle("-fx-text-fill: " + nameColor + "; -fx-font-size: 12;");

        rightCol.getChildren().addAll(rightVal, rightName);

        row.getChildren().addAll(leftCol, rightCol);
        compareContent.getChildren().add(row);
    }

    private boolean isLightTheme() {
        if (foodCombo1.getScene() != null && foodCombo1.getScene().getRoot() instanceof Parent) {
            return ((Parent) foodCombo1.getScene().getRoot()).getStyleClass().contains("light-theme");
        }
        return false;
    }

    private static String nullToDash(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}
