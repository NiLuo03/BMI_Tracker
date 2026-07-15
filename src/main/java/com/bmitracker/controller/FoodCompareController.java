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
import java.util.function.Function;
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
        // combo 选择后立即触发对比构建
        foodCombo1.setOnAction(e -> { f1 = foodCombo1.getValue(); buildComparison(); });
        foodCombo2.setOnAction(e -> { f2 = foodCombo2.getValue(); buildComparison(); });

        loadFoods();
    }

    private void loadFoods() {
        // 初始化加载食物数据，绑定各筛选器变更事件
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
        // 为每个筛选下拉框填充可选项
        filterCategory.setItems(FXCollections.observableArrayList(foodService.getAllCategories()));
        filterMealType.setItems(FXCollections.observableArrayList(foodService.getAllMealTypes()));
        filterTexture.setItems(FXCollections.observableArrayList(foodService.getAllTextures()));
        filterFlavor.setItems(FXCollections.observableArrayList(foodService.getAllFlavors()));
        filterStorage.setItems(FXCollections.observableArrayList(foodService.getAllStorages()));
        filterCooking.setItems(FXCollections.observableArrayList(foodService.getAllCookingMethods()));
    }

    @FXML
    private void resetFilters() {
        // 一键清空所有筛选条件
        filterCategory.setValue(null);
        filterMealType.setValue(null);
        filterTexture.setValue(null);
        filterFlavor.setValue(null);
        filterStorage.setValue(null);
        filterCooking.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        // 六项筛选条件对流过滤食物列表
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

        // 若已选食物被过滤掉则清空选择
        if (f1 != null && !filtered.contains(f1)) { f1 = null; foodCombo1.setValue(null); }
        if (f2 != null && !filtered.contains(f2)) { f2 = null; foodCombo2.setValue(null); }
        buildComparison();
    }

    private void buildComparison() {
        // 构建左右对比布局：图片卡片 + 分隔线 + 逐行属性比较
        compareContent.getChildren().clear();

        if (f1 == null && f2 == null) {
            Label hint = new Label("在上方选择两种食物开始对比");
            hint.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 16;");
            VBox.setVgrow(hint, Priority.ALWAYS);
            compareContent.getChildren().add(hint);
            return;
        }

        // 左右两张食物卡片并排展示
        HBox cardRow = new HBox(50);
        cardRow.setAlignment(Pos.CENTER);
        cardRow.setPadding(new Insets(10, 0, 20, 0));

        VBox leftCard = foodCard(f1);
        VBox rightCard = foodCard(f2);

        cardRow.getChildren().addAll(leftCard, rightCard);
        compareContent.getChildren().add(cardRow);

        // Separator
        Region sep = new Region();
        sep.setMinHeight(1);
        sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: rgba(16,185,129,0.10);");
        sep.setPadding(new Insets(10, 0, 10, 0));
        compareContent.getChildren().add(sep);

        // 逐行对比营养指标和属性，左列 f1 右列 f2
        buildSpecRow("热量", f1, f2, f -> String.format("%.0f", f.getCalories()), "大卡");
        buildSpecRow("蛋白质", f1, f2, f -> String.format("%.1f", f.getProtein()), "g");
        buildSpecRow("脂肪", f1, f2, f -> String.format("%.1f", f.getFat()), "g");
        buildSpecRow("碳水化合物", f1, f2, f -> String.format("%.1f", f.getCarb()), "g");
        buildSpecRow("餐食类型", f1, f2, f -> nullToDash(f.getMealType()), null);
        buildSpecRow("口感", f1, f2, f -> nullToDash(f.getFoodTexture()), null);
        buildSpecRow("口味", f1, f2, f -> nullToDash(f.getFlavor()), null);
        buildSpecRow("储存方式", f1, f2, f -> nullToDash(f.getStorage()), null);
        buildSpecRow("烹饪方式", f1, f2, f -> nullToDash(f.getCookingMethod()), null);

        compareContent.getChildren().add(new Region() {{ setMinHeight(40); }});
    }

    // 单张食物卡片：图片 + 名称 + 分类标签
    private VBox foodCard(Food f) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16; -fx-padding: 24; -fx-border-color: rgba(16,185,129,0.10); -fx-border-width: 1; -fx-border-radius: 16;");

        if (f == null) {
            Label placeholder = new Label("未选择");
            placeholder.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 14;");
            card.getChildren().add(placeholder);
            return card;
        }

        ImageView imgView = new ImageView();
        imgView.setFitWidth(280);
        imgView.setFitHeight(280);
        imgView.setPreserveRatio(true);
        try {
            String imgUrl = getClass().getResource("/images/foods/" + f.getImage()).toExternalForm();
            Image img = new Image(imgUrl, 280, 280, true, true, true);
            imgView.setImage(img);
        } catch (Exception e) {
            LOG.log(Level.FINE, "图片加载失败: " + f.getImage(), e);
        }
        card.getChildren().add(imgView);

        Label nameLabel = new Label(f.getFoodName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setStyle("-fx-text-fill: #d0d0d0;");
        card.getChildren().add(nameLabel);

        Label catLabel = new Label(f.getCategory());
        catLabel.setStyle("-fx-background-color: rgba(16,185,129,0.12); -fx-text-fill: #34d399; -fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12;");
        card.getChildren().add(catLabel);

        return card;
    }

    // 单行属性对比：左食物值 | 属性名 | 右食物值，支持单位后缀
    private void buildSpecRow(String label, Food f1, Food f2, Function<Food, String> valueExtractor, String unit) {
        HBox row = new HBox(135);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(18, 40, 18, 40));

        VBox leftCol = new VBox(2);
        leftCol.setAlignment(Pos.CENTER);
        leftCol.setPrefWidth(240);

        String leftText = f1 != null ? valueExtractor.apply(f1) : "-";
        Label leftVal = new Label(unit != null ? leftText + " " + unit : leftText);
        leftVal.setFont(Font.font("System", FontWeight.BOLD, 22));
        leftVal.setStyle("-fx-text-fill: #d0d0d0;");

        Label leftName = new Label(label);
        leftName.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        leftCol.getChildren().addAll(leftVal, leftName);

        VBox rightCol = new VBox(2);
        rightCol.setAlignment(Pos.CENTER);
        rightCol.setPrefWidth(240);

        String rightText = f2 != null ? valueExtractor.apply(f2) : "-";
        Label rightVal = new Label(unit != null ? rightText + " " + unit : rightText);
        rightVal.setFont(Font.font("System", FontWeight.BOLD, 22));
        rightVal.setStyle("-fx-text-fill: #d0d0d0;");

        Label rightName = new Label(label);
        rightName.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        rightCol.getChildren().addAll(rightVal, rightName);

        row.getChildren().addAll(leftCol, rightCol);
        compareContent.getChildren().add(row);
    }

    // 空值或空串转占位符 "-"
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
