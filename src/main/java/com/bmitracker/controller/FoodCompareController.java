package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class FoodCompareController implements Initializable {

    @FXML private ComboBox<Food> foodCombo1;
    @FXML private ComboBox<Food> foodCombo2;
    @FXML private ComboBox<Food> foodCombo3;
    @FXML private TableView<CompareRow> compareTable;
    @FXML private TableColumn<CompareRow, String> metricColumn;
    @FXML private TableColumn<CompareRow, String> food1Column;
    @FXML private TableColumn<CompareRow, String> food2Column;
    @FXML private TableColumn<CompareRow, String> food3Column;

    private FoodService foodService;
    private Food f1, f2, f3;
    private Food minCalFood, maxProFood;

    public void setFoodService(FoodService foodService) {
        this.foodService = foodService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        metricColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        setupCompareColumns(food1Column, 1);
        setupCompareColumns(food2Column, 2);
        setupCompareColumns(food3Column, 3);

        foodCombo1.setOnAction(e -> { f1 = foodCombo1.getValue(); updateComparison(); });
        foodCombo2.setOnAction(e -> { f2 = foodCombo2.getValue(); updateComparison(); });
        foodCombo3.setOnAction(e -> { f3 = foodCombo3.getValue(); updateComparison(); });
    }

    public void loadFoods() {
        if (foodService == null) return;
        try {
            foodCombo1.setItems(FXCollections.observableArrayList(foodService.getAllFoods()));
            foodCombo2.setItems(FXCollections.observableArrayList(foodService.getAllFoods()));
            foodCombo3.setItems(FXCollections.observableArrayList(foodService.getAllFoods()));
        } catch (Exception e) {
            showAlert("加载失败", "无法加载食物数据：" + e.getMessage());
        }
    }

    private void setupCompareColumns(TableColumn<CompareRow, String> col, int foodIdx) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                int rowIdx = getIndex();
                Food food = foodForColumn(foodIdx);
                String style = "";
                if (rowIdx == 0 && food != null && food == minCalFood) {
                    style = "-fx-background-color: #c8e6c9; -fx-font-weight: bold;";
                } else if (rowIdx == 1 && food != null && food == maxProFood) {
                    style = "-fx-background-color: #bbdefb; -fx-font-weight: bold;";
                }
                setStyle(style);
            }
        });
    }

    private Food foodForColumn(int idx) {
        return switch (idx) {
            case 1 -> f1;
            case 2 -> f2;
            case 3 -> f3;
            default -> null;
        };
    }

    private void updateComparison() {
        if (f1 == null && f2 == null && f3 == null) {
            compareTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        food1Column.setText(f1 != null ? f1.getFoodName() : "-");
        food2Column.setText(f2 != null ? f2.getFoodName() : "-");
        food3Column.setText(f3 != null ? f3.getFoodName() : "-");

        List<Food> selected = Stream.of(f1, f2, f3).filter(f -> f != null).toList();
        minCalFood = selected.size() >= 2
                ? selected.stream().min(Comparator.comparingDouble(Food::getCalories)).orElse(null)
                : null;
        maxProFood = selected.size() >= 2
                ? selected.stream().max(Comparator.comparingDouble(Food::getProtein)).orElse(null)
                : null;

        ObservableList<CompareRow> rows = FXCollections.observableArrayList();
        rows.add(new CompareRow("热量(大卡)", val(f1, Food::getCalories), val(f2, Food::getCalories), val(f3, Food::getCalories)));
        rows.add(new CompareRow("蛋白质(g)", val(f1, Food::getProtein), val(f2, Food::getProtein), val(f3, Food::getProtein)));
        rows.add(new CompareRow("脂肪(g)", val(f1, Food::getFat), val(f2, Food::getFat), val(f3, Food::getFat)));
        rows.add(new CompareRow("碳水(g)", val(f1, Food::getCarb), val(f2, Food::getCarb), val(f3, Food::getCarb)));
        compareTable.setItems(rows);
    }

    private String val(Food f, ToDoubleFunction<Food> extractor) {
        return f != null ? String.valueOf(extractor.applyAsDouble(f)) : "-";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CompareRow {
        private final StringProperty metric = new SimpleStringProperty();
        private final StringProperty food1 = new SimpleStringProperty();
        private final StringProperty food2 = new SimpleStringProperty();
        private final StringProperty food3 = new SimpleStringProperty();

        public CompareRow(String metric, String food1, String food2, String food3) {
            this.metric.set(metric);
            this.food1.set(food1);
            this.food2.set(food2);
            this.food3.set(food3);
        }

        public String getMetric() { return metric.get(); }
        public StringProperty metricProperty() { return metric; }
        public String getFood1() { return food1.get(); }
        public StringProperty food1Property() { return food1; }
        public String getFood2() { return food2.get(); }
        public StringProperty food2Property() { return food2; }
        public String getFood3() { return food3.get(); }
        public StringProperty food3Property() { return food3; }
    }
}
