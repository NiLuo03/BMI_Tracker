package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField oldPwdField;
    @FXML private PasswordField newPwdField;
    @FXML private PasswordField confirmPwdField;

    private final UserService userService = new UserService();
    private Label oldPwdError;
    private Label newPwdError;
    private Label confirmPwdError;

    @FXML
    void handleChange(ActionEvent event) {
        clearFieldError(oldPwdField);
        clearFieldError(newPwdField);
        clearFieldError(confirmPwdField);

        String oldPwd = oldPwdField.getText();
        String newPwd = newPwdField.getText();
        String confirmPwd = confirmPwdField.getText();

        String error = userService.changePassword(BMIApplication.currentUserId, oldPwd, newPwd, confirmPwd);
        if (error == null) {
            showInfo("密码修改成功，请重新登录");
            BMIApplication.currentUserId = -1;
            try {
                Stage stage = (Stage) oldPwdField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 800);
                scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
                stage.setScene(scene);
                stage.centerOnScreen();
            } catch (Exception e) {
                showAlert("页面加载失败");
            }
        } else {
            showFieldError(oldPwdField, error);
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) oldPwdField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
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
        if (field == oldPwdField) errorLabel = oldPwdError;
        else if (field == newPwdField) errorLabel = newPwdError;
        else if (field == confirmPwdField) errorLabel = confirmPwdError;
        if (errorLabel == null) {
            errorLabel = new Label(msg);
            errorLabel.getStyleClass().add("error-label");
            VBox parent = (VBox) field.getParent();
            int idx = parent.getChildren().indexOf(field);
            parent.getChildren().add(idx + 1, errorLabel);
            if (field == oldPwdField) oldPwdError = errorLabel;
            else if (field == newPwdField) newPwdError = errorLabel;
            else if (field == confirmPwdField) confirmPwdError = errorLabel;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearFieldError(TextField field) {
        field.getStyleClass().removeAll("text-field-error");
        Label errorLabel = null;
        if (field == oldPwdField) errorLabel = oldPwdError;
        else if (field == newPwdField) errorLabel = newPwdError;
        else if (field == confirmPwdField) errorLabel = confirmPwdError;
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }
}
