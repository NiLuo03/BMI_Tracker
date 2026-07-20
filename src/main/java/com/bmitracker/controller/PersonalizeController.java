package com.bmitracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PersonalizeController {

    @FXML private Label hintLabel;

    @FXML void selectBlack(MouseEvent e) { apply("#000000", "纯黑"); }
    @FXML void selectWhite(MouseEvent e) { apply("#ffffff", "纯白"); }

    @FXML
    void selectCustom(MouseEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("选择背景图片");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        Stage stage = (Stage) hintLabel.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            MainController.getInstance().changeBackdropImage(file);
            if (hintLabel != null) hintLabel.setText("已切换为自定义图片");
        }
    }

    private void apply(String color, String name) {
        MainController.getInstance().changeBackdrop(color);
        if (hintLabel != null) hintLabel.setText("已切换为 " + name);
    }
}
