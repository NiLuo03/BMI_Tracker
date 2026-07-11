package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField oldPwdField;
    @FXML private PasswordField newPwdField;
    @FXML private PasswordField confirmPwdField;

    private final UserService userService = new UserService();

    @FXML
    void handleChange(ActionEvent event) {
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
                stage.setScene(new Scene(loader.load(), 400, 350));
                stage.centerOnScreen();
            } catch (Exception e) {
                showAlert("页面加载失败");
            }
        } else {
            showAlert(error);
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) oldPwdField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            stage.setScene(new Scene(loader.load(), 600, 400));
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
}
