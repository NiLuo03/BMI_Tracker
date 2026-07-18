package com.bmitracker.controller;

import com.bmitracker.component.WheelPicker;
import com.bmitracker.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegisterController {

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private HBox ageDisplayBox;
    @FXML private Label ageDisplayText;
    @FXML private WheelPicker<Integer> agePicker;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private VBox root;
    @FXML private Rectangle rootClip;

    private final UserService userService = new UserService();
    private boolean agePickerVisible = false;

    @FXML
    void initialize() {
        rootClip.widthProperty().bind(root.widthProperty());
        rootClip.heightProperty().bind(root.heightProperty());
        agePicker.setItems(IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()));
        agePicker.setValue(25);

        agePicker.valueProperty().addListener((obs, old, val) -> {
            ageDisplayText.setText(String.valueOf(val));
            hideAgePicker();
        });

        ageDisplayBox.setOnMouseClicked(e -> toggleAgePicker());

        userNameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> confirmField.requestFocus());
        confirmField.setOnAction(e -> handleRegister(null));
    }

    private void toggleAgePicker() {
        if (agePickerVisible) {
            hideAgePicker();
        } else {
            showAgePicker();
        }
    }

    private void showAgePicker() {
        agePickerVisible = true;
        agePicker.setVisible(true);
        agePicker.setManaged(true);
    }

    private void hideAgePicker() {
        agePickerVisible = false;
        agePicker.setVisible(false);
        agePicker.setManaged(false);
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String userName = userNameField.getText();
        String password = passwordField.getText();
        String confirmPwd = confirmField.getText();
        int age = agePicker.getValue();
        int sex = maleRadio.isSelected() ? 0 : 1;

        String error = userService.register(userName, password, confirmPwd, age, sex);
        if (error == null) {
            showInfo("注册成功！请登录");
            goToLogin();
        } else {
            showAlert(error);
        }
    }

    @FXML
    void goToLogin() {
        try {
            Stage stage = (Stage) userNameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
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
}
