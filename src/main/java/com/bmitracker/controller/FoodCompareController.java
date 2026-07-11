package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class FoodCompareController {

    @FXML private ComboBox<Food> foodCombo1;
    @FXML private ComboBox<Food> foodCombo2;
    @FXML private ComboBox<Food> foodCombo3;
    @FXML private TableView<Food> compareTable;
    @FXML private TableColumn<Food, String> colName;
    @FXML private TableColumn<Food, Double> colCal;
    @FXML private TableColumn<Food, Double> colProtein;
    @FXML private TableColumn<Food, Double> colFat;
    @FXML private TableColumn<Food, Double> colCarb;

    private final FoodService foodService = new FoodService();

    @FXML
    void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        colCal.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colProtein.setCellValueFactory(new PropertyValueFactory<>("protein"));
        colFat.setCellValueFactory(new PropertyValueFactory<>("fat"));
        colCarb.setCellValueFactory(new PropertyValueFactory<>("carb"));

        List<String> categories = foodService.getAllCategories();
        if (categories != null) {
            foodCombo1.getItems().addAll(FXCollections.observableArrayList(
                    foodService.getFoodsByIds(List.of(1))));
        }
    }

    @FXML
    void handleCompare(ActionEvent event) {
        // 简化版：直接用 ComboBox 选好的食物对比
    }
}
