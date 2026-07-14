package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FoodCompareController implements Initializable {

    @FXML private ComboBox<Food> foodCombo1;
    @FXML private ComboBox<Food> foodCombo2;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox compareContent;

    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterMealType;
    @FXML private ComboBox<String> filterTexture;
    @FXML private ComboBox<String> filterFlavor;
    @FXML private ComboBox<String> filterStorage;
    @FXML private ComboBox<String> filterCooking;

    private final FoodService foodService = new FoodServiceImpl();
    private final ObservableList<Food> allFoods = FXCollections.observableArrayList();
    private Food f1, f2;
    private static final Logger LOG = Logger.getLogger(FoodCompareController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        foodCombo1.setOnAction(e -> { f1 = foodCombo1.getValue(); buildComparison(); });
        foodCombo2.setOnAction(e -> { f2 = foodCombo2.getValue(); buildComparison(); });

        loadFoods();
    }

    private void loadFoods() {
        try {
            List<Food> list = foodService.getAllFoods();
            LOG.info("加载到 " + (list == null ? 0 : list.size()) + " 种食物");
            if (list == null || list.isEmpty()) {
                showAlert("数据为空", "食物数据库中没有数据，请检查数据库初始化");
                return;
            }
            allFoods.setAll(list);
            applyFilters();

            loadFilterOptions();
            filterCategory.setOnAction(e -> applyFilters());
            filterMealType.setOnAction(e -> applyFilters());
            filterTexture.setOnAction(e -> applyFilters());
            filterFlavor.setOnAction(e -> applyFilters());
            filterStorage.setOnAction(e -> applyFilters());
            filterCooking.setOnAction(e -> applyFilters());
        } catch (Exception e) {
            showAlert("加载失败", "无法加载食物数据：" + e.getMessage());
        }
    }

    private void loadFilterOptions() {
        filterCategory.setItems(FXCollections.observableArrayList(foodService.getAllCategories()));
        filterMealType.setItems(FXCollections.observableArrayList(foodService.getAllMealTypes()));
        filterTexture.setItems(FXCollections.observableArrayList(foodService.getAllTextures()));
        filterFlavor.setItems(FXCollections.observableArrayList(foodService.getAllFlavors()));
        filterStorage.setItems(FXCollections.observableArrayList(foodService.getAllStorages()));
        filterCooking.setItems(FXCollections.observableArrayList(foodService.getAllCookingMethods()));
    }

    @FXML
    private void resetFilters() {
        filterCategory.setValue(null);
        filterMealType.setValue(null);
        filterTexture.setValue(null);
        filterFlavor.setValue(null);
        filterStorage.setValue(null);
        filterCooking.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        String cat = filterCategory.getValue();
        String meal = filterMealType.getValue();
        String tex = filterTexture.getValue();
        String fla = filterFlavor.getValue();
        String sto = filterStorage.getValue();
        String coo = filterCooking.getValue();

        List<Food> filtered = allFoods.stream()
                .filter(f -> cat == null || cat.equals(f.getCategory()))
                .filter(f -> meal == null || meal.equals(f.getMealType()))
                .filter(f -> tex == null || tex.equals(f.getFoodTexture()))
                .filter(f -> fla == null || fla.equals(f.getFlavor()))
                .filter(f -> sto == null || sto.equals(f.getStorage()))
                .filter(f -> coo == null || coo.equals(f.getCookingMethod()))
                .toList();

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
            Label hint = new Label("在上方选择两种食物开始对比");
            hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16;");
            VBox.setVgrow(hint, Priority.ALWAYS);
            compareContent.getChildren().add(hint);
            return;
        }

        // Card area: two images + names
        HBox cardRow = new HBox(30);
        cardRow.setAlignment(Pos.CENTER);
        cardRow.setPadding(new Insets(20, 0, 30, 0));

        VBox leftCard = foodCard(f1);
        VBox rightCard = foodCard(f2);

        cardRow.getChildren().addAll(leftCard, rightCard);
        compareContent.getChildren().add(cardRow);

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0;");
        compareContent.getChildren().add(sep);

        // Spec rows
        buildSpecRow("热量", f1, f2, f -> String.format("%.0f 大卡", f.getCalories()));
        buildSpecRow("蛋白质", f1, f2, f -> String.format("%.1f g", f.getProtein()));
        buildSpecRow("脂肪", f1, f2, f -> String.format("%.1f g", f.getFat()));
        buildSpecRow("碳水化合物", f1, f2, f -> String.format("%.1f g", f.getCarb()));
        buildSpecRow("餐食类型", f1, f2, f -> nullToDash(f.getMealType()));
        buildSpecRow("口感", f1, f2, f -> nullToDash(f.getFoodTexture()));
        buildSpecRow("口味", f1, f2, f -> nullToDash(f.getFlavor()));
        buildSpecRow("储存方式", f1, f2, f -> nullToDash(f.getStorage()));
        buildSpecRow("烹饪方式", f1, f2, f -> nullToDash(f.getCookingMethod()));

        // Add some bottom padding
        compareContent.getChildren().add(new Region() {{ setMinHeight(40); }});
    }

    private VBox foodCard(Food f) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        if (f == null) {
            Label placeholder = new Label("未选择");
            placeholder.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14;");
            card.getChildren().add(placeholder);
            return card;
        }

        // Image — background loading, 180x180
        ImageView imgView = new ImageView();
        imgView.setFitWidth(180);
        imgView.setFitHeight(180);
        imgView.setPreserveRatio(true);
        try {
            String imgUrl = getClass().getResource("/images/foods/" + f.getImage()).toExternalForm();
            Image img = new Image(imgUrl, 180, 180, true, true, true);
            imgView.setImage(img);
        } catch (Exception e) {
            LOG.log(Level.FINE, "图片加载失败: " + f.getImage(), e);
        }
        card.getChildren().add(imgView);

        // Name
        Label nameLabel = new Label(f.getFoodName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setStyle("-fx-text-fill: #1e293b;");
        card.getChildren().add(nameLabel);

        // Category tag
        Label catLabel = new Label(f.getCategory());
        catLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 12;");
        card.getChildren().add(catLabel);

        return card;
    }

    private void buildSpecRow(String label, Food f1, Food f2, java.util.function.Function<Food, String> extractor) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");

        // Three columns: left value, metric name (center), right value
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Label metricLabel = new Label(label);
        metricLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");
        metricLabel.setMinWidth(100);
        metricLabel.setAlignment(Pos.CENTER);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Left food value (at start of row)
        Label leftVal = new Label(f1 != null ? extractor.apply(f1) : "-");
        leftVal.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14;");
        leftVal.setMinWidth(200);
        leftVal.setAlignment(Pos.CENTER);

        // Right food value (at end of row)
        Label rightVal = new Label(f2 != null ? extractor.apply(f2) : "-");
        rightVal.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14;");
        rightVal.setMinWidth(200);
        rightVal.setAlignment(Pos.CENTER);

        row.getChildren().addAll(leftVal, leftSpacer, metricLabel, rightSpacer, rightVal);
        compareContent.getChildren().add(row);
    }

    private static String nullToDash(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
