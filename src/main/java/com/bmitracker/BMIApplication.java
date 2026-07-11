package com.bmitracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BMIApplication extends Application {

    public static int currentUserId = -1;

    @Override
    public void start(Stage stage) throws Exception {
        // 前端预览模式：跳过登录，直接进主界面测试所有页面布局
        currentUserId = 1;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("BMI体质评估与预测系统 - 前端预览模式");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
