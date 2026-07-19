package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.User;
import com.bmitracker.service.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class ProfileController {

    @FXML private Label userIdLabel;
    @FXML private Label userNameLabel;
    @FXML private TextField ageField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TextField preferencesField;

    private final UserService userService = new UserService();
    private Label ageError;
    private Label heightError;
    private Label weightError;

    @FXML
    void initialize() {
        Platform.runLater(() -> {
            User user = userService.getUserById(BMIApplication.currentUserId);
            if (user != null) {
                userIdLabel.setText(String.valueOf(user.getUserId()));
                userNameLabel.setText(user.getUserName());
                ageField.setText(String.valueOf(user.getUserAge()));
                if (user.getSex() == 0) maleRadio.setSelected(true);
                else femaleRadio.setSelected(true);
                if (user.getHeight() > 0) heightField.setText(String.valueOf(user.getHeight()));
                if (user.getWeight() > 0) weightField.setText(String.valueOf(user.getWeight()));
                preferencesField.setText(user.getPreferences());
            }
        });
    }

    @FXML
    void handleSave(ActionEvent event) {
        clearFieldError(ageField);
        clearFieldError(heightField);
        clearFieldError(weightField);

        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user == null) return;
        try {
            user.setUserAge(Integer.parseInt(ageField.getText()));
        } catch (NumberFormatException e) {
            showFieldError(ageField, "请输入有效年龄"); return;
        }
        user.setSex(maleRadio.isSelected() ? 0 : 1);
        try {
            user.setHeight(Double.parseDouble(heightField.getText()));
            user.setWeight(Double.parseDouble(weightField.getText()));
        } catch (NumberFormatException e) {
            showFieldError(heightField, "请输入有效身高");
            showFieldError(weightField, "请输入有效体重"); return;
        }
        user.setPreferences(preferencesField.getText());

        String error = userService.updateProfile(user);
        if (error == null) {
            showInfo("保存成功");
        } else {
            showFieldError(ageField, error);
        }
    }

    @FXML
    void goToChangePassword(ActionEvent event) {
        try {
            Stage stage = (Stage) userIdLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/change_password.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("页面加载失败");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showFieldError(TextField field, String msg) {
        field.getStyleClass().add("text-field-error");
        Label errorLabel = null;
        if (field == ageField) errorLabel = ageError;
        else if (field == heightField) errorLabel = heightError;
        else if (field == weightField) errorLabel = weightError;
        if (errorLabel == null) {
            errorLabel = new Label(msg);
            errorLabel.getStyleClass().add("error-label");
            VBox parent = (VBox) field.getParent();
            int idx = parent.getChildren().indexOf(field);
            parent.getChildren().add(idx + 1, errorLabel);
            if (field == ageField) ageError = errorLabel;
            else if (field == heightField) heightError = errorLabel;
            else if (field == weightField) weightError = errorLabel;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearFieldError(TextField field) {
        field.getStyleClass().removeAll("text-field-error");
        Label errorLabel = null;
        if (field == ageField) errorLabel = ageError;
        else if (field == heightField) errorLabel = heightError;
        else if (field == weightField) errorLabel = weightError;
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }
}
