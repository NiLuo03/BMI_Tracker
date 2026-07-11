package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class FoodRankingController implements Initializable {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TableView<Food> rankingTable;
    @FXML private TableColumn<Food, String> nameColumn;
    @FXML private TableColumn<Food, String> categoryColumn;
    @FXML private TableColumn<Food, Number> caloriesColumn;
    @FXML private TableColumn<Food, Number> proteinColumn;
    @FXML private TableColumn<Food, Number> fatColumn;
    @FXML private TableColumn<Food, Number> carbColumn;

    private FoodService foodService;
    private final ObservableList<Food> allFoods = FXCollections.observableArrayList();
    private final Label badge = new Label("低卡推荐");

    public FoodRankingController() {
        badge.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                "-fx-padding: 2px 6px; -fx-background-radius: 10px; -fx-font-size: 10px;");
    }

    public void setFoodService(FoodService foodService) {
        this.foodService = foodService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        caloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        proteinColumn.setCellValueFactory(new PropertyValueFactory<>("protein"));
        fatColumn.setCellValueFactory(new PropertyValueFactory<>("fat"));
        carbColumn.setCellValueFactory(new PropertyValueFactory<>("carb"));

        categoryCombo.getItems().addAll("全部", "主食", "肉类", "蔬菜", "水果", "饮品");
        categoryCombo.setValue("全部");
        categoryCombo.setOnAction(e -> filterAndDisplay());

        caloriesColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                    return;
                }
                setText(String.valueOf(item));
                setStyle("");
                setGraphic(null);

                if (getIndex() == 0 && getTableView().getItems().size() > 0) {
                    setStyle("-fx-background-color: #c8e6c9; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setText(String.valueOf(item) + "  ");
                }
            }
        });
    }

    public void loadFoods() {
        if (foodService == null) return;
        try {
            allFoods.setAll(foodService.getAllFoods());
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("加载失败", "无法加载食物数据：" + e.getMessage());
        }
    }

    private void filterAndDisplay() {
        String category = categoryCombo.getValue();
        List<Food> top10 = allFoods.stream()
                .filter(f -> category == null || "全部".equals(category) || f.getCategory().equals(category))
                .sorted(Comparator.comparingDouble(Food::getCalories))
                .limit(10)
                .toList();

        rankingTable.setItems(FXCollections.observableArrayList(top10));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
