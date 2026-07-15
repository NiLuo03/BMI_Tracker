package com.bmitracker.controller;

import com.bmitracker.model.Food;
import com.bmitracker.service.FoodService;
import com.bmitracker.service.FoodServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class FoodRankController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TableView<Food> rankTable;
    @FXML private TableColumn<Food, String> colName;
    @FXML private TableColumn<Food, Double> colCal;
    @FXML private TableColumn<Food, Double> colProtein;
    @FXML private TableColumn<Food, Double> colFat;
    @FXML private TableColumn<Food, Double> colCarb;

    private final FoodService foodService = new FoodServiceImpl();

    @FXML
    void initialize() {
        // 绑定表格列与 Food 模型字段
        colName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        colCal.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colProtein.setCellValueFactory(new PropertyValueFactory<>("protein"));
        colFat.setCellValueFactory(new PropertyValueFactory<>("fat"));
        colCarb.setCellValueFactory(new PropertyValueFactory<>("carb"));

        // 加载分类下拉列表
        List<String> categories = foodService.getAllCategories();
        if (categories != null) {
            categoryCombo.getItems().addAll(categories);
        }

        // 选择分类后自动刷新排行
        categoryCombo.setOnAction(e -> loadRank());
    }

    private void loadRank() {
        // 按当前分类查询 Top 食品并刷新表格
        String category = categoryCombo.getValue();
        if (category == null) return;
        List<Food> list = foodService.getTopByCategory(category);
        if (list != null) {
            rankTable.getItems().setAll(list);
        }
    }
}
