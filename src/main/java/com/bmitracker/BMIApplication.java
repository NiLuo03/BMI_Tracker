package com.bmitracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BMIApplication extends Application {

    public static int currentUserId = -1;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 460, 520);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
        stage.setTitle("BMI 体质评估与预测系统");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
