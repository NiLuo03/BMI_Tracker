package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.service.UserService;
import com.bmitracker.util.ParticleTextCanvas;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private Canvas particleCanvas;
    @FXML private StackPane particlePane;
    @FXML private BorderPane loginCard;

    private ParticleTextCanvas particleText;
    private Rotate rotateX;
    private Rotate rotateY;

    @FXML
    void initialize() {
        if (particleCanvas != null) {
            particleText = new ParticleTextCanvas(
                    1200, 800,
                    new String[]{"BMI", "PHYSICAL", "ASSESSMENT", "PREDICTION", "HEALTH"});
            particlePane.getChildren().clear();
            particlePane.getChildren().add(particleText);
        }
        if (loginCard != null) {
            initCardTilt();
        }
    }

    private void initCardTilt() {
        rotateX = new Rotate(0, Rotate.X_AXIS);
        rotateY = new Rotate(0, Rotate.Y_AXIS);
        loginCard.getTransforms().addAll(rotateX, rotateY);

        loginCard.setOnMouseMoved(e -> {
            double halfW = loginCard.getWidth() / 2;
            double halfH = loginCard.getHeight() / 2;
            if (halfW <= 0 || halfH <= 0) return;
            double dx = (e.getX() - halfW) / halfW;
            double dy = (e.getY() - halfH) / halfH;
            rotateY.setAngle(dx * 8);
            rotateX.setAngle(dy * -8);
        });

        loginCard.setOnMouseExited(e -> {
            Timeline reset = new Timeline(
                new KeyFrame(Duration.millis(300),
                    new KeyValue(rotateX.angleProperty(), 0),
                    new KeyValue(rotateY.angleProperty(), 0))
            );
            reset.play();
        });
    }

    private final UserService userService = new UserService();

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
            try {
                Stage stage = (Stage) userNameField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                Scene newScene = new Scene(loader.load(), 1200, 800);
                newScene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
                newScene.setFill(null);

                // 淡入动画
                Scene oldScene = stage.getScene();
                Node oldRoot = oldScene.getRoot();
                Parent newRoot = newScene.getRoot();
                newRoot.setOpacity(0);

                stage.setScene(newScene);
                stage.setResizable(true);

                Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.millis(400),
                        new KeyValue(newRoot.opacityProperty(), 1))
                );
                fadeIn.play();

                if (particleText != null) particleText.stop();
            } catch (Exception e) {
                showAlert("系统繁忙，请稍后再试");
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
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
