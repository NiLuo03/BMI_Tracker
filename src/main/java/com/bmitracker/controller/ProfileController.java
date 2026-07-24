package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.WheelPicker;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.model.User;
import com.bmitracker.service.BmiService;
import com.bmitracker.service.UserService;
import com.bmitracker.util.NotificationUtil;
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
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    private final BmiService bmiService = new BmiService();
    private Popup agePopup;
    private Popup heightPopup;
    private Popup weightPopup;
    private static final double POPUP_HEIGHT = 180;

    @FXML
    void initialize() {
        ageField.setEditable(false);
        ageField.setFocusTraversable(false);
        heightField.setEditable(false);
        heightField.setFocusTraversable(false);
        weightField.setEditable(false);
        weightField.setFocusTraversable(false);

        setupDismissHandler();

        Platform.runLater(() -> {
            javafx.stage.Window win = ageField.getScene().getWindow();
            if (win != null) {
                win.focusedProperty().addListener((o, ov, focused) -> {
                    if (focused) return;
                    if (agePopup != null) agePopup.hide();
                    if (heightPopup != null) heightPopup.hide();
                    if (weightPopup != null) weightPopup.hide();
                });
            }
        });

        setupFieldClick(ageField, 1, 100, "年龄", p -> agePopup = p, 1);
        setupFieldClick(heightField, 100, 220, "身高", p -> heightPopup = p, 100);
        setupFieldClick(weightField, 25, 250, "体重", p -> weightPopup = p, 25);

        Platform.runLater(() -> {
            User user = userService.getUserById(BMIApplication.currentUserId);
            if (user != null) {
                userIdLabel.setText(String.valueOf(user.getUserId()));
                userNameLabel.setText(user.getUserName());
                ageField.setText(String.valueOf(user.getUserAge()));
                if (user.getSex() == 0) maleRadio.setSelected(true);
                else femaleRadio.setSelected(true);
                preferencesField.setText(user.getPreferences());
                try {
                    List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
                    if (records != null && !records.isEmpty()) {
                        BmiRecord latest = records.get(0);
                        heightField.setText(String.valueOf((int) latest.getHeight()));
                        weightField.setText(String.valueOf((int) latest.getWeight()));
                    } else {
                        if (user.getHeight() > 0) heightField.setText(String.valueOf((int) user.getHeight()));
                        if (user.getWeight() > 0) weightField.setText(String.valueOf((int) user.getWeight()));
                    }
                } catch (Exception e) {
                    if (user.getHeight() > 0) heightField.setText(String.valueOf((int) user.getHeight()));
                    if (user.getWeight() > 0) weightField.setText(String.valueOf((int) user.getWeight()));
                }
            }
        });
    }

    private void setupDismissHandler() {
        ageField.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                dismiss(e, agePopup, ageField);
                dismiss(e, heightPopup, heightField);
                dismiss(e, weightPopup, weightField);
            });
        });
    }

    private void dismiss(MouseEvent event, Popup popup, TextField field) {
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

    private void setupFieldClick(TextField field, int min, int max, String label, Consumer<Popup> setter, int defaultValue) {
        field.setOnMouseClicked(e -> {
            Popup existing = popupForField(field);
            if (existing != null && existing.isShowing()) {
                existing.hide();
                return;
            }
            showPicker(field, min, max, label, setter, defaultValue);
        });
    }

    private Popup popupForField(TextField field) {
        if (field == ageField) return agePopup;
        if (field == heightField) return heightPopup;
        if (field == weightField) return weightPopup;
        return null;
    }

    private void showPicker(TextField field, int min, int max, String label, Consumer<Popup> setter, int defaultVal) {
        Popup existing = popupForField(field);
        if (existing != null) existing.hide();

        double pw = Math.max(field.getWidth(), 100);

        WheelPicker<Integer> wheel = new WheelPicker<>();
        wheel.setVisibleItems(5);
        wheel.setItemHeight(36);
        wheel.setPrefWidth(pw);
        wheel.setPrefHeight(POPUP_HEIGHT);

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
        setter.accept(popup);

        Bounds bounds = field.localToScreen(field.getBoundsInLocal());
        popup.show(field, bounds.getMaxX() + 4, bounds.getMinY());

        Platform.runLater(() -> {
            wheel.resize(pw, POPUP_HEIGHT);
            wheel.setSelectedIndex(def - min);
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

    private static boolean isParent(Node target, Node root) {
        Node n = target;
        while (n != null) {
            if (n == root) return true;
            n = n.getParent();
        }
        return false;
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
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("页面加载失败");
        }
    }

    private void showAlert(String msg) {
        NotificationUtil.show(ageField.getScene().getWindow(), NotificationUtil.Type.WARNING, "提示", msg);
    }


    private void showInfo(String msg) {
        NotificationUtil.show(ageField.getScene().getWindow(), NotificationUtil.Type.SUCCESS, "成功", msg);
    }
    private Label ageError;
    private Label heightError;
    private Label weightError;

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
