package com.bmitracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentPane;

    @FXML
    void showBmiRecord(ActionEvent event) { loadView("bmi_record.fxml"); }

    @FXML
    void showHistory(ActionEvent event) { loadView("history.fxml"); }

    @FXML
    void showPrediction(ActionEvent event) { loadView("prediction.fxml"); }

    @FXML
    void showDiet(ActionEvent event) { loadView("diet.fxml"); }

    @FXML
    void showFood(ActionEvent event) { loadView("food_compare.fxml"); }

    @FXML
    void showFoodRank(ActionEvent event) { loadView("food_rank.fxml"); }

    @FXML
    void showProfile(ActionEvent event) { loadView("profile.fxml"); }

    @FXML
    void showChart(ActionEvent event) { loadView("chart.fxml"); }

    private void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
