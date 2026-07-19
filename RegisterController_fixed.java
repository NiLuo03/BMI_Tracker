package com.bmitracker.controller;

import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class RegisterController {

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private TextField ageField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;

    private final UserService userService = new UserService();
    private Label userNameError;
    private Label passwordError;
    private Label confirmError;
    private Label ageError;

    @FXML
    void handleRegister(ActionEvent event) {
        clearFieldError(userNameField);
        clearFieldError(passwordField);
        clearFieldError(confirmField);
        clearFieldError(ageField);

        String userName = userNameField.getText();
        String password = passwordField.getText();
        String confirmPwd = confirmField.getText();
        String ageText = ageField.getText();
        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            showFieldError(ageField, "请输入有效年龄");
            return;
        }
        int sex = maleRadio.isSelected() ? 0 : 1;

        String error = userService.register(userName, password, confirmPwd, age, sex);
        if (error == null) {
            showInfo("注册成功！请登录");
            goToLogin();
        } else {
            showFieldError(userNameField, error);
        }
    }

    @FXML
    void goToLogin() {
        try {
            Stage stage = (Stage) userNameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("页面加载失败");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示"); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示"); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showFieldError(TextField field, String msg) {
        field.getStyleClass().add("text-field-error");
        Label errorLabel = null;
        if (field == userNameField) errorLabel = userNameError;
        else if (field == passwordField) errorLabel = passwordError;
        else if (field == confirmField) errorLabel = confirmError;
        else ageError = ageError;
        if (errorLabel == null) {
            errorLabel = new Label(msg);
            errorLabel.getStyleClass().add("error-label");
            VBox parent = (VBox) field.getParent();
            int idx = parent.getChildren().indexOf(field);
            parent.getChildren().add(idx + 1, errorLabel);
            if (field == userNameField) userNameError = errorLabel;
            else if (field == passwordField) passwordError = errorLabel;
            else if (field == confirmField) confirmError = errorLabel;
            else ageError = errorLabel;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearFieldError(TextField field) {
        field.getStyleClass().removeAll("text-field-error");
        Label errorLabel = null;
        if (field == userNameField) errorLabel = userNameError;
        else if (field == passwordField) errorLabel = passwordError;
        else if (field == confirmField) errorLabel = confirmError;
        else errorLabel = ageError;
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }
}
