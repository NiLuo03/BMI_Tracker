package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.component.TitleBar;
import com.bmitracker.model.User;
import com.bmitracker.service.UserService;
import com.bmitracker.util.ParticleTextCanvas;
import com.bmitracker.util.NotificationUtil;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private Canvas particleCanvas;
    @FXML private StackPane particlePane;
    @FXML private Rectangle rootClip;
    @FXML private VBox root;
    @FXML private TitleBar titleBar;

    private ParticleTextCanvas particleText;

    @FXML
    void initialize() {
        if (particleCanvas != null) {
            particleText = new ParticleTextCanvas(
                    1200, 800,
                    new String[]{"BMI", "PHYSICAL", "ASSESSMENT", "PREDICTION", "HEALTH"});
            particlePane.getChildren().clear();
            particlePane.getChildren().add(particleText);
        }
        if (rootClip != null && root != null) {
            rootClip.widthProperty().bind(root.widthProperty());
            rootClip.heightProperty().bind(root.heightProperty());
        }
        Platform.runLater(() -> userNameField.requestFocus());
        if (titleBar != null) titleBar.setMaximizeVisible(false);
    }

    private final UserService userService = new UserService();

    @FXML
    void onUserNameAction() {
        passwordField.requestFocus();
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String userName = userNameField.getText();
        String password = passwordField.getText();
        if (userName.isEmpty() || password.isEmpty()) {
            showAlert("请输入账号和密码");
            return;
        }
        int userId = userService.login(userName, password);
        if (userId > 0) {
            BMIApplication.currentUserId = userId;
            User user = userService.getUserById(userId);
            if (user != null && user.needsHealthProfile()) {
                navigateToHealthSetup();
            } else {
                navigateToMain();
            }
        } else {
            showAlert("账号或密码错误");
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        if (particleText != null) particleText.stop();
        try {
            Stage stage = (Stage) userNameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("页面加载失败");
        }
    }

    private void navigateToHealthSetup() {
        try {
            Stage stage = (Stage) userNameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/health_setup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            if (particleText != null) particleText.stop();
        } catch (Exception e) {
            showAlert("页面加载失败");
        }
    }

    private void navigateToMain() {
        try {
            Stage stage = (Stage) userNameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot);
            newScene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            newScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            newRoot.setOpacity(0);

            stage.setScene(newScene);
            stage.setResizable(true);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();

            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(newRoot.opacityProperty(), 1))
            );
            fadeIn.play();

            if (particleText != null) particleText.stop();
        } catch (Exception e) {
            showAlert("系统繁忙，请稍后再试");
        }
    }

    private void showAlert(String msg) {
        NotificationUtil.show(userNameField.getScene().getWindow(), NotificationUtil.Type.WARNING, "提示", msg);
    }
}
