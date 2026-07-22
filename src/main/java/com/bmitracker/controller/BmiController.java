package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.WheelPicker;
import com.bmitracker.model.User;
import com.bmitracker.service.BmiService;
import com.bmitracker.service.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class BmiController {

    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private VBox healthCard;
    @FXML private VBox bmiEmptyState;
    @FXML private FlowPane allergenPane, diseasePane;
    @FXML private Label allergenEmptyLabel, diseaseEmptyLabel;

    private final BmiService bmiService = new BmiService();
    private final UserService userService = new UserService();
    private Popup heightPopup;
    private Popup weightPopup;

    @FXML
    void initialize() {
        heightField.setEditable(false);
        heightField.setFocusTraversable(false);
        weightField.setEditable(false);
        weightField.setFocusTraversable(false);

        Scene scene = heightField.getScene();
        if (scene != null) {
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissPopup(e, heightPopup, heightField));
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissPopup(e, weightPopup, weightField));
        } else {
            heightField.sceneProperty().addListener((obs, old, sc) -> {
                if (sc != null) {
                    sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissPopup(e, heightPopup, heightField));
                    sc.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> dismissPopup(e, weightPopup, weightField));
                }
            });
        }

        Platform.runLater(() -> {
            javafx.stage.Window win = heightField.getScene().getWindow();
            if (win != null) {
                win.focusedProperty().addListener((o, ov, focused) -> {
                    if (focused) return;
                    if (heightPopup != null) heightPopup.hide();
                    if (weightPopup != null) weightPopup.hide();
                });
            }
        });

        boolean hasRecords = bmiService.getRecordCount(BMIApplication.currentUserId) > 0;
        if (hasRecords) {
            bmiEmptyState.setVisible(false);
            bmiEmptyState.setManaged(false);
        } else {
            bmiEmptyState.setVisible(true);
            bmiEmptyState.setManaged(true);
        }

        heightField.setOnMouseClicked(e -> {
            if (heightPopup != null && heightPopup.isShowing()) {
                heightPopup.hide();
                return;
            }
            if (heightField.getText() == null || heightField.getText().trim().isEmpty()) {
                heightField.setText("165");
            }
            showPicker(heightField, heightPopup, 100, 220, 165, picker -> heightPopup = picker);
        });

        weightField.setOnMouseClicked(e -> {
            if (weightPopup != null && weightPopup.isShowing()) {
                weightPopup.hide();
                return;
            }
            if (weightField.getText() == null || weightField.getText().trim().isEmpty()) {
                weightField.setText("60");
            }
            showPicker(weightField, weightPopup, 25, 250, 60, picker -> weightPopup = picker);
        });

        loadHealthProfile();
    }

    private void loadHealthProfile() {
        User user = userService.getUserById(BMIApplication.currentUserId);
        if (user == null) return;

        fillTags(allergenPane, allergenEmptyLabel, user.getAllergens(), "尚未记录过敏信息");
        fillTags(diseasePane, diseaseEmptyLabel, user.getChronicDiseases(), "尚未记录慢性病史");
    }

    private void fillTags(FlowPane pane, Label emptyLabel, String csv, String emptyText) {
        pane.getChildren().clear();
        if (csv == null || csv.trim().isEmpty()) {
            emptyLabel.setVisible(true);
            return;
        }
        emptyLabel.setVisible(false);
        String[] items = csv.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) continue;
            Button tag = new Button(trimmed);
            tag.setStyle("-fx-background-color: #1a6b3c; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 13px;");
            pane.getChildren().add(tag);
        }
    }

    @FXML
    void handleEditHealth() {
        MainController main = MainController.getInstance();
        if (main != null) main.loadView("health_edit.fxml");
    }

    private void dismissPopup(MouseEvent event, Popup popup, TextField field) {
        if (popup == null || !popup.isShowing()) return;
        EventTarget target = event.getTarget();
        if (target instanceof Node) {
            Node node = (Node) target;
            if (node == field) return;
            for (Node n : popup.getContent()) {
                if (isParent(node, n)) return;
            }
        }
        popup.hide();
    }

    private void showPicker(TextField field, Popup existingPopup, int min, int max, int defaultVal, java.util.function.Consumer<Popup> setter) {
        if (existingPopup != null) existingPopup.hide();

        double pw = Math.max(field.getWidth(), 100);

        WheelPicker<Integer> wheel = new WheelPicker<>();
        wheel.setVisibleItems(5);
        wheel.setItemHeight(36);
        wheel.setPrefWidth(pw);
        wheel.setPrefHeight(180);

        List<Integer> items = new ArrayList<>();
        for (int i = min; i <= max; i++) items.add(i);
        wheel.setItems(items);

        int current = defaultVal;
        try {
            current = Integer.parseInt(field.getText().trim());
            if (current < min || current > max) current = defaultVal;
        } catch (NumberFormatException ignored) {}
        final int def = current;

        wheel.valueProperty().addListener((obs, old, val) -> {
            if (val != null) field.setText(String.valueOf(val));
        });

        wheel.setLightTheme(isLightTheme());

        StackPane container = new StackPane(wheel);
        container.setPadding(new Insets(12, 0, 12, 0));
        container.setMinWidth(pw);
        container.getStyleClass().add("wheel-popup");
        if (isLightTheme()) container.getStyleClass().add("light-theme");
        container.setVisible(false);

        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        popup.getContent().add(container);
        wheel.setOnMouseClicked(ev -> popup.hide());
        setter.accept(popup);

        Bounds bounds = field.localToScreen(field.getBoundsInLocal());
        popup.show(field, bounds.getMinX(), bounds.getMaxY());

        Platform.runLater(() -> {
            wheel.resize(pw, 180);
            wheel.setSelectedIndex(def - min);
            container.setVisible(true);
        });
    }

    private boolean isLightTheme() {
        Scene scene = heightField.getScene();
        if (scene != null && scene.getRoot() instanceof Parent) {
            return ((Parent) scene.getRoot()).getStyleClass().contains("light-theme");
        }
        return false;
    }

    private static boolean isParent(Node target, Node root) {
        Node n = target;
        while (n != null) {
            if (n == root) return true;
            n = n.getParent();
        }
        return false;
    }

    @FXML
    void handleCalculate(ActionEvent event) {
        clearFieldError(heightField);
        clearFieldError(weightField);
        String heightText = heightField.getText();
        String weightText = weightField.getText();
        double height, weight;
        try {
            height = Double.parseDouble(heightText);
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            showFieldError(heightField, "请输入有效的身高");
            showFieldError(weightField, "请输入有效的体重");
            return;
        }
        String error = bmiService.saveRecord(BMIApplication.currentUserId, height, weight);
        if (error == null) {
            double bmi = bmiService.calculateBMI(height, weight);
            String status = bmiService.getHealthStatus(bmi);
            showInfo(String.format("您的 BMI 为 %.1f，状态：%s", bmi, status));
            heightField.clear();
            weightField.clear();
            bmiEmptyState.setVisible(false);
            bmiEmptyState.setManaged(false);
        } else {
            showFieldError(heightField, error);
        }
    }


    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("BMI结果"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private Label heightError;
    private Label weightError;

    private void showFieldError(TextField field, String msg) {
        field.getStyleClass().add("text-field-error");
        Label errorLabel = (field == heightField) ? heightError : weightError;
        if (errorLabel == null) {
            errorLabel = new Label(msg);
            errorLabel.getStyleClass().add("error-label");
            VBox parent = (VBox) field.getParent();
            int idx = parent.getChildren().indexOf(field);
            parent.getChildren().add(idx + 1, errorLabel);
            if (field == heightField) heightError = errorLabel;
            else weightError = errorLabel;
        } else {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearFieldError(TextField field) {
        field.getStyleClass().removeAll("text-field-error");
        Label errorLabel = (field == heightField) ? heightError : weightError;
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }
}
