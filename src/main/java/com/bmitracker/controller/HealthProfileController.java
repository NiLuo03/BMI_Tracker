package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HealthProfileController {

    @FXML private TextField allergensField;
    @FXML private TextField diseasesField;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();

    @FXML
    void initialize() {
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user != null) {
            if (user.getAllergens() != null) allergensField.setText(user.getAllergens());
            if (user.getChronicDiseases() != null) diseasesField.setText(user.getChronicDiseases());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user == null) return;
        user.setAllergens(allergensField.getText().trim());
        user.setChronicDiseases(diseasesField.getText().trim());
        String error = userService.updateProfile(user);
        if (error == null) {
            statusLabel.setText("已保存 ✓");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
        } else {
            showAlert(error);
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
