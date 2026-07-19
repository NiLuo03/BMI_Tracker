package com.bmitracker.controller;

import com.bmitracker.component.WheelPicker;
import com.bmitracker.service.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RegisterController {

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private TextField ageField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;

    private final UserService userService = new UserService();
    private Popup agePopup;

    @FXML
    void initialize() {
        ageField.setEditable(false);
        ageField.setFocusTraversable(false);

        ageField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (agePopup == null || !agePopup.isShowing()) return;
                EventTarget target = event.getTarget();
                if (target instanceof Node) {
                    Node node = (Node) target;
                    for (Node popupNode : agePopup.getContent()) {
                        if (isInside(node, popupNode)) return;
                    }
                }
                agePopup.hide();
            });
        });

        Platform.runLater(() -> {
            javafx.stage.Window win = ageField.getScene().getWindow();
            if (win != null) {
                win.focusedProperty().addListener((o, ov, focused) -> {
                    if (focused) return;
                    if (agePopup != null) agePopup.hide();
                });
            }
        });

        ageField.setOnMouseClicked(e -> {
            if (agePopup != null && agePopup.isShowing()) {
                agePopup.hide();
                return;
            }
            showAgePopup();
        });
    }

    private void showAgePopup() {
        if (agePopup != null) agePopup.hide();

        double popupWidth = ageField.getWidth();

        WheelPicker<Integer> wheel = new WheelPicker<>();
        wheel.setVisibleItems(5);
        wheel.setItemHeight(36);
        wheel.setPrefWidth(popupWidth);
        wheel.setPrefHeight(180);
        wheel.setLightTheme(isLightTheme());

        List<Integer> ages = new ArrayList<>();
        for (int i = 1; i <= 100; i++) ages.add(i);
        wheel.setItems(ages);

        int currentAge = 25;
        try {
            currentAge = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ignored) {
        }
        final int defaultAge = currentAge;

        wheel.valueProperty().addListener((obs, old, val) -> {
            if (val != null) ageField.setText(String.valueOf(val));
        });

        StackPane container = new StackPane(wheel);
        container.setPadding(new Insets(12, 0, 12, 0));
        container.setMinWidth(popupWidth);
        container.getStyleClass().add("wheel-popup");
        if (isLightTheme()) container.getStyleClass().add("light-theme");
        container.setVisible(false);

        agePopup = new Popup();
        agePopup.setAutoHide(false);
        agePopup.setHideOnEscape(true);
        agePopup.getContent().add(container);

        Bounds bounds = ageField.localToScreen(ageField.getBoundsInLocal());
        agePopup.show(ageField, bounds.getMinX(), bounds.getMaxY());

        Platform.runLater(() -> {
            wheel.resize(popupWidth, 180);
            wheel.setSelectedIndex(defaultAge - 1);
            container.setVisible(true);
        });
    }

    private boolean isLightTheme() {
        Scene scene = ageField.getScene();
        if (scene != null && scene.getRoot() instanceof Parent) {
            return ((Parent) scene.getRoot()).getStyleClass().contains("light-theme");
        }
        return false;
    }

    private static boolean isInside(Node target, Node root) {
        Node n = target;
        while (n != null) {
            if (n == root) return true;
            n = n.getParent();
        }
        return false;
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String userName = userNameField.getText();
        String password = passwordField.getText();
        String confirmPwd = confirmField.getText();
        String ageText = ageField.getText();
        int age;
        if (ageText == null || ageText.trim().isEmpty()) {
            age = 25;
        } else {
            try {
                age = Integer.parseInt(ageText.trim());
            } catch (NumberFormatException e) {
                showAlert("请输入有效年龄");
                return;
            }
        }
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
